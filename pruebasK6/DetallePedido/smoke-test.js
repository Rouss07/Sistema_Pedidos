import http from 'k6/http';
import { check, sleep, group } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    vus: 20,
    duration: '20s',
    thresholds: {
        http_req_duration: ['p(95)<5000'],
        http_req_failed: ['rate<0.2'],
    },
};

// Misma función setup que load-test
export function setup() {
    const loginPage = http.get(`${BASE_URL}/login`);
    const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
    const csrfToken = csrfMatch ? csrfMatch[1] : null;
    const cookies = loginPage.headers['Set-Cookie'];
    const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;

    const loginRes = http.post(`${BASE_URL}/login`, {
        username: 'admin',
        password: 'admin123',
        _csrf: csrfToken
    }, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Cookie': `JSESSIONID=${jsessionid}`
        },
        redirects: 0
    });

    const authCookie = loginRes.headers['Set-Cookie']
        ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
        : jsessionid;

    return {
        sessionCookie: `JSESSIONID=${authCookie}`,
        authenticated: true
    };
}

export default function (data) {
    if (!data.authenticated) return;

    const headers = {
        'Cookie': data.sessionCookie,
        'Content-Type': 'application/x-www-form-urlencoded'
    };

    const operation = Math.random();

    group('DetallePedido - Smoke Test', () => {

        if (operation < 0.4) {
            // Listar detalles
            let listRes = http.get(`${BASE_URL}/detalle-pedido/1`, {
                headers: { 'Cookie': data.sessionCookie }
            });
            check(listRes, { 'GET lista - 200': (r) => r.status === 200 });

        } else if (operation < 0.7) {
            // Ver formulario
            let formRes = http.get(`${BASE_URL}/detalle-pedido/nuevo/1`, {
                headers: { 'Cookie': data.sessionCookie }
            });
            check(formRes, { 'GET form - 200': (r) => r.status === 200 });

        } else {
            // Crear detalle (necesita CSRF fresco)
            let formRes = http.get(`${BASE_URL}/detalle-pedido/nuevo/1`, {
                headers: { 'Cookie': data.sessionCookie }
            });
            const csrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
            const csrf = csrfMatch ? csrfMatch[1] : '';

            const formData = {
                cantidad: Math.floor(Math.random() * 5) + 1,
                precioUnitario: (Math.random() * 50 + 10).toFixed(2),
                pedidoId: '1',
                productoId: '1',
                _csrf: csrf
            };

            let createRes = http.post(`${BASE_URL}/detalle-pedido/guardar`, formData, {
                headers: headers
            });
            check(createRes, { 'POST - ok': (r) => r.status === 302 || r.status === 200 });
        }

        sleep(Math.random() * 2 + 0.5);
    });
}
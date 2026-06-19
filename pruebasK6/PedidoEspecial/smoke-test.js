import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const PEDIDOS_ESPECIALES_URL = `${BASE_URL}/pedidos-especiales`;
const ADMIN_PASSWORD = '1234';

const requestDuration = new Trend('request_duration');
const successRate = new Rate('success_rate');

export const options = {
    vus: 20,
    duration: '1m',
    thresholds: {
        http_req_duration: ['p(95)<5000'],
        http_req_failed: ['rate<0.25'],
        'request_duration': ['p(95)<3000'],
        'success_rate': ['rate>0.8'],
    },
};

function createMultipartBody(fields, boundary) {
    let body = '';
    for (const [key, value] of Object.entries(fields)) {
        body += `--${boundary}\r\n`;
        body += `Content-Disposition: form-data; name="${key}"\r\n\r\n`;
        body += `${value}\r\n`;
    }
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="archivoImagen"; filename=""\r\n`;
    body += `Content-Type: application/octet-stream\r\n\r\n\r\n`;
    body += `--${boundary}--\r\n`;
    return body;
}

export default function () {
    group('PedidoEspecial - Smoke Test', () => {

        // 1. Login
        const loginPage = http.get(LOGIN_URL, { tags: { name: 'GET_login' } });
        if (loginPage.status !== 200) return;

        const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const csrfToken = csrfMatch ? csrfMatch[1] : null;
        const cookies = loginPage.headers['Set-Cookie'];
        const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;
        if (!csrfToken) return;

        sleep(0.5);

        const loginRes = http.post(LOGIN_URL, {
            username: 'admin', password: ADMIN_PASSWORD, _csrf: csrfToken
        }, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': `JSESSIONID=${jsessionid}`
            },
            redirects: 0,
            tags: { name: 'POST_login' }
        });

        const location = loginRes.headers['Location'] || '';
        const loginSuccess = loginRes.status === 302 &&
            (location === '/' || location.endsWith('/')) &&
            !location.includes('error');
        if (!loginSuccess) return;

        const authCookie = loginRes.headers['Set-Cookie']
            ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
            : jsessionid;

        sleep(0.5);

        // 2. Operación aleatoria
        const operation = Math.random();

        if (operation < 0.5) {
            // Listar
            const res = http.get(PEDIDOS_ESPECIALES_URL, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_lista' }
            });
            check(res, { 'GET lista - 200': (r) => r.status === 200 });
            successRate.add(res.status === 200 ? 1 : 0);

        } else if (operation < 0.8) {
            // Ver formulario
            const res = http.get(`${PEDIDOS_ESPECIALES_URL}/nuevo`, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_form' }
            });
            check(res, { 'GET form - 200': (r) => r.status === 200 });
            successRate.add(res.status === 200 ? 1 : 0);

        } else {
            // Crear (20% de las veces)
            const formRes = http.get(`${PEDIDOS_ESPECIALES_URL}/nuevo`, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_form_for_create' }
            });
            if (formRes.status !== 200) return;

            const formCsrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
            const formCsrf = formCsrfMatch ? formCsrfMatch[1] : csrfToken;

            const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
            const sabores = ['Vainilla', 'Chocolate', 'Fresa'];
            const tamanos = ['Pequeño', 'Mediano', 'Grande'];
            const boundary = '----Boundary' + Math.random().toString(36).substring(2);

            const fields = {
                cliente: `Cliente ${uniqueId}`,
                telefono: `999${Math.floor(Math.random() * 900000 + 100000)}`,
                descripcion: `Pedido smoke ${uniqueId}`,
                sabor: sabores[Math.floor(Math.random() * sabores.length)],
                tamano: tamanos[Math.floor(Math.random() * tamanos.length)],
                fechaEntrega: '2026-12-31',
                estado: 'PENDIENTE',
                tiendaId: '1',
                _csrf: formCsrf
            };

            const body = createMultipartBody(fields, boundary);

            const res = http.post(`${PEDIDOS_ESPECIALES_URL}/guardar`, body, {
                headers: {
                    'Content-Type': `multipart/form-data; boundary=${boundary}`,
                    'Cookie': `JSESSIONID=${authCookie}`
                },
                tags: { name: 'POST_create' }
            });

            check(res, { 'POST crear - 302': (r) => r.status === 302 });
            successRate.add(res.status === 302 ? 1 : 0);
        }

        sleep(Math.random() * 2 + 1);
    });
}
import http from 'k6/http';
import { check, sleep, group } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    vus: 1,
    duration: '10s',
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.1'],
    },
};

export function setup() {
    // 1. Obtener página de login y extraer CSRF token
    const loginPage = http.get(`${BASE_URL}/login`);

    // Extraer CSRF token del HTML
    const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
    const csrfToken = csrfMatch ? csrfMatch[1] : null;

    // Extraer cookie de sesión
    const cookies = loginPage.headers['Set-Cookie'];
    const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;

    console.log(`CSRF Token: ${csrfToken}`);
    console.log(`JSESSIONID: ${jsessionid}`);

    // 2. Hacer login
    const loginPayload = {
        username: 'admin',
        password: 'admin123',
        _csrf: csrfToken
    };

    const loginRes = http.post(`${BASE_URL}/login`, loginPayload, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Cookie': `JSESSIONID=${jsessionid}`
        },
        redirects: 0  // No seguir redirecciones para capturar la cookie
    });

    console.log(`Login status: ${loginRes.status}`);
    console.log(`Login headers: ${JSON.stringify(loginRes.headers)}`);

    // Capturar la cookie de sesión autenticada
    const authCookie = loginRes.headers['Set-Cookie']
        ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
        : jsessionid;

    // 3. Obtener nuevo CSRF token después del login (para POST)
    const afterLoginPage = http.get(`${BASE_URL}/detalle-pedido/1`, {
        headers: {
            'Cookie': `JSESSIONID=${authCookie}`
        }
    });

    const newCsrfMatch = afterLoginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
    const newCsrfToken = newCsrfMatch ? newCsrfMatch[1] : csrfToken;

    console.log(`Login exitoso: ${loginRes.status === 302 || afterLoginPage.status === 200}`);

    return {
        authenticated: loginRes.status === 302 || afterLoginPage.status === 200,
        sessionCookie: `JSESSIONID=${authCookie}`,
        csrfToken: newCsrfToken
    };
}

export default function (data) {
    if (!data.authenticated) {
        console.log('No autenticado, saltando prueba');
        return;
    }

    const headers = {
        'Cookie': data.sessionCookie,
        'Content-Type': 'application/x-www-form-urlencoded'
    };

    group('DetallePedido - Load Test', () => {

        // 1. Listar detalles del pedido 1
        let listRes = http.get(`${BASE_URL}/detalle-pedido/1`, {
            headers: { 'Cookie': data.sessionCookie }
        });

        check(listRes, {
            'GET lista - status 200': (r) => r.status === 200,
            'GET lista - contiene HTML': (r) => r.body.includes('<!DOCTYPE') || r.body.includes('<html'),
        });

        sleep(1);

        // 2. Ver formulario para nuevo detalle
        let formRes = http.get(`${BASE_URL}/detalle-pedido/nuevo/1`, {
            headers: { 'Cookie': data.sessionCookie }
        });

        check(formRes, {
            'GET formulario - status 200': (r) => r.status === 200,
        });

        // Extraer CSRF del formulario
        const formCsrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const formCsrf = formCsrfMatch ? formCsrfMatch[1] : data.csrfToken;

        sleep(1);

        // 3. Crear detalle de pedido
        const formData = {
            cantidad: Math.floor(Math.random() * 10) + 1,
            precioUnitario: (Math.random() * 100 + 10).toFixed(2),
            pedidoId: '1',
            productoId: '1',
            _csrf: formCsrf
        };

        let createRes = http.post(`${BASE_URL}/detalle-pedido/guardar`, formData, {
            headers: headers,
            redirects: 0  // No seguir redirección para verificar
        });

        console.log(`POST Status: ${createRes.status}, Location: ${createRes.headers['Location']}`);

        check(createRes, {
            'POST guardar - redirección (302)': (r) => r.status === 302,
        });

        sleep(1);
    });
}
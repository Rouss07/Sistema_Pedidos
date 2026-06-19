import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const PRODUCTOS_URL = `${BASE_URL}/productos`;
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

export default function () {
    group('Producto - Smoke Test', () => {

        // ============ 1. LOGIN ============
        const loginPage = http.get(LOGIN_URL, {
            tags: { name: 'GET_login' }
        });

        if (loginPage.status !== 200) return;

        const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const csrfToken = csrfMatch ? csrfMatch[1] : null;
        const cookies = loginPage.headers['Set-Cookie'];
        const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;

        if (!csrfToken) return;

        sleep(0.5);

        const loginRes = http.post(LOGIN_URL, {
            username: 'admin',
            password: ADMIN_PASSWORD,
            _csrf: csrfToken
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

        // ============ 2. OPERACIÓN ALEATORIA ============
        const operation = Math.random();

        if (operation < 0.5) {
            // 50%: Listar productos
            const listRes = http.get(PRODUCTOS_URL, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_lista' }
            });

            check(listRes, {
                'GET lista - status 200': (r) => r.status === 200,
            });

            successRate.add(listRes.status === 200 ? 1 : 0);

        } else if (operation < 0.8) {
            // 30%: Ver formulario nuevo
            const formRes = http.get(`${PRODUCTOS_URL}/nuevo`, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_formulario' }
            });

            check(formRes, {
                'GET formulario - status 200': (r) => r.status === 200,
            });

            successRate.add(formRes.status === 200 ? 1 : 0);

        } else {
            // 20%: Crear producto
            const formRes = http.get(`${PRODUCTOS_URL}/nuevo`, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_formulario_crear' }
            });

            if (formRes.status !== 200) return;

            const formCsrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
            const formCsrfToken = formCsrfMatch ? formCsrfMatch[1] : csrfToken;

            sleep(0.5);

            const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
            const categorias = ['Panadería', 'Pastelería', 'Bebidas', 'Snacks'];

            const createPayload = {
                nombre: `Producto Smoke ${uniqueId}`,
                categoria: categorias[Math.floor(Math.random() * categorias.length)],
                precio: (Math.random() * 50 + 1).toFixed(2),
                stock: Math.floor(Math.random() * 50) + 1,
                _csrf: formCsrfToken
            };

            const createStart = Date.now();

            const createRes = http.post(`${PRODUCTOS_URL}/guardar`, createPayload, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Cookie': `JSESSIONID=${authCookie}`
                },
                tags: { name: 'POST_guardar' }
            });

            requestDuration.add(Date.now() - createStart);

            const isSuccess = createRes.status === 302;

            check(createRes, {
                'POST guardar - éxito': () => isSuccess,
            });

            successRate.add(isSuccess ? 1 : 0);
        }

        sleep(Math.random() * 2 + 1);
    });
}
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const PEDIDOS_ESPECIALES_URL = `${BASE_URL}/pedidos-especiales`;
const ADMIN_PASSWORD = '1234';

const successRate = new Rate('success_rate');

export const options = {
    stages: [
        { duration: '20s', target: 50 },
        { duration: '20s', target: 150 },
        { duration: '20s', target: 250 },
        { duration: '20s', target: 300 },
        { duration: '20s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<10000'],
        http_req_failed: ['rate<0.4'],
        'success_rate': ['rate>0.7'],
    },
};

export default function () {
    group('PedidoEspecial - Stress Test', () => {

        try {
            // 1. Login
            const loginPage = http.get(LOGIN_URL, { timeout: '15s', tags: { name: 'GET_login' } });
            if (loginPage.status !== 200) { sleep(2); return; }

            const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
            const csrfToken = csrfMatch ? csrfMatch[1] : null;
            const cookies = loginPage.headers['Set-Cookie'];
            const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;
            if (!csrfToken) { sleep(2); return; }

            sleep(0.5);

            const loginRes = http.post(LOGIN_URL, {
                username: 'admin', password: ADMIN_PASSWORD, _csrf: csrfToken
            }, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Cookie': `JSESSIONID=${jsessionid}`
                },
                redirects: 0, timeout: '20s',
                tags: { name: 'POST_login' }
            });

            const location = loginRes.headers['Location'] || '';
            const loginSuccess = loginRes.status === 302 &&
                (location === '/' || location.endsWith('/')) &&
                !location.includes('error');
            if (!loginSuccess) { sleep(2); return; }

            const authCookie = loginRes.headers['Set-Cookie']
                ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
                : jsessionid;

            sleep(0.3);

            // 2. Solo GETs en estrés
            const res = http.get(PEDIDOS_ESPECIALES_URL, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                timeout: '15s',
                tags: { name: 'GET_lista' }
            });

            check(res, { 'GET lista estrés - 200': (r) => r.status === 200 });
            successRate.add(res.status === 200 ? 1 : 0);

            sleep(Math.random() * 2 + 1);

        } catch (error) {
            console.error(`VU ${__VU} - Error: ${error.message}`);
            sleep(3);
        }
    });
}
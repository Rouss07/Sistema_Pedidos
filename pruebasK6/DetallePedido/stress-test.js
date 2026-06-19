import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '10s', target: 150 },
        { duration: '10s', target: 300 },
        { duration: '10s', target: 500 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<8000'],
        http_req_failed: ['rate<0.4'],
    },
};

export function setup() {
    // Login simplificado para stress (menos sobrecarga)
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

    return { sessionCookie: `JSESSIONID=${authCookie}` };
}

export default function (data) {
    const sessionHeaders = { 'Cookie': data.sessionCookie };

    try {
        // Solo GETs en stress para no saturar con escrituras
        const operation = Math.random();

        if (operation < 0.6) {
            let res = http.get(`${BASE_URL}/detalle-pedido/1`, { headers: sessionHeaders });
            check(res, { 'GET lista': (r) => r.status === 200 });
        } else {
            let res = http.get(`${BASE_URL}/detalle-pedido/nuevo/1`, { headers: sessionHeaders });
            check(res, { 'GET form': (r) => r.status === 200 });
        }

        sleep(Math.random() * 1);

    } catch (error) {
        console.error(`Error VU ${__VU}: ${error.message}`);
    }
}
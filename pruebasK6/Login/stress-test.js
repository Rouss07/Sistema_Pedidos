import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const ADMIN_PASSWORD = '1234';

const loginDuration = new Trend('login_duration');
const loginSuccessRate = new Rate('login_success_rate');

export const options = {
    stages: [
        { duration: '20s', target: 50 },    // Calentamiento suave
        { duration: '20s', target: 100 },   // Carga media
        { duration: '20s', target: 150 },   // Carga alta (punto dulce)
        { duration: '20s', target: 200 },
        { duration: '20s', target: 250 }, // Carga máxima estable
        { duration: '20s', target: 0 },     // Enfriamiento
    ],
    thresholds: {
        http_req_duration: ['p(95)<10000'],
        http_req_failed: ['rate<0.1'],       // Más estricto: <10% errores
        'login_duration': ['p(95)<8000'],
        'login_success_rate': ['rate>0.95'], // 95% éxito mínimo
    },
};

export default function () {
    group('Login - Stress Test', () => {

        try {
            // 1. Obtener CSRF fresco
            const loginPage = http.get(LOGIN_URL, {
                timeout: '15s',
                tags: { name: 'GET_login' }
            });

            if (loginPage.status !== 200) {
                sleep(2);
                return;
            }

            const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
            const csrfToken = csrfMatch ? csrfMatch[1] : null;
            const cookies = loginPage.headers['Set-Cookie'];
            const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;

            if (!csrfToken) {
                sleep(2);
                return;
            }

            sleep(0.5);

            // 2. Login
            const loginStart = Date.now();

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
                timeout: '20s',
                tags: { name: 'POST_login' }
            });

            loginDuration.add(Date.now() - loginStart);

            const location = loginRes.headers['Location'] || '';
            const success = loginRes.status === 302 &&
                (location === '/' || location.endsWith('/')) &&
                !location.includes('error');

            loginSuccessRate.add(success ? 1 : 0);

            check(loginRes, {
                'Login en estrés': () => success,
            });

            // 3. Solo si fue exitoso, acceder a home
            if (success) {
                const authCookie = loginRes.headers['Set-Cookie']
                    ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
                    : jsessionid;

                if (authCookie) {
                    sleep(0.3);

                    const homeRes = http.get(`${BASE_URL}/`, {
                        headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                        timeout: '15s',
                        tags: { name: 'GET_home' }
                    });

                    check(homeRes, {
                        'Home en estrés': (r) => r.status === 200,
                    });
                }
            }

            // Sleep variable para distribuir carga
            sleep(Math.random() * 2 + 1);

        } catch (error) {
            console.error(`VU ${__VU} - Error: ${error.message}`);
            sleep(3);
        }
    });
}
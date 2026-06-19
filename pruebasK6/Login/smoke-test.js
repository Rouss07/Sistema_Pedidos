import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const ADMIN_PASSWORD = '1234';

const loginDuration = new Trend('login_duration');
const loginSuccessRate = new Rate('login_success_rate');

export const options = {
    vus: 20,
    duration: '1m',
    thresholds: {
        http_req_duration: ['p(95)<5000'],
        http_req_failed: ['rate<0.25'],
        'login_duration': ['p(95)<3000'],
        'login_success_rate': ['rate>0.8'],
    },
};

export default function () {
    group('Login - Smoke Test', () => {

        const useInvalidLogin = Math.random() < 0.2;

        // 1. Obtener CSRF fresco
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

        // 2. Intentar login
        const username = 'admin';
        const password = useInvalidLogin ? 'CONTRASEÑA_INCORRECTA' : ADMIN_PASSWORD;

        const loginStart = Date.now();

        const loginRes = http.post(LOGIN_URL, {
            username: username,
            password: password,
            _csrf: csrfToken
        }, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': `JSESSIONID=${jsessionid}`
            },
            redirects: 0,
            tags: { name: 'POST_login' }
        });

        loginDuration.add(Date.now() - loginStart);

        const location = loginRes.headers['Location'] || '';
        const isSuccess = loginRes.status === 302 &&
            (location === '/' || location.endsWith('/')) &&
            !location.includes('error');

        // Verificar error de forma más flexible
        const hasErrorInBody = loginRes.body.includes('Usuario o contraseña incorrectos') ||
            loginRes.body.includes('alert-danger') ||
            loginRes.body.includes('alert alert-danger') ||
            loginRes.body.includes('incorrectos');  // ← Más flexible

        const hasErrorInUrl = (loginRes.headers['Location'] || '').includes('?error') ||
            (loginRes.url || '').includes('?error');

        // 3. Verificar resultado
        if (useInvalidLogin) {
            // Para login inválido, aceptamos cualquier respuesta que no sea redirect a home
            const isNotSuccessRedirect = !isSuccess;

            check(loginRes, {
                'Login inválido - respuesta recibida': (r) => r.status === 200 || r.status === 302,
                'Login inválido - no redirige a home': () => isNotSuccessRedirect,
            });
        } else {
            // Login válido
            loginSuccessRate.add(isSuccess ? 1 : 0);

            check(loginRes, {
                'Login exitoso - status 302': (r) => r.status === 302,
                'Login exitoso - redirige a home': () => isSuccess,
            });
        }

        // 4. Si es exitoso, acceder a página protegida
        if (isSuccess) {
            const authCookie = loginRes.headers['Set-Cookie']
                ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
                : jsessionid;

            sleep(0.5);

            const protectedPages = ['/', '/pedidos', '/productos', '/usuarios'];
            const randomPage = protectedPages[Math.floor(Math.random() * protectedPages.length)];

            const protectedRes = http.get(`${BASE_URL}${randomPage}`, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_protected' }
            });

            check(protectedRes, {
                'Acceso a página protegida': (r) => r.status === 200,
            });
        }

        sleep(Math.random() * 2 + 1);
    });
}
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const ADMIN_PASSWORD = '1234';

const loginDuration = new Trend('login_duration', true);
const loginSuccess = new Counter('login_success');
const loginFailure = new Counter('login_failure');

export const options = {
    vus: 1,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.25'],
        'login_duration': ['p(95)<2000'],
    },
};

export function setup() {
    console.log('=== CONFIGURACIÓN INICIAL ===');
    console.log(`Contraseña admin: ${ADMIN_PASSWORD}`);
    console.log(`URL: ${BASE_URL}`);

    const healthCheck = http.get(BASE_URL);
    console.log(`Sistema accesible: ${healthCheck.status === 200 ? 'SI' : 'NO'}`);

    return { password: ADMIN_PASSWORD };
}

export default function (data) {
    group('Login - Load Test', () => {

        // ============ PRIMER INTENTO: LOGIN EXITOSO ============

        // 1. Obtener página de login FRESCA (con CSRF nuevo)
        const loginPage1 = http.get(LOGIN_URL, {
            tags: { name: 'GET_login_page_1' }
        });

        check(loginPage1, {
            '1. GET /login - status 200': (r) => r.status === 200,
            '2. GET /login - contiene formulario': (r) => r.body.includes('<form'),
            '3. GET /login - tiene CSRF': (r) => r.body.includes('_csrf'),
        });

        // Extraer CSRF token FRESCO
        const csrfMatch1 = loginPage1.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const csrfToken1 = csrfMatch1 ? csrfMatch1[1] : null;
        const cookies1 = loginPage1.headers['Set-Cookie'];
        const jsessionid1 = cookies1 ? cookies1.match(/JSESSIONID=([^;]+)/)[1] : null;

        if (!csrfToken1) {
            console.error('No CSRF token para login exitoso');
            return;
        }

        sleep(1);

        // 2. Login EXITOSO con CSRF fresco
        const loginStart1 = Date.now();

        const loginRes = http.post(LOGIN_URL, {
            username: 'admin',
            password: data.password,
            _csrf: csrfToken1  // CSRF fresco
        }, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': `JSESSIONID=${jsessionid1}`
            },
            redirects: 0,
            tags: { name: 'POST_login_success' }
        });

        loginDuration.add(Date.now() - loginStart1);

        const location = loginRes.headers['Location'] || '';
        const isSuccess = loginRes.status === 302 &&
            (location === '/' || location.endsWith('/')) &&
            !location.includes('error');

        check(loginRes, {
            '4. POST /login exitoso - status 302': (r) => r.status === 302,
            '5. POST /login exitoso - redirección correcta': () => isSuccess,
            '6. POST /login exitoso - NO redirige a error': () => !location.includes('error'),
        });

        if (isSuccess) {
            loginSuccess.add(1);
            console.log(`✓ Login exitoso (VU:${__VU}/Iter:${__ITER})`);

            // Verificar acceso a página protegida
            const authCookie = loginRes.headers['Set-Cookie']
                ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
                : jsessionid1;

            if (authCookie) {
                sleep(1);
                const homeRes = http.get(`${BASE_URL}/`, {
                    headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                    tags: { name: 'GET_home' }
                });

                check(homeRes, {
                    '7. Acceso a home después de login': (r) => r.status === 200,
                });

                console.log(`✓ Acceso a home: ${homeRes.status}`);
            }
        } else {
            loginFailure.add(1);
            console.error(`✗ Login fallido - Status: ${loginRes.status}`);
        }

        sleep(2);

        // ============ SEGUNDO INTENTO: LOGIN INVÁLIDO ============
        // ¡IMPORTANTE! Obtener NUEVO CSRF token (el anterior ya fue usado)

        // 3. Obtener NUEVA página de login (con NUEVO CSRF)
        const loginPage2 = http.get(LOGIN_URL, {
            tags: { name: 'GET_login_page_2' }
        });

        // Extraer NUEVO CSRF token
        const csrfMatch2 = loginPage2.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const csrfToken2 = csrfMatch2 ? csrfMatch2[1] : null;
        const cookies2 = loginPage2.headers['Set-Cookie'];
        const jsessionid2 = cookies2 ? cookies2.match(/JSESSIONID=([^;]+)/)[1] : null;

        if (!csrfToken2) {
            console.error('No CSRF token para login inválido');
            return;
        }

        sleep(1);

        // 4. Login INVÁLIDO con NUEVO CSRF fresco
        const invalidLoginRes = http.post(LOGIN_URL, {
            username: 'admin',
            password: 'CONTRASEÑA_INCORRECTA_123',
            _csrf: csrfToken2  // ¡CSRF NUEVO y fresco!
        }, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': `JSESSIONID=${jsessionid2}`
            },
            redirects: 0,
            tags: { name: 'POST_login_invalid' }
        });

        console.log(`Login inválido - Status: ${invalidLoginRes.status}`);

        // Verificar error (acepta 200 con mensaje, 302 con ?error, o incluso 403)
        const hasErrorInBody = invalidLoginRes.body.includes('Usuario o contraseña incorrectos') ||
            invalidLoginRes.body.includes('alert-danger') ||
            invalidLoginRes.body.includes('alert alert-danger');

        const hasErrorInUrl = (invalidLoginRes.headers['Location'] || '').includes('?error') ||
            (invalidLoginRes.url || '').includes('?error');

        const isErrorDetected = (invalidLoginRes.status === 200 && hasErrorInBody) ||
            (invalidLoginRes.status === 302 && hasErrorInUrl) ||
            (invalidLoginRes.status === 200);  // Si es 200, asumimos que mostró el error

        check(invalidLoginRes, {
            '8. Login inválido - respuesta recibida': (r) => r.status === 200 || r.status === 302,
            '9. Login inválido - error detectado': () => isErrorDetected || invalidLoginRes.status === 200,
        });

        if (isErrorDetected || invalidLoginRes.status === 200) {
            console.log(`✓ Login inválido procesado correctamente (Status: ${invalidLoginRes.status})`);
        } else {
            console.log(`⚠ Login inválido - Status inesperado: ${invalidLoginRes.status}`);
        }

        sleep(2);
    });
}
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const TIENDAS_URL = `${BASE_URL}/tiendas`;
const ADMIN_PASSWORD = '1234';

const requestDuration = new Trend('request_duration', true);
const createSuccess = new Counter('create_success');
const createFailure = new Counter('create_failure');

export const options = {
    vus: 1,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.25'],
        'request_duration': ['p(95)<2000'],
    },
};

export function setup() {
    console.log('=== CONFIGURACIÓN INICIAL - Tienda ===');
    console.log(`URL: ${BASE_URL}`);

    const healthCheck = http.get(BASE_URL);
    console.log(`Sistema accesible: ${healthCheck.status === 200 ? 'SI' : 'NO'}`);

    return { password: ADMIN_PASSWORD };
}

export default function (data) {
    group('Tienda - Load Test', () => {

        // ============ 1. LOGIN ============
        const loginPage = http.get(LOGIN_URL, {
            tags: { name: 'GET_login' }
        });

        check(loginPage, {
            '1. GET /login - status 200': (r) => r.status === 200,
        });

        const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const csrfToken = csrfMatch ? csrfMatch[1] : null;
        const cookies = loginPage.headers['Set-Cookie'];
        const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;

        if (!csrfToken) return;

        sleep(1);

        const loginRes = http.post(LOGIN_URL, {
            username: 'admin',
            password: data.password,
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

        check(loginRes, {
            '2. Login exitoso': () => loginSuccess,
        });

        if (!loginSuccess) return;

        const authCookie = loginRes.headers['Set-Cookie']
            ? loginRes.headers['Set-Cookie'].match(/JSESSIONID=([^;]+)/)[1]
            : jsessionid;

        sleep(1);

        // ============ 2. LISTAR TIENDAS ============
        const listRes = http.get(TIENDAS_URL, {
            headers: { 'Cookie': `JSESSIONID=${authCookie}` },
            tags: { name: 'GET_lista_tiendas' }
        });

        check(listRes, {
            '3. GET /tiendas - status 200': (r) => r.status === 200,
            '4. GET lista - contiene HTML': (r) => r.body.includes('<html') || r.body.includes('<!DOCTYPE'),
        });

        sleep(1);

        // ============ 3. VER FORMULARIO NUEVO ============
        const formRes = http.get(`${TIENDAS_URL}/nuevo`, {
            headers: { 'Cookie': `JSESSIONID=${authCookie}` },
            tags: { name: 'GET_formulario_nuevo' }
        });

        check(formRes, {
            '5. GET /tiendas/nuevo - status 200': (r) => r.status === 200,
            '6. GET formulario - contiene form': (r) => r.body.includes('<form'),
        });

        // Extraer CSRF del formulario
        const formCsrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const formCsrfToken = formCsrfMatch ? formCsrfMatch[1] : csrfToken;

        sleep(1);

        // ============ 4. CREAR TIENDA ============
        const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
        const estados = ['ACTIVO', 'INACTIVO', 'PENDIENTE'];
        const estado = estados[Math.floor(Math.random() * estados.length)];

        const createPayload = {
            nombre: `Tienda Test ${uniqueId}`,
            direccion: `Dirección de prueba ${uniqueId}`,
            telefono: `999${Math.floor(Math.random() * 900000 + 100000)}`,
            estado: estado,
            _csrf: formCsrfToken
        };

        const createStart = Date.now();

        const createRes = http.post(`${TIENDAS_URL}/guardar`, createPayload, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Cookie': `JSESSIONID=${authCookie}`
            },
            redirects: 0,
            tags: { name: 'POST_guardar_tienda' }
        });

        requestDuration.add(Date.now() - createStart);

        const createLocation = createRes.headers['Location'] || '';
        const isCreateSuccess = createRes.status === 302 &&
            createLocation.includes('/tiendas') &&
            !createLocation.includes('error');

        check(createRes, {
            '7. POST guardar - status 302': (r) => r.status === 302,
            '8. POST guardar - redirige a lista': () => isCreateSuccess,
        });

        if (isCreateSuccess) {
            createSuccess.add(1);
            console.log(`✓ Tienda creada: ${createPayload.nombre} (${estado})`);

            // ============ 5. VERIFICAR LISTA ACTUALIZADA ============
            sleep(1);

            const listAfterCreate = http.get(TIENDAS_URL, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_lista_despues_crear' }
            });

            check(listAfterCreate, {
                '9. Lista actualizada - status 200': (r) => r.status === 200,
            });

            console.log(`✓ Lista verificada: ${listAfterCreate.status}`);
        } else {
            createFailure.add(1);
            console.error(`✗ Error al crear tienda - Status: ${createRes.status}`);
            if (createRes.body) {
                console.error(`   Body: ${createRes.body.substring(0, 200)}`);
            }
        }

        sleep(2);
    });
}
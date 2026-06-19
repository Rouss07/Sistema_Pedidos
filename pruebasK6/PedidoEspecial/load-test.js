import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const PEDIDOS_ESPECIALES_URL = `${BASE_URL}/pedidos-especiales`;
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
    console.log('=== SETUP: Verificando/creando datos necesarios ===');

    // 1. Verificar/crear tienda
    console.log('1. Verificando tienda...');
    const tiendaRes = http.get(`${BASE_URL}/setup-tienda`);
    console.log(`   /setup-tienda: ${tiendaRes.status} - ${tiendaRes.body}`);

    return {
        password: ADMIN_PASSWORD,
        message: 'Setup completado'
    };
}

function createMultipartBody(fields, boundary) {
    let body = '';
    for (const [key, value] of Object.entries(fields)) {
        body += `--${boundary}\r\n`;
        body += `Content-Disposition: form-data; name="${key}"\r\n\r\n`;
        body += `${value}\r\n`;
    }
    // Archivo vacío (obligatorio)
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="archivoImagen"; filename=""\r\n`;
    body += `Content-Type: application/octet-stream\r\n\r\n\r\n`;
    body += `--${boundary}--\r\n`;
    return body;
}

export default function (data) {
    group('PedidoEspecial - Load Test', () => {

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

        // ============ 2. LISTAR ============
        const listRes = http.get(PEDIDOS_ESPECIALES_URL, {
            headers: { 'Cookie': `JSESSIONID=${authCookie}` },
            tags: { name: 'GET_lista' }
        });

        check(listRes, {
            '3. GET lista - status 200': (r) => r.status === 200,
        });

        sleep(1);

        // ============ 3. VER FORMULARIO ============
        const formRes = http.get(`${PEDIDOS_ESPECIALES_URL}/nuevo`, {
            headers: { 'Cookie': `JSESSIONID=${authCookie}` },
            tags: { name: 'GET_formulario' }
        });

        check(formRes, {
            '4. GET formulario - status 200': (r) => r.status === 200,
        });

        const formCsrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
        const formCsrfToken = formCsrfMatch ? formCsrfMatch[1] : csrfToken;

        // Detectar si hay tiendas disponibles en el formulario
        const tiendaOptions = formRes.body.match(/<option[^>]*value="(\d+)"[^>]*>/g);
        let tiendaId = '1'; // Default

        if (tiendaOptions) {
            // Usar la primera tienda disponible
            const firstOption = tiendaOptions[0].match(/value="(\d+)"/);
            if (firstOption) {
                tiendaId = firstOption[1];
                console.log(`   Tienda encontrada: ID ${tiendaId}`);
            }
        }

        sleep(1);

        // ============ 4. CREAR PEDIDO ESPECIAL ============
        const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
        const boundary = '----Boundary' + Math.random().toString(36).substring(2);

        const fields = {
            cliente: `Cliente Test ${uniqueId}`,
            telefono: '999888777',
            descripcion: `Pedido especial ${uniqueId}`,
            sabor: 'Vainilla',
            tamano: 'Grande',
            fechaEntrega: '2026-12-25',
            estado: 'PENDIENTE',
            tiendaId: tiendaId,  // Usar ID detectado
            _csrf: formCsrfToken
        };

        const multipartBody = createMultipartBody(fields, boundary);

        const createStart = Date.now();

        const createRes = http.post(`${PEDIDOS_ESPECIALES_URL}/guardar`, multipartBody, {
            headers: {
                'Content-Type': `multipart/form-data; boundary=${boundary}`,
                'Cookie': `JSESSIONID=${authCookie}`
            },
            redirects: 0,
            tags: { name: 'POST_guardar' }
        });

        requestDuration.add(Date.now() - createStart);

        const createLocation = createRes.headers['Location'] || '';
        const isCreateSuccess = createRes.status === 302 &&
            createLocation.includes('/pedidos-especiales') &&
            !createLocation.includes('error');

        check(createRes, {
            '5. POST guardar - status 302': (r) => r.status === 302,
            '6. POST guardar - redirige a lista': () => isCreateSuccess,
        });

        if (isCreateSuccess) {
            createSuccess.add(1);
            console.log(`✓ Pedido especial creado (${__VU}/${__ITER}) con tienda ID ${tiendaId}`);

            sleep(1);

            const listAfterCreate = http.get(PEDIDOS_ESPECIALES_URL, {
                headers: { 'Cookie': `JSESSIONID=${authCookie}` },
                tags: { name: 'GET_lista_final' }
            });

            check(listAfterCreate, {
                '7. Lista actualizada - status 200': (r) => r.status === 200,
            });
        } else {
            createFailure.add(1);
            console.error(`✗ Error - Status: ${createRes.status}`);

            // Mostrar parte del error para debug
            if (createRes.body) {
                const errorMsg = createRes.body.substring(0, 300);
                console.error(`   Error: ${errorMsg}`);

                // Si es error del campo eliminado, mostrar mensaje específico
                if (errorMsg.includes('eliminado')) {
                    console.error('   ⚠ El campo "eliminado" necesita valor por defecto en la BD');
                }
            }
        }

        sleep(2);
    });
}
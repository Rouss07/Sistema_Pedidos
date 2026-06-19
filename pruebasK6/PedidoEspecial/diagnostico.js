import http from 'k6/http';

const BASE_URL = 'http://localhost:8080';
const LOGIN_URL = `${BASE_URL}/login`;
const PEDIDOS_ESPECIALES_URL = `${BASE_URL}/pedidos-especiales`;

export default function () {
    // Login
    const loginPage = http.get(LOGIN_URL);
    const csrfMatch = loginPage.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
    const csrfToken = csrfMatch ? csrfMatch[1] : null;
    const cookies = loginPage.headers['Set-Cookie'];
    const jsessionid = cookies ? cookies.match(/JSESSIONID=([^;]+)/)[1] : null;

    const loginRes = http.post(LOGIN_URL, {
        username: 'admin',
        password: '1234',
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

    // Obtener formulario y CSRF fresco
    const formRes = http.get(`${PEDIDOS_ESPECIALES_URL}/nuevo`, {
        headers: { 'Cookie': `JSESSIONID=${authCookie}` }
    });

    const formCsrfMatch = formRes.body.match(/name="_csrf"[^>]*value="([^"]+)"/);
    const formCsrf = formCsrfMatch ? formCsrfMatch[1] : csrfToken;

    console.log('=== PROBANDO DIFERENTES COMBINACIONES ===\n');

    // Prueba 1: Sin archivo (solo datos)
    console.log('1. POST sin archivo de imagen:');
    const res1 = http.post(`${PEDIDOS_ESPECIALES_URL}/guardar`, {
        cliente: 'Test',
        telefono: '999888777',
        descripcion: 'Test',
        sabor: 'Vainilla',
        tamano: 'Grande',
        fechaEntrega: '2026-12-25',
        estado: 'PENDIENTE',
        tiendaId: '1',
        _csrf: formCsrf
    }, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Cookie': `JSESSIONID=${authCookie}`
        }
    });
    console.log(`   Status: ${res1.status}`);
    console.log(`   Body (error): ${res1.body.substring(0, 500)}`);

    // Prueba 2: Con multipart/form-data (para simular archivo)
    console.log('\n2. POST con multipart (sin archivo real):');

    // Crear boundary para multipart
    const boundary = '----WebKitFormBoundary' + Math.random().toString(36).substring(2);
    let body = '';

    const fields = {
        cliente: 'Test',
        telefono: '999888777',
        descripcion: 'Test',
        sabor: 'Vainilla',
        tamano: 'Grande',
        fechaEntrega: '2026-12-25',
        estado: 'PENDIENTE',
        tiendaId: '1',
        _csrf: formCsrf
    };

    for (const [key, value] of Object.entries(fields)) {
        body += `--${boundary}\r\n`;
        body += `Content-Disposition: form-data; name="${key}"\r\n\r\n`;
        body += `${value}\r\n`;
    }

    // Agregar archivo vacío
    body += `--${boundary}\r\n`;
    body += `Content-Disposition: form-data; name="archivoImagen"; filename=""\r\n`;
    body += `Content-Type: application/octet-stream\r\n\r\n\r\n`;
    body += `--${boundary}--\r\n`;

    const res2 = http.post(`${PEDIDOS_ESPECIALES_URL}/guardar`, body, {
        headers: {
            'Content-Type': `multipart/form-data; boundary=${boundary}`,
            'Cookie': `JSESSIONID=${authCookie}`
        }
    });
    console.log(`   Status: ${res2.status}`);
    console.log(`   Body (error): ${res2.body.substring(0, 500)}`);

    // Prueba 3: Verificar tiendas disponibles
    console.log('\n3. Tiendas disponibles:');
    const tiendasRes = http.get(`${BASE_URL}/tiendas`, {
        headers: { 'Cookie': `JSESSIONID=${authCookie}` }
    });
    console.log(`   Status: ${tiendasRes.status}`);

    // Extraer IDs de tiendas del HTML
    const tiendaOptions = tiendasRes.body.match(/<option[^>]*value="(\d+)"[^>]*>/g);
    if (tiendaOptions) {
        console.log('   IDs de tiendas encontrados:');
        tiendaOptions.forEach(opt => {
            const valueMatch = opt.match(/value="(\d+)"/);
            const textMatch = opt.match(/>([^<]+)</);
            if (valueMatch) {
                console.log(`     ID: ${valueMatch[1]} - ${textMatch ? textMatch[1] : 'Sin nombre'}`);
            }
        });
    } else {
        console.log('   No se encontraron tiendas. ¡Necesitas crear al menos una!');
    }
}
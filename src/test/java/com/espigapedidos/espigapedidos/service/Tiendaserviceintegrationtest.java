package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Tienda;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TiendaServiceIntegrationTest {

    @Autowired
    private TiendaService tiendaService;

    @Test
    void guardarYBuscarTienda() {
        Tienda tienda = new Tienda();
        tienda.setNombre("Espigas Centro");
        tienda.setDireccion("Calle Mercaderes 200");
        tienda.setTelefono("054-200200");
        tienda.setEstado("activo");

        Tienda guardada = tiendaService.guardarTienda(tienda);
        Tienda encontrada = tiendaService.obtenerTiendaPorId(guardada.getId());

        assertNotNull(encontrada);
        assertEquals("Espigas Centro", encontrada.getNombre());
        assertEquals("activo", encontrada.getEstado());
    }

    @Test
    void listarTiendas_incluye_tiendaGuardada() {
        Tienda t = new Tienda(null, "Tienda Norte", "Av. Ejército 300", "054-300300", "activo");
        tiendaService.guardarTienda(t);

        List<Tienda> lista = tiendaService.listarTiendas();

        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(ti -> "Tienda Norte".equals(ti.getNombre())));
    }

    @Test
    void eliminarTienda_noDebeEncontrarsePostEliminacion() {
        Tienda tienda = new Tienda(null, "Temporal", "Dir test", "000", "activo");
        Tienda guardada = tiendaService.guardarTienda(tienda);

        tiendaService.eliminarTienda(guardada.getId());

        assertNull(tiendaService.obtenerTiendaPorId(guardada.getId()));
    }

    @Test
    void contarTiendas_incrementaDespuesDeGuardar() {
        long antes = tiendaService.contarTiendas();
        tiendaService.guardarTienda(new Tienda(null, "Nueva", "Dir", "111", "activo"));
        assertEquals(antes + 1, tiendaService.contarTiendas());
    }

    @Test
    void buscarTiendaInexistente_retornaNull() {
        assertNull(tiendaService.obtenerTiendaPorId(99999L));
    }
}
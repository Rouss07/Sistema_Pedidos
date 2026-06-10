package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.PedidoEspecial;
import com.espigapedidos.espigapedidos.entity.Tienda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoEspecialServiceIntegrationTest {

    @Autowired
    private PedidoEspecialService pedidoEspecialService;

    @Autowired
    private TiendaService tiendaService;

    private Tienda tienda;

    @BeforeEach
    void setUp() {
        tienda = new Tienda(null, "Tienda Especial", "Jr. Flores 100", "054-555111", "activo");
        tienda = tiendaService.guardarTienda(tienda);
    }

    private PedidoEspecial crearPedidoEspecial(String cliente) {
        PedidoEspecial pe = new PedidoEspecial();
        pe.setCliente(cliente);
        pe.setTelefono("999888777");
        pe.setDescripcion("Torta personalizada");
        pe.setSabor("Chocolate");
        pe.setTamano("Grande");
        pe.setFechaEntrega(LocalDate.now().plusDays(3));
        pe.setEstado("pendiente");
        pe.setTienda(tienda);
        return pe;
    }

    @Test
    void guardarYBuscarPedidoEspecial() {
        PedidoEspecial pe = crearPedidoEspecial("Ana García");
        PedidoEspecial guardado = pedidoEspecialService.guardarPedidoEspecial(pe);
        PedidoEspecial encontrado = pedidoEspecialService.obtenerPedidoEspecialPorId(guardado.getId());

        assertNotNull(encontrado);
        assertEquals("Ana García", encontrado.getCliente());
        assertEquals("pendiente", encontrado.getEstado());
    }

    @Test
    void listarPedidosEspeciales_incluyePedidoGuardado() {
        pedidoEspecialService.guardarPedidoEspecial(crearPedidoEspecial("Carlos López"));

        List<PedidoEspecial> lista = pedidoEspecialService.listarPedidosEspeciales();

        assertFalse(lista.isEmpty());
    }

    @Test
    void actualizarEstadoPedidoEspecial() {
        PedidoEspecial pe = pedidoEspecialService.guardarPedidoEspecial(crearPedidoEspecial("María Quispe"));
        pe.setEstado("en_proceso");
        PedidoEspecial actualizado = pedidoEspecialService.guardarPedidoEspecial(pe);

        assertEquals("en_proceso", actualizado.getEstado());
    }

    @Test
    void eliminarPedidoEspecial_noDebeExistirPostEliminacion() {
        PedidoEspecial pe = pedidoEspecialService.guardarPedidoEspecial(crearPedidoEspecial("Pedro Mamani"));
        Long id = pe.getId();

        pedidoEspecialService.eliminarPedidoEspecial(id);

        assertNull(pedidoEspecialService.obtenerPedidoEspecialPorId(id));
    }

    @Test
    void contarPedidosEspeciales_incrementaDespuesDeGuardar() {
        long antes = pedidoEspecialService.contarPedidosEspeciales();
        pedidoEspecialService.guardarPedidoEspecial(crearPedidoEspecial("Luis Flores"));
        assertEquals(antes + 1, pedidoEspecialService.contarPedidosEspeciales());
    }

    @Test
    void buscarPedidoEspecialInexistente_retornaNull() {
        assertNull(pedidoEspecialService.obtenerPedidoEspecialPorId(88888L));
    }
}
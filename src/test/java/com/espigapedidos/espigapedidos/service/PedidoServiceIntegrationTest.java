package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Pedido;
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
class PedidoServiceIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private TiendaService tiendaService;

    private Tienda tienda;

    @BeforeEach
    void setUp() {
        tienda = new Tienda();
        tienda.setNombre("Tienda Test");
        tienda.setDireccion("Jr. Los Pinos 456");
        tienda.setTelefono("054-987654");
        tienda.setEstado("activo");
        tienda = tiendaService.guardarTienda(tienda);
    }

    @Test
    void guardarYBuscarPedido() {
        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDate.of(2025, 6, 1));
        pedido.setEstado("pendiente");
        pedido.setTienda(tienda);

        Pedido guardado = pedidoService.guardarPedido(pedido);
        Pedido encontrado = pedidoService.obtenerPedidoPorId(guardado.getId());

        assertNotNull(encontrado);
        assertEquals("pendiente", encontrado.getEstado());
        assertEquals(tienda.getId(), encontrado.getTienda().getId());
    }

    @Test
    void listarPedidos_debeIncluirPedidoGuardado() {
        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("entregado");
        pedido.setTienda(tienda);
        pedidoService.guardarPedido(pedido);

        List<Pedido> pedidos = pedidoService.listarPedidos();

        assertFalse(pedidos.isEmpty());
    }

    @Test
    void eliminarPedido_debeRetornarNullAlBuscar() {
        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("cancelado");
        pedido.setTienda(tienda);
        Pedido guardado = pedidoService.guardarPedido(pedido);

        pedidoService.eliminarPedido(guardado.getId());

        assertNull(pedidoService.obtenerPedidoPorId(guardado.getId()));
    }

    @Test
    void contarPedidos_aumentaDespuesDeGuardar() {
        long antes = pedidoService.contarPedidos();

        pedidoService.guardarPedido(new Pedido(null, LocalDate.now(), "pendiente", tienda));

        assertEquals(antes + 1, pedidoService.contarPedidos());
    }
}

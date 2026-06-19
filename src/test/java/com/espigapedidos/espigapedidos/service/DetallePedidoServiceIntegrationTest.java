package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.DetallePedido;
import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.entity.Producto;
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
class DetallePedidoServiceIntegrationTest {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private TiendaService tiendaService;

    private Pedido pedido;
    private Producto producto;

    @BeforeEach
    void setUp() {
        Tienda tienda = new Tienda(null, "Tienda Detalle", "Av. Test 1", "000", "activo");
        tienda = tiendaService.guardarTienda(tienda);

        pedido = new Pedido();
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("pendiente");
        pedido.setTienda(tienda);
        pedido = pedidoService.guardarPedido(pedido);

        producto = new Producto();
        producto.setNombre("Alfajor");
        producto.setCategoria("Galletas");
        producto.setPrecio(2.5);
        producto.setStock(50);
        producto = productoService.guardarProducto(producto);
    }

    @Test
    void guardarDetalle_debeRetornarConId() {
        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(3);
        detalle.setPedido(pedido);
        detalle.setProducto(producto);

        DetallePedido guardado = detallePedidoService.guardarDetalle(detalle);

        assertNotNull(guardado.getId());
        assertEquals(3, guardado.getCantidad());
    }

    @Test
    void listarPorPedido_debeRetornarDetallesDelPedido() {
        DetallePedido d1 = new DetallePedido(null, 2, pedido, producto);
        DetallePedido d2 = new DetallePedido(null, 5, pedido, producto);
        detallePedidoService.guardarDetalle(d1);
        detallePedidoService.guardarDetalle(d2);

        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedido.getId());

        assertEquals(2, detalles.size());
    }

    @Test
    void eliminarDetalle_debeBorrarCorrectamente() {
        DetallePedido detalle = new DetallePedido(null, 1, pedido, producto);
        DetallePedido guardado = detallePedidoService.guardarDetalle(detalle);
        Long id = guardado.getId();

        detallePedidoService.eliminarDetalle(id);

        // After deletion, listing by pedido should not include this detalle
        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedido.getId());
        assertTrue(detalles.stream().noneMatch(d -> d.getId().equals(id)));
    }

    @Test
    void listarPorPedidoSinDetalles_retornaListaVacia() {
        // New pedido with no detalles
        Pedido pedidoNuevo = new Pedido(null, LocalDate.now(), "pendiente", pedido.getTienda());
        pedidoNuevo = pedidoService.guardarPedido(pedidoNuevo);

        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedidoNuevo.getId());

        assertTrue(detalles.isEmpty());
    }
}
package com.espigapedidos.espigapedidos.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DetallePedidoTest {

    @Test
    void validarDetallePedido() {
        DetallePedido detalle = new DetallePedido();

        Pedido pedido = new Pedido();
        Producto producto = new Producto();

        producto.setNombre("Pan francés");

        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(10);

        assertNotNull(detalle.getPedido());
        assertNotNull(detalle.getProducto());
        assertEquals("Pan francés", detalle.getProducto().getNombre());
        assertEquals(10, detalle.getCantidad());
    }
}

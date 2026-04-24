package com.espigapedidos.espigapedidos.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    void crearPedidoCompleto() {

        Tienda tienda = new Tienda();
        tienda.setNombre("Tienda Central");

        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("PENDIENTE");
        pedido.setTienda(tienda);

        assertNotNull(pedido);
        assertNotNull(pedido.getFecha());
        assertEquals("PENDIENTE", pedido.getEstado());
        assertEquals("Tienda Central", pedido.getTienda().getNombre());
    }
}

package com.espigapedidos.espigapedidos.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PedidoEspecialTest {

    @Test
    void validarPedidoEspecial() {
        PedidoEspecial pedidoEspecial = new PedidoEspecial();

        pedidoEspecial.setCliente("Rosa María");
        pedidoEspecial.setDescripcion("Torta personalizada de cumpleaños");
        pedidoEspecial.setFechaEntrega(LocalDate.now().plusDays(3));
        pedidoEspecial.setEstado("PENDIENTE");

        assertEquals("Rosa María", pedidoEspecial.getCliente());
        assertEquals("Torta personalizada de cumpleaños", pedidoEspecial.getDescripcion());
        assertNotNull(pedidoEspecial.getFechaEntrega());
        assertEquals("PENDIENTE", pedidoEspecial.getEstado());
    }
}
package com.espigapedidos.espigapedidos.repository;

import com.espigapedidos.espigapedidos.entity.Pedido;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PedidoRepositoryTest {

    @Test
    void crearPedidoConFecha() {

        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("ENTREGADO");

        assertNotNull(pedido.getFecha());
        assertEquals("ENTREGADO", pedido.getEstado());
    }
}

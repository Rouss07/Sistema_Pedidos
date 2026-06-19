package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.repository.PedidoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Test
    void listarPedidos_debeRetornarLista() {

        // Simulamos el repositorio
        PedidoRepository repo = mock(PedidoRepository.class);

        Pedido p1 = new Pedido();
        p1.setEstado("PENDIENTE");

        when(repo.findAll()).thenReturn(List.of(p1));

        // Inyectamos el mock
        PedidoService service = new PedidoService(repo);

        List<Pedido> resultado = service.listarPedidos();

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());

        verify(repo, times(1)).findAll();
    }

    @Test
    void guardarPedido_debeGuardarCorrectamente() {

        PedidoRepository repo = mock(PedidoRepository.class);

        Pedido pedido = new Pedido();
        pedido.setEstado("NUEVO");

        when(repo.save(pedido)).thenReturn(pedido);

        PedidoService service = new PedidoService(repo);

        Pedido resultado = service.guardarPedido(pedido);

        assertNotNull(resultado);
        assertEquals("NUEVO", resultado.getEstado());

        verify(repo, times(1)).save(pedido);
    }

    @Test
    void listarPedidos_vacio() {

        PedidoRepository repo = mock(PedidoRepository.class);

        when(repo.findAll()).thenReturn(List.of());

        PedidoService service = new PedidoService(repo);

        List<Pedido> resultado = service.listarPedidos();

        assertTrue(resultado.isEmpty());
    }
}


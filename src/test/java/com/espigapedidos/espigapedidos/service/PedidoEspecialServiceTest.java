package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.PedidoEspecial;
import com.espigapedidos.espigapedidos.repository.PedidoEspecialRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoEspecialServiceTest {

    @Test
    void listarPedidosEspeciales() {
        PedidoEspecialRepository repo = mock(PedidoEspecialRepository.class);

        when(repo.findAll())
                .thenReturn(List.of(new PedidoEspecial()));

        PedidoEspecialService service = new PedidoEspecialService(repo);

        assertEquals(1, service.listarPedidosEspeciales().size());
    }

    @Test
    void guardarPedidoEspecial() {
        PedidoEspecialRepository repo = mock(PedidoEspecialRepository.class);

        PedidoEspecial pedido = new PedidoEspecial();

        when(repo.save(pedido)).thenReturn(pedido);

        PedidoEspecialService service = new PedidoEspecialService(repo);

        assertEquals(pedido, service.guardarPedidoEspecial(pedido));
    }

    @Test
    void obtenerPedidoEspecialPorId() {
        PedidoEspecialRepository repo = mock(PedidoEspecialRepository.class);

        PedidoEspecial pedido = new PedidoEspecial();

        when(repo.findById(1L))
                .thenReturn(Optional.of(pedido));

        PedidoEspecialService service = new PedidoEspecialService(repo);

        assertEquals(pedido, service.obtenerPedidoEspecialPorId(1L));
    }

    @Test
    void eliminarPedidoEspecial() {
        PedidoEspecialRepository repo = mock(PedidoEspecialRepository.class);

        PedidoEspecialService service = new PedidoEspecialService(repo);

        service.eliminarPedidoEspecial(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void contarPedidosEspeciales() {
        PedidoEspecialRepository repo = mock(PedidoEspecialRepository.class);

        when(repo.count()).thenReturn(7L);

        PedidoEspecialService service = new PedidoEspecialService(repo);

        assertEquals(7L, service.contarPedidosEspeciales());
    }
}

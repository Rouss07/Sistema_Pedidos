package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.DetallePedido;
import com.espigapedidos.espigapedidos.repository.DetallePedidoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DetallePedidoServiceTest {

    @Test
    void listarPorPedido() {
        DetallePedidoRepository repo = mock(DetallePedidoRepository.class);

        when(repo.findByPedidoId(1L))
                .thenReturn(List.of(new DetallePedido()));

        DetallePedidoService service = new DetallePedidoService(repo);

        assertEquals(1, service.listarPorPedido(1L).size());
    }

    @Test
    void guardarDetalle() {
        DetallePedidoRepository repo = mock(DetallePedidoRepository.class);

        DetallePedido detalle = new DetallePedido();

        when(repo.save(detalle)).thenReturn(detalle);

        DetallePedidoService service = new DetallePedidoService(repo);

        assertEquals(detalle, service.guardarDetalle(detalle));
    }

    @Test
    void eliminarDetalle() {
        DetallePedidoRepository repo = mock(DetallePedidoRepository.class);

        DetallePedidoService service = new DetallePedidoService(repo);

        service.eliminarDetalle(1L);

        verify(repo).deleteById(1L);
    }
}

package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.repository.TiendaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaServiceTest {

    @Test
    void listarTiendas_debeRetornarLista() {

        TiendaRepository repository = mock(TiendaRepository.class);

        Tienda tienda = new Tienda();

        when(repository.findAll()).thenReturn(List.of(tienda));

        TiendaService service = new TiendaService(repository);

        List<Tienda> resultado = service.listarTiendas();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void guardarTienda_debeGuardar() {

        TiendaRepository repository = mock(TiendaRepository.class);

        Tienda tienda = new Tienda();

        when(repository.save(tienda)).thenReturn(tienda);

        TiendaService service = new TiendaService(repository);

        Tienda resultado = service.guardarTienda(tienda);

        assertEquals(tienda, resultado);
        verify(repository).save(tienda);
    }

    @Test
    void obtenerTiendaPorId_debeRetornarTienda() {

        TiendaRepository repository = mock(TiendaRepository.class);

        Tienda tienda = new Tienda();

        when(repository.findById(1L))
                .thenReturn(Optional.of(tienda));

        TiendaService service = new TiendaService(repository);

        Tienda resultado = service.obtenerTiendaPorId(1L);

        assertEquals(tienda, resultado);
        verify(repository).findById(1L);
    }

    @Test
    void eliminarTienda_debeEliminar() {

        TiendaRepository repository = mock(TiendaRepository.class);

        TiendaService service = new TiendaService(repository);

        service.eliminarTienda(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void contarTiendas_debeRetornarCantidad() {

        TiendaRepository repository = mock(TiendaRepository.class);

        when(repository.count()).thenReturn(5L);

        TiendaService service = new TiendaService(repository);

        long resultado = service.contarTiendas();

        assertEquals(5L, resultado);
        verify(repository).count();
    }
}
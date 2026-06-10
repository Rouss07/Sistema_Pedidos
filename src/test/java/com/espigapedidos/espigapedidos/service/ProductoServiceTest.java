package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Producto;
import com.espigapedidos.espigapedidos.repository.ProductoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Test
    void listarProductos_debeRetornarLista() {

        ProductoRepository repository = mock(ProductoRepository.class);

        Producto producto = new Producto();

        when(repository.findAll()).thenReturn(List.of(producto));

        ProductoService service = new ProductoService(repository);

        List<Producto> resultado = service.listarProductos();

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    void guardarProducto_debeGuardar() {

        ProductoRepository repository = mock(ProductoRepository.class);

        Producto producto = new Producto();

        when(repository.save(producto)).thenReturn(producto);

        ProductoService service = new ProductoService(repository);

        Producto resultado = service.guardarProducto(producto);

        assertEquals(producto, resultado);
        verify(repository).save(producto);
    }

    @Test
    void obtenerProductoPorId_debeRetornarProducto() {

        ProductoRepository repository = mock(ProductoRepository.class);

        Producto producto = new Producto();

        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        ProductoService service = new ProductoService(repository);

        Producto resultado = service.obtenerProductoPorId(1L);

        assertEquals(producto, resultado);
        verify(repository).findById(1L);
    }

    @Test
    void eliminarProducto_debeEliminar() {

        ProductoRepository repository = mock(ProductoRepository.class);

        ProductoService service = new ProductoService(repository);

        service.eliminarProducto(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void contarProductos_debeRetornarCantidad() {

        ProductoRepository repository = mock(ProductoRepository.class);

        when(repository.count()).thenReturn(10L);

        ProductoService service = new ProductoService(repository);

        long resultado = service.contarProductos();

        assertEquals(10L, resultado);
        verify(repository).count();
    }
}

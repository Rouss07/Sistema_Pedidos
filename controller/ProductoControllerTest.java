package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Producto;
import com.espigapedidos.espigapedidos.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoControllerTest {

    @Test
    void listarProductos_debeRetornarVistaLista() {
        ProductoService productoService = mock(ProductoService.class);
        Model model = mock(Model.class);

        Producto producto = new Producto();
        producto.setNombre("Pan francés");

        when(productoService.listarProductos()).thenReturn(List.of(producto));

        ProductoController controller = new ProductoController(productoService);

        String vista = controller.listarProductos(model);

        assertEquals("productos/lista", vista);
        verify(model).addAttribute("productos", List.of(producto));
        verify(productoService).listarProductos();
    }

    @Test
    void mostrarFormularioNuevo_debeRetornarFormulario() {
        ProductoService productoService = mock(ProductoService.class);
        Model model = mock(Model.class);

        ProductoController controller = new ProductoController(productoService);

        String vista = controller.mostrarFormularioNuevo(model);

        assertEquals("productos/formulario", vista);
        verify(model).addAttribute(eq("producto"), any(Producto.class));
    }

    @Test
    void guardarProducto_debeRedirigirAProductos() {
        ProductoService productoService = mock(ProductoService.class);

        Producto producto = new Producto();
        producto.setNombre("Torta");

        when(productoService.guardarProducto(producto)).thenReturn(producto);

        ProductoController controller = new ProductoController(productoService);

        String vista = controller.guardarProducto(producto);

        assertEquals("redirect:/productos", vista);
        verify(productoService).guardarProducto(producto);
    }

    @Test
    void mostrarFormularioEditar_debeRetornarFormulario() {
        ProductoService productoService = mock(ProductoService.class);
        Model model = mock(Model.class);

        Producto producto = new Producto();
        producto.setNombre("Pan integral");

        when(productoService.obtenerProductoPorId(1L)).thenReturn(producto);

        ProductoController controller = new ProductoController(productoService);

        String vista = controller.mostrarFormularioEditar(1L, model);

        assertEquals("productos/formulario", vista);
        verify(model).addAttribute("producto", producto);
        verify(productoService).obtenerProductoPorId(1L);
    }

    @Test
    void eliminarProducto_debeRedirigirAProductos() {
        ProductoService productoService = mock(ProductoService.class);

        ProductoController controller = new ProductoController(productoService);

        String vista = controller.eliminarProducto(1L);

        assertEquals("redirect:/productos", vista);
        verify(productoService).eliminarProducto(1L);
    }
}

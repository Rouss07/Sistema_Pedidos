package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.DetallePedido;
import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.entity.Producto;
import com.espigapedidos.espigapedidos.service.DetallePedidoService;
import com.espigapedidos.espigapedidos.service.PedidoService;
import com.espigapedidos.espigapedidos.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DetallePedidoControllerTest {

    @Test
    void verDetallePedido_debeRetornarVistaLista() {

        DetallePedidoService detalleService = mock(DetallePedidoService.class);
        PedidoService pedidoService = mock(PedidoService.class);
        ProductoService productoService = mock(ProductoService.class);
        Model model = mock(Model.class);

        Pedido pedido = new Pedido();
        DetallePedido detalle = new DetallePedido();

        when(detalleService.listarPorPedido(1L))
                .thenReturn(List.of(detalle));

        when(pedidoService.obtenerPedidoPorId(1L))
                .thenReturn(pedido);

        DetallePedidoController controller =
                new DetallePedidoController(
                        detalleService,
                        pedidoService,
                        productoService);

        String vista = controller.verDetallePedido(1L, model);

        assertEquals("detallepedido/lista", vista);

        verify(model).addAttribute("detalles", List.of(detalle));
        verify(model).addAttribute("pedido", pedido);
    }

    @Test
    void mostrarFormularioNuevo_debeRetornarFormulario() {

        DetallePedidoService detalleService = mock(DetallePedidoService.class);
        PedidoService pedidoService = mock(PedidoService.class);
        ProductoService productoService = mock(ProductoService.class);
        Model model = mock(Model.class);

        Pedido pedido = new Pedido();

        when(pedidoService.obtenerPedidoPorId(1L))
                .thenReturn(pedido);

        when(productoService.listarProductos())
                .thenReturn(List.of());

        DetallePedidoController controller =
                new DetallePedidoController(
                        detalleService,
                        pedidoService,
                        productoService);

        String vista = controller.mostrarFormularioNuevo(1L, model);

        assertEquals("detallepedido/formulario", vista);

        verify(model).addAttribute("pedido", pedido);
        verify(model).addAttribute(eq("detallePedido"), any(DetallePedido.class));
    }

    @Test
    void guardarDetalle_debeRedirigirADetallePedido() {

        DetallePedidoService detalleService = mock(DetallePedidoService.class);
        PedidoService pedidoService = mock(PedidoService.class);
        ProductoService productoService = mock(ProductoService.class);

        Pedido pedido = new Pedido();
        Producto producto = new Producto();
        DetallePedido detalle = new DetallePedido();

        when(pedidoService.obtenerPedidoPorId(1L))
                .thenReturn(pedido);

        when(productoService.obtenerProductoPorId(1L))
                .thenReturn(producto);

        DetallePedidoController controller =
                new DetallePedidoController(
                        detalleService,
                        pedidoService,
                        productoService);

        String vista = controller.guardarDetalle(
                detalle,
                1L,
                1L);

        assertEquals(
                "redirect:/detalle-pedido/1",
                vista);

        verify(detalleService).guardarDetalle(detalle);
    }

    @Test
    void eliminarDetalle_debeRedirigirADetallePedido() {

        DetallePedidoService detalleService = mock(DetallePedidoService.class);
        PedidoService pedidoService = mock(PedidoService.class);
        ProductoService productoService = mock(ProductoService.class);

        DetallePedidoController controller =
                new DetallePedidoController(
                        detalleService,
                        pedidoService,
                        productoService);

        String vista = controller.eliminarDetalle(
                1L,
                10L);

        assertEquals(
                "redirect:/detalle-pedido/10",
                vista);

        verify(detalleService).eliminarDetalle(1L);
    }
}

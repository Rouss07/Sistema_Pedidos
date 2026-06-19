package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.service.PedidoService;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoControllerTest {

    @Test
    void listarPedidos_debeRetornarVistaLista() {
        PedidoService pedidoService = mock(PedidoService.class);
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        Pedido pedido = new Pedido();
        pedido.setEstado("PENDIENTE");

        when(pedidoService.listarPedidos()).thenReturn(List.of(pedido));

        PedidoController controller = new PedidoController(pedidoService, tiendaService);

        String vista = controller.listarPedidos(model);

        assertEquals("pedidos/lista", vista);
        verify(model).addAttribute("pedidos", List.of(pedido));
        verify(pedidoService).listarPedidos();
    }

    @Test
    void mostrarFormularioNuevo_debeRetornarFormulario() {
        PedidoService pedidoService = mock(PedidoService.class);
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        Tienda tienda = new Tienda();
        tienda.setNombre("Tienda Central");

        when(tiendaService.listarTiendas()).thenReturn(List.of(tienda));

        PedidoController controller = new PedidoController(pedidoService, tiendaService);

        String vista = controller.mostrarFormularioNuevo(model);

        assertEquals("pedidos/formulario", vista);
        verify(model).addAttribute(eq("pedido"), any(Pedido.class));
        verify(model).addAttribute("tiendas", List.of(tienda));
        verify(tiendaService).listarTiendas();
    }

    @Test
    void guardarPedido_debeRedirigirAPedidos() {
        PedidoService pedidoService = mock(PedidoService.class);
        TiendaService tiendaService = mock(TiendaService.class);

        Pedido pedido = new Pedido();

        Tienda tienda = new Tienda();
        tienda.setNombre("Tienda Puno");

        when(tiendaService.obtenerTiendaPorId(1L)).thenReturn(tienda);
        when(pedidoService.guardarPedido(pedido)).thenReturn(pedido);

        PedidoController controller = new PedidoController(pedidoService, tiendaService);

        String vista = controller.guardarPedido(pedido, 1L);

        assertEquals("redirect:/pedidos", vista);
        assertEquals(tienda, pedido.getTienda());
        verify(tiendaService).obtenerTiendaPorId(1L);
        verify(pedidoService).guardarPedido(pedido);
    }

    @Test
    void mostrarFormularioEditar_debeRetornarFormulario() {
        PedidoService pedidoService = mock(PedidoService.class);
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        Pedido pedido = new Pedido();
        pedido.setEstado("ENTREGADO");

        Tienda tienda = new Tienda();
        tienda.setNombre("Tienda Juliaca");

        when(pedidoService.obtenerPedidoPorId(1L)).thenReturn(pedido);
        when(tiendaService.listarTiendas()).thenReturn(List.of(tienda));

        PedidoController controller = new PedidoController(pedidoService, tiendaService);

        String vista = controller.mostrarFormularioEditar(1L, model);

        assertEquals("pedidos/formulario", vista);
        verify(model).addAttribute("pedido", pedido);
        verify(model).addAttribute("tiendas", List.of(tienda));
        verify(pedidoService).obtenerPedidoPorId(1L);
        verify(tiendaService).listarTiendas();
    }

    @Test
    void eliminarPedido_debeRedirigirAPedidos() {
        PedidoService pedidoService = mock(PedidoService.class);
        TiendaService tiendaService = mock(TiendaService.class);

        PedidoController controller = new PedidoController(pedidoService, tiendaService);

        String vista = controller.eliminarPedido(1L);

        assertEquals("redirect:/pedidos", vista);
        verify(pedidoService).eliminarPedido(1L);
    }
}

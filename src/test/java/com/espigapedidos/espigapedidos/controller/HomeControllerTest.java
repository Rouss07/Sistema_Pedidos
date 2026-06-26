package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    @Test
    void inicio_debeRetornarIndex() {

        ProductoService productoService = mock(ProductoService.class);
        TiendaService tiendaService = mock(TiendaService.class);
        PedidoService pedidoService = mock(PedidoService.class);
        PedidoEspecialService pedidoEspecialService = mock(PedidoEspecialService.class);
        Model model = mock(Model.class);

        when(productoService.contarProductos()).thenReturn(10L);
        when(tiendaService.contarTiendas()).thenReturn(5L);
        when(pedidoService.contarPedidos()).thenReturn(20L);
        when(pedidoEspecialService.contarPedidosEspeciales()).thenReturn(3L);

        HomeController controller = new HomeController(
                productoService,
                tiendaService,
                pedidoService,
                pedidoEspecialService
        );

        String vista = controller.inicio(model);

        assertEquals("index", vista);

        verify(model).addAttribute("totalProductos", 10L);
        verify(model).addAttribute("totalTiendas", 5L);
        verify(model).addAttribute("totalPedidos", 20L);
        verify(model).addAttribute("totalPedidosEspeciales", 3L);
    }
}

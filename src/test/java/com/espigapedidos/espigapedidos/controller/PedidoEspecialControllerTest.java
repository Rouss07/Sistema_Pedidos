package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.PedidoEspecial;
import com.espigapedidos.espigapedidos.service.PedidoEspecialService;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PedidoEspecialControllerTest {

    @Test
    void listarPedidosEspeciales() {

        PedidoEspecialService pedidoEspecialService = mock(PedidoEspecialService.class);
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        PedidoEspecial pedido = new PedidoEspecial();

        when(pedidoEspecialService.listarPedidosEspeciales())
                .thenReturn(List.of(pedido));

        PedidoEspecialController controller =
                new PedidoEspecialController(pedidoEspecialService, tiendaService);

        String vista = controller.listarPedidosEspeciales(model);

        assertEquals("pedidosespeciales/lista", vista);

        verify(model).addAttribute(
                "pedidosEspeciales",
                List.of(pedido)
        );
    }

    @Test
    void mostrarFormularioNuevo() {

        PedidoEspecialService pedidoEspecialService = mock(PedidoEspecialService.class);
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        when(tiendaService.listarTiendas())
                .thenReturn(List.of());

        PedidoEspecialController controller =
                new PedidoEspecialController(pedidoEspecialService, tiendaService);

        String vista = controller.mostrarFormularioNuevo(model);

        assertEquals("pedidosespeciales/formulario", vista);

        verify(model).addAttribute(eq("pedidoEspecial"), any(PedidoEspecial.class));
        verify(model).addAttribute(eq("tiendas"), any());
    }

    @Test
    void eliminarPedidoEspecial() {

        PedidoEspecialService pedidoEspecialService = mock(PedidoEspecialService.class);
        TiendaService tiendaService = mock(TiendaService.class);

        PedidoEspecialController controller =
                new PedidoEspecialController(pedidoEspecialService, tiendaService);

        String vista = controller.eliminarPedidoEspecial(1L);

        assertEquals("redirect:/pedidos-especiales", vista);

        verify(pedidoEspecialService).eliminarPedidoEspecial(1L);
    }

    @Test
    void guardarPedidoEspecialSinImagen() throws Exception {

        PedidoEspecialService pedidoEspecialService = mock(PedidoEspecialService.class);
        TiendaService tiendaService = mock(TiendaService.class);

        PedidoEspecial pedidoEspecial = new PedidoEspecial();

        when(tiendaService.obtenerTiendaPorId(1L))
                .thenReturn(null);

        org.springframework.web.multipart.MultipartFile archivo =
                mock(org.springframework.web.multipart.MultipartFile.class);

        when(archivo.isEmpty()).thenReturn(true);

        PedidoEspecialController controller =
                new PedidoEspecialController(
                        pedidoEspecialService,
                        tiendaService
                );

        String vista = controller.guardarPedidoEspecial(
                pedidoEspecial,
                1L,
                archivo
        );

        assertEquals("redirect:/pedidos-especiales", vista);

        verify(tiendaService).obtenerTiendaPorId(1L);
        verify(pedidoEspecialService).guardarPedidoEspecial(pedidoEspecial);
    }

    @Test
    void guardarPedidoEspecialConImagen() throws Exception {

        PedidoEspecialService pedidoEspecialService = mock(PedidoEspecialService.class);
        TiendaService tiendaService = mock(TiendaService.class);

        PedidoEspecial pedidoEspecial = new PedidoEspecial();

        when(tiendaService.obtenerTiendaPorId(1L))
                .thenReturn(new com.espigapedidos.espigapedidos.entity.Tienda());

        org.springframework.web.multipart.MultipartFile archivo =
                mock(org.springframework.web.multipart.MultipartFile.class);

        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getOriginalFilename()).thenReturn("foto.jpg");

        PedidoEspecialController controller =
                new PedidoEspecialController(
                        pedidoEspecialService,
                        tiendaService
                );

        String vista = controller.guardarPedidoEspecial(
                pedidoEspecial,
                1L,
                archivo
        );

        assertEquals("redirect:/pedidos-especiales", vista);

        verify(archivo).transferTo(any(java.io.File.class));
        verify(pedidoEspecialService).guardarPedidoEspecial(pedidoEspecial);
    }
}
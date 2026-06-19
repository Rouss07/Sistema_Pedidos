package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaControllerTest {

    @Test
    void listarTiendas_debeRetornarVistaLista() {
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        Tienda tienda = new Tienda();
        tienda.setNombre("Tienda Central");

        when(tiendaService.listarTiendas()).thenReturn(List.of(tienda));

        TiendaController controller = new TiendaController(tiendaService);

        String vista = controller.listarTiendas(model);

        assertEquals("tiendas/lista", vista);
        verify(model).addAttribute("tiendas", List.of(tienda));
        verify(tiendaService).listarTiendas();
    }

    @Test
    void mostrarFormularioNuevo_debeRetornarFormulario() {
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        TiendaController controller = new TiendaController(tiendaService);

        String vista = controller.mostrarFormularioNuevo(model);

        assertEquals("tiendas/formulario", vista);
        verify(model).addAttribute(eq("tienda"), any(Tienda.class));
    }

    @Test
    void guardarTienda_debeRedirigirATiendas() {
        TiendaService tiendaService = mock(TiendaService.class);

        Tienda tienda = new Tienda();
        tienda.setNombre("Sucursal Puno");

        when(tiendaService.guardarTienda(tienda)).thenReturn(tienda);

        TiendaController controller = new TiendaController(tiendaService);

        String vista = controller.guardarTienda(tienda);

        assertEquals("redirect:/tiendas", vista);
        verify(tiendaService).guardarTienda(tienda);
    }

    @Test
    void mostrarFormularioEditar_debeRetornarFormulario() {
        TiendaService tiendaService = mock(TiendaService.class);
        Model model = mock(Model.class);

        Tienda tienda = new Tienda();
        tienda.setNombre("Sucursal Juliaca");

        when(tiendaService.obtenerTiendaPorId(1L)).thenReturn(tienda);

        TiendaController controller = new TiendaController(tiendaService);

        String vista = controller.mostrarFormularioEditar(1L, model);

        assertEquals("tiendas/formulario", vista);
        verify(model).addAttribute("tienda", tienda);
        verify(tiendaService).obtenerTiendaPorId(1L);
    }

    @Test
    void eliminarTienda_debeRedirigirATiendas() {
        TiendaService tiendaService = mock(TiendaService.class);

        TiendaController controller = new TiendaController(tiendaService);

        String vista = controller.eliminarTienda(1L);

        assertEquals("redirect:/tiendas", vista);
        verify(tiendaService).eliminarTienda(1L);
    }
}

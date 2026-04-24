package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Usuario;
import com.espigapedidos.espigapedidos.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    @Test
    void listarUsuarios_debeRetornarVistaLista() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        Model model = mock(Model.class);

        Usuario u = new Usuario();
        u.setUsername("ross");

        when(usuarioService.listarUsuarios()).thenReturn(List.of(u));

        UsuarioController controller = new UsuarioController(usuarioService);

        String vista = controller.listarUsuarios(model);

        assertEquals("usuarios/lista", vista);
        verify(model).addAttribute("usuarios", List.of(u));
        verify(usuarioService).listarUsuarios();
    }

    @Test
    void mostrarFormularioNuevo_debeRetornarFormulario() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        Model model = mock(Model.class);

        UsuarioController controller = new UsuarioController(usuarioService);

        String vista = controller.mostrarFormularioNuevo(model);

        assertEquals("usuarios/formulario", vista);
        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
    }

    @Test
    void guardarUsuario_debeRedirigirAUsuarios() {
        UsuarioService usuarioService = mock(UsuarioService.class);

        Usuario u = new Usuario();
        u.setUsername("ross");

        when(usuarioService.guardarUsuario(u)).thenReturn(u);

        UsuarioController controller = new UsuarioController(usuarioService);

        String vista = controller.guardarUsuario(u);

        assertEquals("redirect:/usuarios", vista);
        verify(usuarioService).guardarUsuario(u);
    }

    @Test
    void mostrarFormularioEditar_debeRetornarFormulario() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        Model model = mock(Model.class);

        Usuario u = new Usuario();
        u.setUsername("rossy");

        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(u);

        UsuarioController controller = new UsuarioController(usuarioService);

        String vista = controller.mostrarFormularioEditar(1L, model);

        assertEquals("usuarios/formulario", vista);
        verify(model).addAttribute("usuario", u);
        verify(usuarioService).obtenerUsuarioPorId(1L);
    }

    @Test
    void eliminarUsuario_debeRedirigirAUsuarios() {
        UsuarioService usuarioService = mock(UsuarioService.class);

        UsuarioController controller = new UsuarioController(usuarioService);

        String vista = controller.eliminarUsuario(1L);

        assertEquals("redirect:/usuarios", vista);
        verify(usuarioService).eliminarUsuario(1L);
    }
}

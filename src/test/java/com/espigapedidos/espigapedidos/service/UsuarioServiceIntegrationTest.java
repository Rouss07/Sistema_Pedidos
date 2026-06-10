package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    private Usuario crearUsuario(String username) {
        Usuario u = new Usuario();
        u.setNombre("Usuario Test");
        u.setUsername(username);
        u.setPassword("password123");
        u.setRol("ADMIN");
        u.setActivo(true);
        return u;
    }

    @Test
    void guardarYBuscarUsuario() {
        Usuario guardado = usuarioService.guardarUsuario(crearUsuario("testuser1"));
        Usuario encontrado = usuarioService.obtenerUsuarioPorId(guardado.getId());

        assertNotNull(encontrado);
        assertEquals("testuser1", encontrado.getUsername());
    }

    @Test
    void guardarUsuario_passwordDebeEstarEncriptada() {
        Usuario usuario = crearUsuario("testuser2");
        Usuario guardado = usuarioService.guardarUsuario(usuario);

        assertNotEquals("password123", guardado.getPassword());
        assertTrue(guardado.getPassword().startsWith("$2"));
    }

    @Test
    void listarUsuarios_incluyeUsuarioGuardado() {
        usuarioService.guardarUsuario(crearUsuario("testuser3"));
        List<Usuario> lista = usuarioService.listarUsuarios();
        assertFalse(lista.isEmpty());
    }

    @Test
    void loadUserByUsername_devuelveUserDetailsCorrectos() {
        usuarioService.guardarUsuario(crearUsuario("adminuser"));

        UserDetails details = usuarioService.loadUserByUsername("adminuser");

        assertNotNull(details);
        assertEquals("adminuser", details.getUsername());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_usuarioInexistente_lanzaExcepcion() {
        assertThrows(UsernameNotFoundException.class, () ->
                usuarioService.loadUserByUsername("noeexiste"));
    }

    @Test
    void loadUserByUsername_usuarioInactivo_lanzaExcepcion() {
        Usuario usuario = crearUsuario("inactivo_user");
        usuario.setActivo(false);
        usuarioService.guardarUsuario(usuario);

        assertThrows(UsernameNotFoundException.class, () ->
                usuarioService.loadUserByUsername("inactivo_user"));
    }

    @Test
    void eliminarUsuario_noDebeEncontrarsePostEliminacion() {
        Usuario guardado = usuarioService.guardarUsuario(crearUsuario("todelete"));
        Long id = guardado.getId();

        usuarioService.eliminarUsuario(id);

        assertNull(usuarioService.obtenerUsuarioPorId(id));
    }

    @Test
    void guardarUsuario_activoPorDefecto_siEsNull() {
        Usuario usuario = crearUsuario("autoactivo");
        usuario.setActivo(null);
        Usuario guardado = usuarioService.guardarUsuario(usuario);

        assertTrue(guardado.getActivo());
    }
}
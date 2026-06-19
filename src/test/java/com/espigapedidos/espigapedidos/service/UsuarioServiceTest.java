package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Usuario;
import com.espigapedidos.espigapedidos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Test
    void listarUsuarios() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.findAll()).thenReturn(List.of(new Usuario()));

        UsuarioService service = new UsuarioService(repo, encoder);

        assertEquals(1, service.listarUsuarios().size());
    }

    @Test
    void guardarUsuario() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        Usuario usuario = new Usuario();
        usuario.setPassword("123");

        when(encoder.encode("123")).thenReturn("abc123");
        when(repo.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioService service = new UsuarioService(repo, encoder);

        service.guardarUsuario(usuario);

        verify(encoder).encode("123");
        verify(repo).save(usuario);
    }

    @Test
    void obtenerUsuarioPorId() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        Usuario usuario = new Usuario();

        when(repo.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioService service = new UsuarioService(repo, encoder);

        assertEquals(usuario, service.obtenerUsuarioPorId(1L));
    }

    @Test
    void eliminarUsuario() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        UsuarioService service = new UsuarioService(repo, encoder);

        service.eliminarUsuario(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void loadUserByUsername_usuarioActivo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        Usuario usuario = new Usuario();
        usuario.setUsername("ross");
        usuario.setPassword("123");
        usuario.setRol("ADMIN");
        usuario.setActivo(true);

        when(repo.findByUsername("ross"))
                .thenReturn(Optional.of(usuario));

        UsuarioService service = new UsuarioService(repo, encoder);

        UserDetails details = service.loadUserByUsername("ross");

        assertEquals("ross", details.getUsername());
    }

    @Test
    void loadUserByUsername_usuarioNoExiste() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.findByUsername("ross"))
                .thenReturn(Optional.empty());

        UsuarioService service = new UsuarioService(repo, encoder);

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("ross")
        );
    }

    @Test
    void loadUserByUsername_usuarioInactivo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        Usuario usuario = new Usuario();
        usuario.setUsername("ross");
        usuario.setPassword("123");
        usuario.setRol("ADMIN");
        usuario.setActivo(false);

        when(repo.findByUsername("ross"))
                .thenReturn(Optional.of(usuario));

        UsuarioService service = new UsuarioService(repo, encoder);

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("ross")
        );
    }
}

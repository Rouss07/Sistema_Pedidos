package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Usuario;
import com.espigapedidos.espigapedidos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SetupControllerTest {

    @Test
    void crearAdmin_cuandoExiste() {

        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.findByUsername("admin"))
                .thenReturn(Optional.of(new Usuario()));

        SetupController controller = new SetupController(repo, encoder);

        String resultado = controller.crearAdmin();

        assertEquals("El usuario admin ya existe", resultado);
    }

    @Test
    void crearAdmin_cuandoNoExiste() {

        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(encoder.encode("1234"))
                .thenReturn("1234codificada");

        SetupController controller = new SetupController(repo, encoder);

        String resultado = controller.crearAdmin();

        assertEquals("Usuario admin creado correctamente", resultado);

        verify(repo).save(any(Usuario.class));
    }

    @Test
    void crearTienda_cuandoExiste() {

        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.findByUsername("tienda"))
                .thenReturn(Optional.of(new Usuario()));

        SetupController controller = new SetupController(repo, encoder);

        String resultado = controller.crearTienda();

        assertEquals("Usuario tienda ya existe", resultado);
    }

    @Test
    void crearTienda_cuandoNoExiste() {

        UsuarioRepository repo = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(repo.findByUsername("tienda"))
                .thenReturn(Optional.empty());

        when(encoder.encode("1234"))
                .thenReturn("1234codificada");

        SetupController controller = new SetupController(repo, encoder);

        String resultado = controller.crearTienda();

        assertEquals("Usuario tienda creado", resultado);

        verify(repo).save(any(Usuario.class));
    }
}

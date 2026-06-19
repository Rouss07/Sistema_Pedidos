package com.espigapedidos.espigapedidos.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void validarDatosUsuario() {
        Usuario usuario = new Usuario();

        usuario.setUsername("ross");
        usuario.setPassword("123456");

        assertEquals("ross", usuario.getUsername());
        assertEquals("123456", usuario.getPassword());
    }
}

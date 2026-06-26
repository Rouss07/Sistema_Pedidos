package com.espigapedidos.espigapedidos.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginControllerTest {

    @Test
    void login_debeRetornarVistaLogin() {

        LoginController controller = new LoginController();

        String vista = controller.login();

        assertEquals("login", vista);
    }
}

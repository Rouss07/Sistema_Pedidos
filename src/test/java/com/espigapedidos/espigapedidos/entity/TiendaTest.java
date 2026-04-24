package com.espigapedidos.espigapedidos.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TiendaTest {

    @Test
    void validarDatosTienda() {
        Tienda tienda = new Tienda();

        tienda.setNombre("Tienda Juliaca");
        tienda.setDireccion("Jr. Lima 123");

        assertEquals("Tienda Juliaca", tienda.getNombre());
        assertEquals("Jr. Lima 123", tienda.getDireccion());
    }
}

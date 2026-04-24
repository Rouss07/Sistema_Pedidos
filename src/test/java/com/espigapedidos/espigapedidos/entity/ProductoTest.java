package com.espigapedidos.espigapedidos.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void validarDatosProducto() {
        Producto producto = new Producto();

        producto.setNombre("Pan francés");
        producto.setStock(50);

        assertEquals("Pan francés", producto.getNombre());
        assertEquals(50, producto.getStock());
    }
}

package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductoServiceIntegrationTest {

    @Autowired
    private ProductoService productoService;

    @Test
    void guardarYBuscarProducto() {

        Producto producto = new Producto();
        producto.setNombre("Torta Fresa");
        producto.setCategoria("Tortas");
        producto.setPrecio(40.0);
        producto.setStock(5);

        Producto guardado = productoService.guardarProducto(producto);

        Producto encontrado =
                productoService.obtenerProductoPorId(guardado.getId());

        assertNotNull(encontrado);
        assertEquals("Torta Fresa", encontrado.getNombre());
    }
}

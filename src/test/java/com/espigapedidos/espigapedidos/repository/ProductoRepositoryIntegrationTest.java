package com.espigapedidos.espigapedidos.repository;

import com.espigapedidos.espigapedidos.entity.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductoRepositoryIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void guardarProducto() {

        Producto producto = new Producto();
        producto.setNombre("Torta Chocolate");
        producto.setCategoria("Tortas");
        producto.setPrecio(35.0);
        producto.setStock(10);

        Producto guardado = productoRepository.save(producto);

        assertNotNull(guardado.getId());
        assertEquals("Torta Chocolate", guardado.getNombre());
        assertEquals("Tortas", guardado.getCategoria());
    }
}

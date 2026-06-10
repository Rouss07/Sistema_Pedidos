package com.espigapedidos.espigapedidos.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IntegracionBasicaTest {

    @Test
    void contextoCarga() {
        assertDoesNotThrow(() -> {});
    }
}
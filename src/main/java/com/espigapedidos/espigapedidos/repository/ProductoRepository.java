package com.espigapedidos.espigapedidos.repository;

import com.espigapedidos.espigapedidos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

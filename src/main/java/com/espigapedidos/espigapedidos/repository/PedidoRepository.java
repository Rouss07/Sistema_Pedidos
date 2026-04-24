package com.espigapedidos.espigapedidos.repository;

import com.espigapedidos.espigapedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}

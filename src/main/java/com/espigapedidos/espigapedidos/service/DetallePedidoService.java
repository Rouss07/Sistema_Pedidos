package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.DetallePedido;
import com.espigapedidos.espigapedidos.repository.DetallePedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    public DetallePedidoService(DetallePedidoRepository detallePedidoRepository) {
        this.detallePedidoRepository = detallePedidoRepository;
    }

    public List<DetallePedido> listarPorPedido(Long pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    public DetallePedido guardarDetalle(DetallePedido detallePedido) {
        return detallePedidoRepository.save(detallePedido);
    }

    public void eliminarDetalle(Long id) {
        detallePedidoRepository.deleteById(id);
    }
}

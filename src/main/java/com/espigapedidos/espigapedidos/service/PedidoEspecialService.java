package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.PedidoEspecial;
import com.espigapedidos.espigapedidos.repository.PedidoEspecialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoEspecialService {

    private final PedidoEspecialRepository pedidoEspecialRepository;

    public PedidoEspecialService(PedidoEspecialRepository pedidoEspecialRepository) {
        this.pedidoEspecialRepository = pedidoEspecialRepository;
    }

    public List<PedidoEspecial> listarPedidosEspeciales() {
        return pedidoEspecialRepository.findAll();
    }

    public PedidoEspecial guardarPedidoEspecial(PedidoEspecial pedidoEspecial) {
        return pedidoEspecialRepository.save(pedidoEspecial);
    }

    public PedidoEspecial obtenerPedidoEspecialPorId(Long id) {
        return pedidoEspecialRepository.findById(id).orElse(null);
    }

    public void eliminarPedidoEspecial(Long id) {
        pedidoEspecialRepository.deleteById(id);
    }

    public long contarPedidosEspeciales() {
        return pedidoEspecialRepository.count();
    }
}

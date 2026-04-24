package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.repository.TiendaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TiendaService {

    private final TiendaRepository tiendaRepository;

    public TiendaService(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    public List<Tienda> listarTiendas() {
        return tiendaRepository.findAll();
    }

    public Tienda guardarTienda(Tienda tienda) {
        return tiendaRepository.save(tienda);
    }

    public Tienda obtenerTiendaPorId(Long id) {
        return tiendaRepository.findById(id).orElse(null);
    }

    public void eliminarTienda(Long id) {
        tiendaRepository.deleteById(id);
    }

    public long contarTiendas() {
        return tiendaRepository.count();
    }
}

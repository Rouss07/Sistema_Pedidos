package com.espigapedidos.espigapedidos.repository;

import com.espigapedidos.espigapedidos.entity.SolicitudReset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudResetRepository extends JpaRepository<SolicitudReset, Long> {
    List<SolicitudReset> findByAtendidaFalse();
}

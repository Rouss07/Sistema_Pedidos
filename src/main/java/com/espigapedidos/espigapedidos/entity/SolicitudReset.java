package com.espigapedidos.espigapedidos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_reset")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String mensaje;

    private LocalDateTime fecha;

    private Boolean atendida = false;
}
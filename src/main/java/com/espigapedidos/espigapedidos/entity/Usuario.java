package com.espigapedidos.espigapedidos.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String rol;

    private Boolean activo;

    @Column(name = "eliminado", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean eliminado = false;
}

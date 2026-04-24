package com.espigapedidos.espigapedidos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pedido_especial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;

    private String telefono;

    private String descripcion;

    private String sabor;

    private String tamano;

    private LocalDate fechaEntrega;

    private String estado;

    private String imagen;

    @ManyToOne
    @JoinColumn(name = "tienda_id")
    private Tienda tienda;
}

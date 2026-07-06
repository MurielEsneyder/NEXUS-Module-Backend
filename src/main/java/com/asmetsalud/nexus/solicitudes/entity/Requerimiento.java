package com.asmetsalud.nexus.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sd_requerimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Requerimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(name = "numero_orden", nullable = false)
    private Integer numeroOrden;

    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "tipo_requerimiento", nullable = false)
    private Short tipoRequerimiento; // 0 = Funcional, 1 = No Funcional

    @Column(name = "objetivo", nullable = false, length = 200)
    private String objetivo;

    @Column(name = "detalle", nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "cargo_impactado", length = 100)
    private String cargoImpactado;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id")
    private EstadoSolicitud estado;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaIngreso == null) {
            fechaIngreso = LocalDate.now();
        }
        if (estado == null) {
            // Estado por defecto: Borrador (ID 1)
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
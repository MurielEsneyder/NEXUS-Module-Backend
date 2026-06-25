package com.asmetsalud.nexus.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sd_solicitud")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "empleado_documento", nullable = false, length = 20)
    private String empleadoDocumento;

    @Column(name = "empleado_nombre", nullable = false, length = 200)
    private String empleadoNombre;

    @Column(name = "empleado_correo", nullable = false, length = 150)
    private String empleadoCorreo;

    @Column(name = "empleado_cargo", nullable = false, length = 150)
    private String empleadoCargo;

    @Column(name = "empleado_sede", nullable = false, length = 100)
    private String empleadoSede;

    @Column(name = "solicitud_proceso", nullable = false, length = 500)
    private String solicitudProceso;

    @Column(name = "proceso_id", nullable = false)
    private Long procesoId;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(name = "macroproceso_id", nullable = false)
    private Long macroprocesoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_solicitud_id", nullable = false)
    private TipoSolicitud tipoSolicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoSolicitud estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "impacto", nullable = false, columnDefinition = "TEXT")
    private String impacto;

    @Column(name = "pdf_nombre")
    private String pdfNombre;

    @Column(name = "pdf_contenido", columnDefinition = "BYTEA")
    private byte[] pdfContenido;

    @Column(name = "usuario_registro", nullable = false, length = 100)
    private String usuarioRegistro;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Requerimiento> requerimientos = new ArrayList<>();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Auditoria> auditorias = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
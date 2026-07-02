package com.asmetsalud.nexus.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponseDTO {
    private Long id;
    private String codigo;
    private LocalDate fechaCreacion;
    private String empleadoDocumento;
    private String empleadoNombre;
    private String empleadoCorreo;
    private String empleadoCargo;
    private String empleadoSede;
    private String solicitudProceso;
    private Long procesoId;
    private Long areaId;
    private Long macroprocesoId;
    private TipoSolicitudDTO tipoSolicitud;
    private EstadoSolicitudDTO estado;
    private String observaciones;
    private String impacto;
    private String pdfNombre;
    private String usuarioRegistro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalRequerimientos;
    private Integer requerimientosFuncionales;
    private Integer requerimientosNoFuncionales;

    // ============================================================
    // NUEVO: Lista de requerimientos para el PDF
    // ============================================================
    private List<RequerimientoResponseDTO> requerimientos;
}
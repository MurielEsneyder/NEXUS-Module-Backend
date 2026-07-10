package com.asmetsalud.nexus.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDTO {
    private Long id;
    private Long solicitudId;
    private String codigoSolicitud;
    private EstadoSolicitudDTO estadoAnterior;
    private EstadoSolicitudDTO estadoNuevo;
    private String observacion;
    private Integer fase;
    private String usuarioRegistro;
    private LocalDateTime createdAt;
}

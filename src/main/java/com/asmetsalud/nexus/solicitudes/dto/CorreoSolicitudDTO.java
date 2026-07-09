package com.asmetsalud.nexus.solicitudes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorreoSolicitudDTO {
    private Long solicitudId;
    private String numeroSolicitud;
    private String correoDestinatario;
    private String nombreSolicitante;
    private String modalidad;
    private String pdfBase64;
}

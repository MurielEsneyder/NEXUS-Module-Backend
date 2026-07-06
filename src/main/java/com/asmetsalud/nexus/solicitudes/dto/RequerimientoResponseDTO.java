package com.asmetsalud.nexus.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequerimientoResponseDTO {
    private Long id;
    private String codigo;
    private Short tipoRequerimiento;
    private String tipoRequerimientoNombre;
    private String objetivo;
    private String detalle;
    private String cargoImpactado;
    private Integer numeroOrden;
    private String estadoNombre;
}
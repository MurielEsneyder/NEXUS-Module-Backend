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
    private Short tipoRequerimiento; // 0 = Funcional, 1 = No Funcional
    private String objetivo;
    private String detalle;
    private String cargoImpactado;
}
package com.asmetsalud.nexus.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequerimientoRequestDTO {

    @NotNull(message = "El tipo de requerimiento es obligatorio (0=Funcional, 1=No Funcional)")
    private Short tipoRequerimiento;

    @NotBlank(message = "El objetivo del requerimiento es obligatorio")
    private String objetivo;

    @NotBlank(message = "El detalle del requerimiento es obligatorio")
    private String detalle;

    // ============================================================
    // NUEVOS CAMPOS
    // ============================================================
    private String cargoImpactado;
    private List<Long> cargosImpactadosIds;
}
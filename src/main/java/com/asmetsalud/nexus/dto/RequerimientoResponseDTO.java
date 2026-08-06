package com.asmetsalud.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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
    private List<ImagenDTO> imagenesUrls;
}

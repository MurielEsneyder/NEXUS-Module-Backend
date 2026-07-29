package com.asmetsalud.nexus.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MacroprocesoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private Boolean activo;
}

package com.asmetsalud.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoSolicitudDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private Boolean activo;
}

package com.asmetsalud.nexus.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorDTO {
    private String nombreCompleto;
    private String email;
    private String cargo;
    private String sede;
    private String documento;
}
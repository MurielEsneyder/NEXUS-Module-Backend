package com.asmetsalud.nexus.solicitudes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequestDTO {

    @NotBlank(message = "El documento del empleado es obligatorio")
    private String empleadoDocumento;

    @NotBlank(message = "El nombre del empleado es obligatorio")
    private String empleadoNombre;

    @NotBlank(message = "El correo del empleado es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String empleadoCorreo;

    @NotBlank(message = "El cargo del empleado es obligatorio")
    private String empleadoCargo;

    @NotBlank(message = "La sede del empleado es obligatoria")
    private String empleadoSede;

    @NotBlank(message = "El proceso de la solicitud es obligatorio")
    private String solicitudProceso;

    @NotNull(message = "El ID del proceso es obligatorio")
    private Long procesoId;

    @NotNull(message = "El ID del área es obligatorio")
    private Long areaId;

    @NotNull(message = "El ID del macroproceso es obligatorio")
    private Long macroprocesoId;

    @NotNull(message = "El ID del tipo de solicitud es obligatorio")
    private Long tipoSolicitudId;

    @NotNull(message = "El ID del estado es obligatorio")
    private Long estadoId;

    private String prioridad;

    private String observaciones;

    @NotBlank(message = "El impacto es obligatorio")
    private String impacto;

    private List<RequerimientoRequestDTO> requerimientos;
}
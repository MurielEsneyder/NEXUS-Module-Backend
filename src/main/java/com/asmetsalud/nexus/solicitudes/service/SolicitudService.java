package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.SolicitudRequestDTO;
import com.asmetsalud.nexus.solicitudes.dto.SolicitudResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SolicitudService {

    // ============================================================
    // CRUD
    // ============================================================

    SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request);

    SolicitudResponseDTO actualizarSolicitud(Long id, SolicitudRequestDTO request);

    void eliminarSolicitud(Long id);

    SolicitudResponseDTO obtenerSolicitudPorId(Long id);

    SolicitudResponseDTO obtenerSolicitudPorCodigo(String codigo);

    Page<SolicitudResponseDTO> obtenerTodasLasSolicitudes(Pageable pageable);

    List<SolicitudResponseDTO> obtenerSolicitudesPorEmpleado(String documento);

    List<SolicitudResponseDTO> obtenerSolicitudesPorEstado(Long estadoId);

    List<SolicitudResponseDTO> obtenerSolicitudesPorRangoFechas(
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    SolicitudResponseDTO cambiarEstadoSolicitud(
            Long id,
            Long nuevoEstadoId,
            String observacion
    );

    Long contarSolicitudesPorEstado(Long estadoId);

    // ============================================================
    // PDF
    // ============================================================

    byte[] generarPDF(Long id);

}
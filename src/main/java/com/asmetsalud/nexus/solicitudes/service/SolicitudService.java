package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.SolicitudRequestDTO;
import com.asmetsalud.nexus.solicitudes.dto.SolicitudResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SolicitudService {

    // CREATE
    SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request);

    // READ
    Page<SolicitudResponseDTO> obtenerTodasLasSolicitudes(Pageable pageable);
    SolicitudResponseDTO obtenerSolicitudPorId(Long id);
    SolicitudResponseDTO obtenerSolicitudPorCodigo(String codigo);
    List<SolicitudResponseDTO> obtenerSolicitudesPorEmpleado(String documento);
    List<SolicitudResponseDTO> obtenerSolicitudesPorEstado(Long estadoId);

    // UPDATE
    SolicitudResponseDTO actualizarSolicitud(Long id, SolicitudRequestDTO request);
    SolicitudResponseDTO cambiarEstadoSolicitud(Long id, Long nuevoEstadoId, String observacion);
    SolicitudResponseDTO actualizarPrioridad(Long id, String prioridad);

    // DELETE
    void eliminarSolicitud(Long id);

    // UTILS
    Long contarSolicitudesPorEstado(Long estadoId);
    byte[] generarPDF(Long id);
}
package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.*;
import com.asmetsalud.nexus.solicitudes.entity.*;
import com.asmetsalud.nexus.solicitudes.exception.ResourceNotFoundException;
import com.asmetsalud.nexus.solicitudes.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final RequerimientoRepository requerimientoRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final AuditoriaRepository auditoriaRepository;

    // ============================================================
    // MÉTODOS CRUD PRINCIPALES
    // ============================================================

    @Override
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        log.info("Creando nueva solicitud para empleado: {}", request.getEmpleadoDocumento());

        // Validar y obtener entidades relacionadas
        TipoSolicitud tipoSolicitud = tipoSolicitudRepository.findById(request.getTipoSolicitudId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado con ID: " + request.getTipoSolicitudId()));

        EstadoSolicitud estado = estadoSolicitudRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

        // Crear la solicitud
        Solicitud solicitud = new Solicitud();
        solicitud.setCodigo(generarCodigoSolicitud());
        solicitud.setFechaCreacion(LocalDate.now());
        solicitud.setEmpleadoDocumento(request.getEmpleadoDocumento());
        solicitud.setEmpleadoNombre(request.getEmpleadoNombre());
        solicitud.setEmpleadoCorreo(request.getEmpleadoCorreo());
        solicitud.setEmpleadoCargo(request.getEmpleadoCargo());
        solicitud.setEmpleadoSede(request.getEmpleadoSede());
        solicitud.setSolicitudProceso(request.getSolicitudProceso());
        solicitud.setProcesoId(request.getProcesoId());
        solicitud.setAreaId(request.getAreaId());
        solicitud.setMacroprocesoId(request.getMacroprocesoId());
        solicitud.setTipoSolicitud(tipoSolicitud);
        solicitud.setEstado(estado);
        solicitud.setObservaciones(request.getObservaciones());
        solicitud.setImpacto(request.getImpacto());
        solicitud.setUsuarioRegistro(request.getEmpleadoNombre().toLowerCase().replace(" ", "."));

        // Guardar la solicitud
        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con ID: {} y código: {}", solicitudGuardada.getId(), solicitudGuardada.getCodigo());

        // Crear requerimientos si existen
        if (request.getRequerimientos() != null && !request.getRequerimientos().isEmpty()) {
            log.info("📋 Creando {} requerimientos", request.getRequerimientos().size());
            crearRequerimientos(solicitudGuardada, request.getRequerimientos());
        } else {
            log.warn("⚠️ No se recibieron requerimientos");
        }

        // Crear auditoría inicial
        crearAuditoria(solicitudGuardada, null, estado, "Solicitud creada", 1);

        return convertirADTO(solicitudGuardada);
    }

    @Override
    public SolicitudResponseDTO actualizarSolicitud(Long id, SolicitudRequestDTO request) {
        log.info("Actualizando solicitud con ID: {}", id);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));

        // Actualizar campos básicos
        solicitud.setEmpleadoDocumento(request.getEmpleadoDocumento());
        solicitud.setEmpleadoNombre(request.getEmpleadoNombre());
        solicitud.setEmpleadoCorreo(request.getEmpleadoCorreo());
        solicitud.setEmpleadoCargo(request.getEmpleadoCargo());
        solicitud.setEmpleadoSede(request.getEmpleadoSede());
        solicitud.setSolicitudProceso(request.getSolicitudProceso());
        solicitud.setProcesoId(request.getProcesoId());
        solicitud.setAreaId(request.getAreaId());
        solicitud.setMacroprocesoId(request.getMacroprocesoId());
        solicitud.setObservaciones(request.getObservaciones());
        solicitud.setImpacto(request.getImpacto());

        // Actualizar tipo de solicitud si se proporciona
        if (request.getTipoSolicitudId() != null) {
            TipoSolicitud tipoSolicitud = tipoSolicitudRepository.findById(request.getTipoSolicitudId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado con ID: " + request.getTipoSolicitudId()));
            solicitud.setTipoSolicitud(tipoSolicitud);
        }

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);
        log.info("Solicitud actualizada con ID: {}", solicitudActualizada.getId());

        return convertirADTO(solicitudActualizada);
    }

    @Override
    public void eliminarSolicitud(Long id) {
        log.info("Eliminando solicitud con ID: {}", id);

        if (!solicitudRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solicitud no encontrada con ID: " + id);
        }

        solicitudRepository.deleteById(id);
        log.info("Solicitud eliminada con ID: {}", id);
    }

    @Override
    public SolicitudResponseDTO obtenerSolicitudPorId(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));
        return convertirADTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO obtenerSolicitudPorCodigo(String codigo) {
        Solicitud solicitud = solicitudRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con código: " + codigo));
        return convertirADTO(solicitud);
    }

    // ============================================================
    // MÉTODO MODIFICADO CON JOIN FETCH
    // ============================================================
    @Override
    public Page<SolicitudResponseDTO> obtenerTodasLasSolicitudes(Pageable pageable) {
        log.info("📋 Obteniendo todas las solicitudes con sus requerimientos");
        // Usar el método con JOIN FETCH para cargar los requerimientos
        Page<Solicitud> solicitudes = solicitudRepository.findAllWithRequerimientos(pageable);
        return solicitudes.map(this::convertirADTO);
    }

    @Override
    public List<SolicitudResponseDTO> obtenerSolicitudesPorEmpleado(String documento) {
        return solicitudRepository.findByEmpleadoDocumento(documento)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> obtenerSolicitudesPorEstado(Long estadoId) {
        return solicitudRepository.findByEstadoId(estadoId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> obtenerSolicitudesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return solicitudRepository.findByFechaCreacionBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public SolicitudResponseDTO cambiarEstadoSolicitud(Long id, Long nuevoEstadoId, String observacion) {
        log.info("Cambiando estado de solicitud ID: {} a estado ID: {}", id, nuevoEstadoId);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        EstadoSolicitud nuevoEstado = estadoSolicitudRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + nuevoEstadoId));

        // Actualizar estado
        solicitud.setEstado(nuevoEstado);
        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        // Registrar en auditoría
        crearAuditoria(solicitudActualizada, estadoAnterior, nuevoEstado, observacion, nuevoEstado.getFase());

        log.info("Estado cambiado exitosamente para solicitud ID: {}", id);
        return convertirADTO(solicitudActualizada);
    }

    @Override
    public Long contarSolicitudesPorEstado(Long estadoId) {
        return solicitudRepository.countByEstadoId(estadoId);
    }

    // ============================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================================

    /**
     * Genera un código de solicitud secuencial con formato SD_XXX
     */
    private String generarCodigoSolicitud() {
        Long count = solicitudRepository.count();
        return String.format("SD_%03d", count + 1);
    }

    /**
     * Crea los requerimientos asociados a una solicitud
     */
    private void crearRequerimientos(Solicitud solicitud, List<RequerimientoRequestDTO> requerimientosDTO) {
        for (RequerimientoRequestDTO reqDTO : requerimientosDTO) {
            Requerimiento requerimiento = new Requerimiento();
            requerimiento.setSolicitud(solicitud);
            requerimiento.setTipoRequerimiento(reqDTO.getTipoRequerimiento());
            requerimiento.setObjetivo(reqDTO.getObjetivo());
            requerimiento.setDetalle(reqDTO.getDetalle());
            requerimiento.setUsuarioRegistro(solicitud.getUsuarioRegistro());

            // Calcular número de orden y código
            Integer maxOrden = requerimientoRepository.findMaxNumeroOrdenBySolicitudIdAndTipo(
                    solicitud.getId(), reqDTO.getTipoRequerimiento());
            int nuevoOrden = (maxOrden != null) ? maxOrden + 1 : 1;
            requerimiento.setNumeroOrden(nuevoOrden);

            String prefijo = reqDTO.getTipoRequerimiento() == 0 ? "RF" : "RNF";
            requerimiento.setCodigo(String.format("%s_%02d", prefijo, nuevoOrden));

            requerimientoRepository.save(requerimiento);
        }
    }

    /**
     * Crea un registro de auditoría para tracking de cambios
     */
    private void crearAuditoria(Solicitud solicitud, EstadoSolicitud estadoAnterior,
                                EstadoSolicitud estadoNuevo, String observacion, Integer fase) {
        Auditoria auditoria = new Auditoria();
        auditoria.setSolicitud(solicitud);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setObservacion(observacion);
        auditoria.setFase(fase);
        auditoria.setUsuarioRegistro(solicitud.getUsuarioRegistro());

        auditoriaRepository.save(auditoria);
    }

    /**
     * Convierte una entidad Solicitud a SolicitudResponseDTO
     * AHORA CON CONTEO DE REQUERIMIENTOS USANDO EL REPOSITORIO
     */
    private SolicitudResponseDTO convertirADTO(Solicitud solicitud) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setId(solicitud.getId());
        dto.setCodigo(solicitud.getCodigo());
        dto.setFechaCreacion(solicitud.getFechaCreacion());
        dto.setEmpleadoDocumento(solicitud.getEmpleadoDocumento());
        dto.setEmpleadoNombre(solicitud.getEmpleadoNombre());
        dto.setEmpleadoCorreo(solicitud.getEmpleadoCorreo());
        dto.setEmpleadoCargo(solicitud.getEmpleadoCargo());
        dto.setEmpleadoSede(solicitud.getEmpleadoSede());
        dto.setSolicitudProceso(solicitud.getSolicitudProceso());
        dto.setProcesoId(solicitud.getProcesoId());
        dto.setAreaId(solicitud.getAreaId());
        dto.setMacroprocesoId(solicitud.getMacroprocesoId());
        dto.setObservaciones(solicitud.getObservaciones());
        dto.setImpacto(solicitud.getImpacto());
        dto.setPdfNombre(solicitud.getPdfNombre());
        dto.setUsuarioRegistro(solicitud.getUsuarioRegistro());
        dto.setCreatedAt(solicitud.getCreatedAt());
        dto.setUpdatedAt(solicitud.getUpdatedAt());

        // Tipo de solicitud
        if (solicitud.getTipoSolicitud() != null) {
            TipoSolicitudDTO tipoDTO = new TipoSolicitudDTO();
            tipoDTO.setId(solicitud.getTipoSolicitud().getId());
            tipoDTO.setCodigo(solicitud.getTipoSolicitud().getCodigo());
            tipoDTO.setNombre(solicitud.getTipoSolicitud().getNombre());
            dto.setTipoSolicitud(tipoDTO);
        }

        // Estado
        if (solicitud.getEstado() != null) {
            EstadoSolicitudDTO estadoDTO = new EstadoSolicitudDTO();
            estadoDTO.setId(solicitud.getEstado().getId());
            estadoDTO.setCodigo(solicitud.getEstado().getCodigo());
            estadoDTO.setNombre(solicitud.getEstado().getNombre());
            estadoDTO.setColor(solicitud.getEstado().getColor());
            estadoDTO.setFase(solicitud.getEstado().getFase());
            dto.setEstado(estadoDTO);
        }

        // ============================================================
        // CONTAR REQUERIMIENTOS DIRECTAMENTE DESDE EL REPOSITORIO
        // ============================================================
        int totalReq = requerimientoRepository.countBySolicitudId(solicitud.getId());
        dto.setTotalRequerimientos(totalReq);

        List<Requerimiento> reqs = requerimientoRepository.findBySolicitudId(solicitud.getId());
        dto.setRequerimientosFuncionales((int) reqs.stream()
                .filter(r -> r.getTipoRequerimiento() == 0).count());
        dto.setRequerimientosNoFuncionales((int) reqs.stream()
                .filter(r -> r.getTipoRequerimiento() == 1).count());

        return dto;
    }
}
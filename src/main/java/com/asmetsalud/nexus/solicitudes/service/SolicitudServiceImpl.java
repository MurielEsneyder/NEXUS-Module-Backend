package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.*;
import com.asmetsalud.nexus.solicitudes.entity.*;
import com.asmetsalud.nexus.solicitudes.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional("db1TransactionManager")
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final RequerimientoRepository requerimientoRepository;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final AuditoriaRepository auditoriaRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<String> PRIORIDADES_VALIDAS = Arrays.asList("alta", "media", "baja");

    @Override
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        log.info("📝 Creando nueva solicitud para: {}", request.getEmpleadoNombre());

        // Validar que el estado existe
        EstadoSolicitud estado = estadoSolicitudRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new RuntimeException("Estado no encontrado con ID: " + request.getEstadoId()));

        // Validar que el tipo existe
        TipoSolicitud tipo = tipoSolicitudRepository.findById(request.getTipoSolicitudId())
                .orElseThrow(() -> new RuntimeException("Tipo de solicitud no encontrado con ID: " + request.getTipoSolicitudId()));

        // Validar prioridad
        String prioridad = validarPrioridad(request.getPrioridad());

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
        solicitud.setTipoSolicitud(tipo);
        solicitud.setEstado(estado);
        solicitud.setPrioridad(prioridad);
        solicitud.setObservaciones(request.getObservaciones());
        solicitud.setImpacto(request.getImpacto());
        solicitud.setUsuarioRegistro(request.getEmpleadoNombre());

        // Guardar la solicitud
        Solicitud savedSolicitud = solicitudRepository.save(solicitud);
        log.info("✅ Solicitud creada con ID: {} y Código: {}", savedSolicitud.getId(), savedSolicitud.getCodigo());

        // Procesar requerimientos
        if (request.getRequerimientos() != null && !request.getRequerimientos().isEmpty()) {
            procesarRequerimientos(savedSolicitud, request.getRequerimientos());
        }

        // Registrar auditoría inicial
        registrarAuditoria(savedSolicitud, null, estado, "Solicitud creada");

        // Obtener la solicitud con todos los datos
        return convertirADTOConRequerimientos(savedSolicitud);
    }

    @Override
    public Page<SolicitudResponseDTO> obtenerTodasLasSolicitudes(Pageable pageable) {
        log.info("📋 Obteniendo todas las solicitudes");
        return solicitudRepository.findAll(pageable)
                .map(this::convertirADTOConRequerimientos);
    }

    @Override
    public SolicitudResponseDTO obtenerSolicitudPorId(Long id) {
        log.info("🔍 Obteniendo solicitud por ID: {}", id);
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        return convertirADTOConRequerimientos(solicitud);
    }

    @Override
    public SolicitudResponseDTO obtenerSolicitudPorCodigo(String codigo) {
        log.info("🔍 Obteniendo solicitud por código: {}", codigo);
        Solicitud solicitud = solicitudRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con código: " + codigo));
        return convertirADTOConRequerimientos(solicitud);
    }

    @Override
    public List<SolicitudResponseDTO> obtenerSolicitudesPorEmpleado(String documento) {
        log.info("🔍 Obteniendo solicitudes del empleado: {}", documento);
        List<Solicitud> solicitudes = solicitudRepository.findByEmpleadoDocumento(documento);
        return solicitudes.stream()
                .map(this::convertirADTOConRequerimientos)
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> obtenerSolicitudesPorEstado(Long estadoId) {
        log.info("🔍 Obteniendo solicitudes por estado: {}", estadoId);
        List<Solicitud> solicitudes = solicitudRepository.findByEstadoId(estadoId);
        return solicitudes.stream()
                .map(this::convertirADTOConRequerimientos)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditoriaDTO> obtenerHistorialCambios(Long solicitudId) {
        log.info("📜 Obteniendo historial de cambios para solicitud ID: {}", solicitudId);
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));
        List<Auditoria> auditorias = auditoriaRepository.findBySolicitudIdOrderByCreatedAtDesc(solicitudId);
        return auditorias.stream()
                .map(a -> convertirAuditoriaADTO(a, solicitud.getCodigo()))
                .collect(Collectors.toList());
    }

    @Override
    public Page<SolicitudResponseDTO> obtenerSolicitudesPorEmpleadoPaginado(String documento, Pageable pageable) {
        log.info("📋 Obteniendo solicitudes paginadas del empleado: {}", documento);
        return solicitudRepository.findByEmpleadoDocumentoOrderByFechaCreacionDesc(documento, pageable)
                .map(this::convertirADTOConRequerimientos);
    }

    @Override
    public SolicitudResponseDTO actualizarSolicitud(Long id, SolicitudRequestDTO request) {
        log.info("✏️ Actualizando solicitud ID: {}", id);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        // Actualizar campos básicos
        solicitud.setSolicitudProceso(request.getSolicitudProceso());
        solicitud.setProcesoId(request.getProcesoId());
        solicitud.setAreaId(request.getAreaId());
        solicitud.setMacroprocesoId(request.getMacroprocesoId());
        solicitud.setObservaciones(request.getObservaciones());
        solicitud.setImpacto(request.getImpacto());
        solicitud.setPrioridad(validarPrioridad(request.getPrioridad()));

        // Actualizar tipo si cambió
        if (!solicitud.getTipoSolicitud().getId().equals(request.getTipoSolicitudId())) {
            TipoSolicitud tipo = tipoSolicitudRepository.findById(request.getTipoSolicitudId())
                    .orElseThrow(() -> new RuntimeException("Tipo de solicitud no encontrado"));
            solicitud.setTipoSolicitud(tipo);
        }

        // Actualizar estado si cambió
        if (!solicitud.getEstado().getId().equals(request.getEstadoId())) {
            EstadoSolicitud estado = estadoSolicitudRepository.findById(request.getEstadoId())
                    .orElseThrow(() -> new RuntimeException("Estado no encontrado"));
            EstadoSolicitud estadoAnterior = solicitud.getEstado();
            solicitud.setEstado(estado);
            registrarAuditoria(solicitud, estadoAnterior, estado, "Actualización de estado desde edición");
        }

        Solicitud updatedSolicitud = solicitudRepository.save(solicitud);
        log.info("✅ Solicitud actualizada ID: {}", updatedSolicitud.getId());

        return convertirADTOConRequerimientos(updatedSolicitud);
    }

    @Override
    public SolicitudResponseDTO cambiarEstadoSolicitud(Long id, Long nuevoEstadoId, String observacion) {
        log.info("🔄 Cambiando estado de solicitud ID: {} a estado ID: {}", id, nuevoEstadoId);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        EstadoSolicitud nuevoEstado = estadoSolicitudRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado con ID: " + nuevoEstadoId));

        // No permitir cambiar al mismo estado
        if (estadoAnterior.getId().equals(nuevoEstado.getId())) {
            throw new RuntimeException("La solicitud ya se encuentra en el estado: " + estadoAnterior.getNombre());
        }

        solicitud.setEstado(nuevoEstado);
        Solicitud updatedSolicitud = solicitudRepository.save(solicitud);

        // Registrar auditoría
        registrarAuditoria(solicitud, estadoAnterior, nuevoEstado, observacion);

        log.info("✅ Estado cambiado de '{}' a '{}'", estadoAnterior.getNombre(), nuevoEstado.getNombre());

        return convertirADTOConRequerimientos(updatedSolicitud);
    }

    @Override
    public SolicitudResponseDTO actualizarPrioridad(Long id, String prioridad) {
        log.info("✏️ Actualizando prioridad de solicitud ID: {} a {}", id, prioridad);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        String prioridadValidada = validarPrioridad(prioridad);
        solicitud.setPrioridad(prioridadValidada);

        Solicitud updatedSolicitud = solicitudRepository.save(solicitud);
        log.info("✅ Prioridad actualizada a: {}", prioridadValidada);

        return convertirADTOConRequerimientos(updatedSolicitud);
    }

    @Override
    public void eliminarSolicitud(Long id) {
        log.info("🗑️ Eliminando solicitud ID: {}", id);
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));
        solicitudRepository.delete(solicitud);
        log.info("✅ Solicitud eliminada ID: {}", id);
    }

    @Override
    public Long contarSolicitudesPorEstado(Long estadoId) {
        log.info("📊 Contando solicitudes por estado ID: {}", estadoId);
        return solicitudRepository.countByEstadoId(estadoId);
    }

    @Override
    public byte[] generarPDF(Long id) {
        log.info("📄 Generando PDF para solicitud ID: {}", id);
        // TODO: Implementar generación de PDF con iText o JasperReports
        return new byte[0];
    }

    // ============================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================================

    private String validarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isEmpty() || !PRIORIDADES_VALIDAS.contains(prioridad.toLowerCase())) {
            log.warn("⚠️ Prioridad no válida: '{}', usando 'media' por defecto", prioridad);
            return "media";
        }
        return prioridad.toLowerCase();
    }

    private String generarCodigoSolicitud() {
        String fechaStr = LocalDate.now().format(DATE_FORMATTER);
        Long count = solicitudRepository.count() + 1;
        return String.format("SD-%s-%04d", fechaStr, count);
    }

    private void procesarRequerimientos(Solicitud solicitud, List<RequerimientoRequestDTO> requerimientosDTO) {
        int contadorFuncional = 0;
        int contadorNoFuncional = 0;

        // Obtener estado por defecto (Borrador)
        EstadoSolicitud estadoDefault = estadoSolicitudRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Estado por defecto (Borrador) no encontrado"));

        for (RequerimientoRequestDTO reqDTO : requerimientosDTO) {
            Requerimiento requerimiento = new Requerimiento();
            requerimiento.setSolicitud(solicitud);
            requerimiento.setTipoRequerimiento(reqDTO.getTipoRequerimiento());
            requerimiento.setObjetivo(reqDTO.getObjetivo());
            requerimiento.setDetalle(reqDTO.getDetalle());
            requerimiento.setCargoImpactado(reqDTO.getCargoImpactado());
            requerimiento.setFechaIngreso(LocalDate.now());
            requerimiento.setUsuarioRegistro(solicitud.getUsuarioRegistro());
            requerimiento.setEstado(estadoDefault);

            // Asignar número de orden y código
            if (reqDTO.getTipoRequerimiento() == 0) {
                contadorFuncional++;
                requerimiento.setNumeroOrden(contadorFuncional);
                requerimiento.setCodigo(String.format("RF_%02d", contadorFuncional));
            } else {
                contadorNoFuncional++;
                requerimiento.setNumeroOrden(contadorNoFuncional);
                requerimiento.setCodigo(String.format("RNF_%02d", contadorNoFuncional));
            }

            requerimientoRepository.save(requerimiento);
        }
        log.info("✅ Procesados {} requerimientos ({} funcionales, {} no funcionales)",
                requerimientosDTO.size(), contadorFuncional, contadorNoFuncional);
    }

    private void registrarAuditoria(Solicitud solicitud, EstadoSolicitud estadoAnterior,
                                    EstadoSolicitud estadoNuevo, String observacion) {
        Auditoria auditoria = new Auditoria();
        auditoria.setSolicitud(solicitud);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setObservacion(observacion);
        auditoria.setFase(estadoNuevo.getFase() != null ? estadoNuevo.getFase() : 1);
        auditoria.setUsuarioRegistro(solicitud.getUsuarioRegistro());
        auditoriaRepository.save(auditoria);
        log.info("📝 Auditoría registrada para solicitud ID: {}", solicitud.getId());
    }

    private SolicitudResponseDTO convertirADTOConRequerimientos(Solicitud solicitud) {
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
        dto.setPrioridad(solicitud.getPrioridad());
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
            tipoDTO.setActivo(solicitud.getTipoSolicitud().getActivo());
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
            estadoDTO.setActivo(solicitud.getEstado().getActivo());
            dto.setEstado(estadoDTO);
        }

        // Requerimientos
        List<RequerimientoResponseDTO> requerimientosDTO = new ArrayList<>();
        if (solicitud.getRequerimientos() != null) {
            int funcionales = 0;
            int noFuncionales = 0;

            for (Requerimiento req : solicitud.getRequerimientos()) {
                RequerimientoResponseDTO reqDTO = new RequerimientoResponseDTO();
                reqDTO.setId(req.getId());
                reqDTO.setCodigo(req.getCodigo());
                reqDTO.setTipoRequerimiento(req.getTipoRequerimiento());
                reqDTO.setTipoRequerimientoNombre(req.getTipoRequerimiento() == 0 ? "Funcional" : "No Funcional");
                reqDTO.setObjetivo(req.getObjetivo());
                reqDTO.setDetalle(req.getDetalle());
                reqDTO.setCargoImpactado(req.getCargoImpactado());
                reqDTO.setNumeroOrden(req.getNumeroOrden());
                if (req.getEstado() != null) {
                    reqDTO.setEstadoNombre(req.getEstado().getNombre());
                }
                requerimientosDTO.add(reqDTO);

                if (req.getTipoRequerimiento() == 0) {
                    funcionales++;
                } else {
                    noFuncionales++;
                }
            }
            dto.setRequerimientosFuncionales(funcionales);
            dto.setRequerimientosNoFuncionales(noFuncionales);
            dto.setTotalRequerimientos(requerimientosDTO.size());
        }

        dto.setRequerimientos(requerimientosDTO);

        // Auditorías
        List<AuditoriaDTO> auditoriasDTO = new ArrayList<>();
        if (solicitud.getAuditorias() != null) {
            for (Auditoria auditoria : solicitud.getAuditorias()) {
                auditoriasDTO.add(convertirAuditoriaADTO(auditoria, solicitud.getCodigo()));
            }
        }
        dto.setAuditorias(auditoriasDTO);

        return dto;
    }

    private AuditoriaDTO convertirAuditoriaADTO(Auditoria auditoria, String codigoSolicitud) {
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setId(auditoria.getId());
        dto.setSolicitudId(auditoria.getSolicitud().getId());
        dto.setCodigoSolicitud(codigoSolicitud);
        dto.setObservacion(auditoria.getObservacion());
        dto.setFase(auditoria.getFase());
        dto.setUsuarioRegistro(auditoria.getUsuarioRegistro());
        dto.setCreatedAt(auditoria.getCreatedAt());

        if (auditoria.getEstadoAnterior() != null) {
            EstadoSolicitudDTO estadoAntDTO = new EstadoSolicitudDTO();
            estadoAntDTO.setId(auditoria.getEstadoAnterior().getId());
            estadoAntDTO.setCodigo(auditoria.getEstadoAnterior().getCodigo());
            estadoAntDTO.setNombre(auditoria.getEstadoAnterior().getNombre());
            estadoAntDTO.setColor(auditoria.getEstadoAnterior().getColor());
            estadoAntDTO.setFase(auditoria.getEstadoAnterior().getFase());
            estadoAntDTO.setActivo(auditoria.getEstadoAnterior().getActivo());
            dto.setEstadoAnterior(estadoAntDTO);
        }

        if (auditoria.getEstadoNuevo() != null) {
            EstadoSolicitudDTO estadoNuevoDTO = new EstadoSolicitudDTO();
            estadoNuevoDTO.setId(auditoria.getEstadoNuevo().getId());
            estadoNuevoDTO.setCodigo(auditoria.getEstadoNuevo().getCodigo());
            estadoNuevoDTO.setNombre(auditoria.getEstadoNuevo().getNombre());
            estadoNuevoDTO.setColor(auditoria.getEstadoNuevo().getColor());
            estadoNuevoDTO.setFase(auditoria.getEstadoNuevo().getFase());
            estadoNuevoDTO.setActivo(auditoria.getEstadoNuevo().getActivo());
            dto.setEstadoNuevo(estadoNuevoDTO);
        }

        return dto;
    }
}
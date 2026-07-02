package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.*;
import com.asmetsalud.nexus.solicitudes.entity.*;
import com.asmetsalud.nexus.solicitudes.exception.ResourceNotFoundException;
import com.asmetsalud.nexus.solicitudes.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        TipoSolicitud tipoSolicitud = tipoSolicitudRepository.findById(request.getTipoSolicitudId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de solicitud no encontrado con ID: " + request.getTipoSolicitudId()));

        EstadoSolicitud estado = estadoSolicitudRepository.findById(request.getEstadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + request.getEstadoId()));

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

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con ID: {} y código: {}", solicitudGuardada.getId(), solicitudGuardada.getCodigo());

        if (request.getRequerimientos() != null && !request.getRequerimientos().isEmpty()) {
            crearRequerimientos(solicitudGuardada, request.getRequerimientos());
        }

        crearAuditoria(solicitudGuardada, null, estado, "Solicitud creada", 1);

        return convertirADTO(solicitudGuardada);
    }

    @Override
    public SolicitudResponseDTO actualizarSolicitud(Long id, SolicitudRequestDTO request) {
        log.info("Actualizando solicitud con ID: {}", id);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + id));

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

    @Override
    public Page<SolicitudResponseDTO> obtenerTodasLasSolicitudes(Pageable pageable) {
        return solicitudRepository.findAll(pageable).map(this::convertirADTO);
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

        solicitud.setEstado(nuevoEstado);
        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        crearAuditoria(solicitudActualizada, estadoAnterior, nuevoEstado, observacion, nuevoEstado.getFase());

        log.info("Estado cambiado exitosamente para solicitud ID: {}", id);
        return convertirADTO(solicitudActualizada);
    }

    @Override
    public Long contarSolicitudesPorEstado(Long estadoId) {
        return solicitudRepository.countByEstadoId(estadoId);
    }

    // ============================================================
    // PDF - Generar PDF
    // ============================================================
    @Override
    public byte[] generarPDF(Long solicitudId) {
        log.info("📄 Generando PDF para solicitud ID: {}", solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ============================================================
            // TÍTULO PRINCIPAL
            // ============================================================
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
            Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);

            Paragraph title = new Paragraph("SOLICITUD DE DESARROLLO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // ============================================================
            // ENCABEZADO
            // ============================================================
            document.add(new Paragraph("Código: " + solicitud.getCodigo(), boldFont));
            document.add(new Paragraph("Fecha: " + solicitud.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            document.add(new Paragraph(" "));

            // ============================================================
            // INFORMACIÓN DEL COLABORADOR
            // ============================================================
            document.add(new Paragraph("INFORMACIÓN DEL COLABORADOR", subtitleFont));
            document.add(new Paragraph("Nombre: " + solicitud.getEmpleadoNombre(), normalFont));
            document.add(new Paragraph("Correo: " + solicitud.getEmpleadoCorreo(), normalFont));
            document.add(new Paragraph("Cargo: " + solicitud.getEmpleadoCargo(), normalFont));
            document.add(new Paragraph("Sede: " + solicitud.getEmpleadoSede(), normalFont));
            document.add(new Paragraph(" "));

            // ============================================================
            // INFORMACIÓN DE LA SOLICITUD
            // ============================================================
            document.add(new Paragraph("INFORMACIÓN DE LA SOLICITUD", subtitleFont));
            document.add(new Paragraph("Proceso: " + solicitud.getSolicitudProceso(), normalFont));
            document.add(new Paragraph("Tipo: " + solicitud.getTipoSolicitud().getNombre(), normalFont));
            document.add(new Paragraph("Estado: " + solicitud.getEstado().getNombre(), normalFont));
            document.add(new Paragraph(" "));

            // ============================================================
            // IMPACTO
            // ============================================================
            document.add(new Paragraph("IMPACTO", subtitleFont));
            document.add(new Paragraph(solicitud.getImpacto(), normalFont));
            document.add(new Paragraph(" "));

            // ============================================================
            // REQUERIMIENTOS
            // ============================================================
            if (solicitud.getRequerimientos() != null && !solicitud.getRequerimientos().isEmpty()) {
                document.add(new Paragraph("REQUERIMIENTOS", subtitleFont));

                // Crear tabla de requerimientos
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);
                table.setSpacingAfter(10);

                // Encabezados
                PdfPCell header1 = new PdfPCell(new Phrase("Código", boldFont));
                PdfPCell header2 = new PdfPCell(new Phrase("Tipo", boldFont));
                PdfPCell header3 = new PdfPCell(new Phrase("Objetivo", boldFont));
                PdfPCell header4 = new PdfPCell(new Phrase("Cargo Impactado", boldFont));

                table.addCell(header1);
                table.addCell(header2);
                table.addCell(header3);
                table.addCell(header4);

                // Datos
                for (Requerimiento req : solicitud.getRequerimientos()) {
                    String tipo = req.getTipoRequerimiento() == 0 ? "Funcional" : "No Funcional";
                    table.addCell(req.getCodigo());
                    table.addCell(tipo);
                    table.addCell(req.getObjetivo());
                    table.addCell(req.getCargoImpactado() != null ? req.getCargoImpactado() : "N/A");
                }

                document.add(table);
                document.add(new Paragraph(" "));
            }

            // ============================================================
            // OBSERVACIONES
            // ============================================================
            if (solicitud.getObservaciones() != null && !solicitud.getObservaciones().isEmpty()) {
                document.add(new Paragraph("OBSERVACIONES", subtitleFont));
                document.add(new Paragraph(solicitud.getObservaciones(), normalFont));
            }

            // ============================================================
            // PIE DE PÁGINA
            // ============================================================
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Documento generado automáticamente por el sistema HyL Sparta", new Font(Font.HELVETICA, 10, Font.ITALIC)));

            document.close();

            log.info("✅ PDF generado exitosamente para solicitud ID: {}", solicitudId);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("❌ Error al generar PDF: {}", e.getMessage());
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    // ============================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================================

    private String generarCodigoSolicitud() {
        Long count = solicitudRepository.count();
        return String.format("SD_%03d", count + 1);
    }

    private void crearRequerimientos(Solicitud solicitud, List<RequerimientoRequestDTO> requerimientosDTO) {
        for (RequerimientoRequestDTO reqDTO : requerimientosDTO) {
            Requerimiento requerimiento = new Requerimiento();
            requerimiento.setSolicitud(solicitud);
            requerimiento.setTipoRequerimiento(reqDTO.getTipoRequerimiento());
            requerimiento.setObjetivo(reqDTO.getObjetivo());
            requerimiento.setDetalle(reqDTO.getDetalle());
            requerimiento.setCargoImpactado(reqDTO.getCargoImpactado());
            requerimiento.setUsuarioRegistro(solicitud.getUsuarioRegistro());

            Integer maxOrden = requerimientoRepository.findMaxNumeroOrdenBySolicitudIdAndTipo(
                    solicitud.getId(), reqDTO.getTipoRequerimiento());
            int nuevoOrden = (maxOrden != null) ? maxOrden + 1 : 1;
            requerimiento.setNumeroOrden(nuevoOrden);

            String prefijo = reqDTO.getTipoRequerimiento() == 0 ? "RF" : "RNF";
            requerimiento.setCodigo(String.format("%s_%02d", prefijo, nuevoOrden));

            requerimientoRepository.save(requerimiento);
        }
    }

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

        if (solicitud.getTipoSolicitud() != null) {
            TipoSolicitudDTO tipoDTO = new TipoSolicitudDTO();
            tipoDTO.setId(solicitud.getTipoSolicitud().getId());
            tipoDTO.setCodigo(solicitud.getTipoSolicitud().getCodigo());
            tipoDTO.setNombre(solicitud.getTipoSolicitud().getNombre());
            dto.setTipoSolicitud(tipoDTO);
        }

        if (solicitud.getEstado() != null) {
            EstadoSolicitudDTO estadoDTO = new EstadoSolicitudDTO();
            estadoDTO.setId(solicitud.getEstado().getId());
            estadoDTO.setCodigo(solicitud.getEstado().getCodigo());
            estadoDTO.setNombre(solicitud.getEstado().getNombre());
            estadoDTO.setColor(solicitud.getEstado().getColor());
            estadoDTO.setFase(solicitud.getEstado().getFase());
            dto.setEstado(estadoDTO);
        }

        if (solicitud.getRequerimientos() != null) {
            dto.setTotalRequerimientos(solicitud.getRequerimientos().size());
            dto.setRequerimientosFuncionales((int) solicitud.getRequerimientos().stream()
                    .filter(r -> r.getTipoRequerimiento() == 0).count());
            dto.setRequerimientosNoFuncionales((int) solicitud.getRequerimientos().stream()
                    .filter(r -> r.getTipoRequerimiento() == 1).count());

            List<RequerimientoResponseDTO> reqsDTO = solicitud.getRequerimientos().stream().map(req -> {
                RequerimientoResponseDTO reqDTO = new RequerimientoResponseDTO();
                reqDTO.setId(req.getId());
                reqDTO.setCodigo(req.getCodigo());
                reqDTO.setTipoRequerimiento(req.getTipoRequerimiento());
                reqDTO.setObjetivo(req.getObjetivo());
                reqDTO.setDetalle(req.getDetalle());
                reqDTO.setCargoImpactado(req.getCargoImpactado());
                return reqDTO;
            }).collect(Collectors.toList());
            dto.setRequerimientos(reqsDTO);
        }

        return dto;
    }
}
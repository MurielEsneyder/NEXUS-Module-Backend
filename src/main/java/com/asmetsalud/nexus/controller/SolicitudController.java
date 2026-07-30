package com.asmetsalud.nexus.controller;

import com.asmetsalud.nexus.dto.*;
import com.asmetsalud.nexus.db1.model.*;
import com.asmetsalud.nexus.db1.repository.*;
import com.asmetsalud.nexus.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Slf4j

public class SolicitudController {

    private final SolicitudService solicitudService;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final AreaRepository areaRepository;
    private final ProcesoRepository procesoRepository;
    private final MacroprocesoRepository macroprocesoRepository;
    private final CargoRepository cargoRepository;

    @PersistenceContext(unitName = "db1")
    private EntityManager entityManager;

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudRequestDTO request) {
        log.info("POST /solicitudes - Creando nueva solicitud");
        SolicitudResponseDTO response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // READ - Todas las solicitudes
    // ============================================================
    @GetMapping
    public ResponseEntity<Page<SolicitudResponseDTO>> obtenerTodasLasSolicitudes(
            @PageableDefault(size = 10, sort = {"fechaCreacion", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /solicitudes - Obteniendo todas las solicitudes");
        return ResponseEntity.ok(solicitudService.obtenerTodasLasSolicitudes(pageable));
    }

    // ============================================================
    // READ - Por ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorId(@PathVariable Long id) {
        log.info("GET /solicitudes/{} - Obteniendo solicitud por ID", id);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudPorId(id));
    }

    // ============================================================
    // READ - Por Código
    // ============================================================
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorCodigo(@PathVariable String codigo) {
        log.info("GET /solicitudes/codigo/{} - Obteniendo solicitud por código", codigo);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudPorCodigo(codigo));
    }

    // ============================================================
    // READ - Por Empleado
    // ============================================================
    @GetMapping("/empleado/{documento}")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudesPorEmpleado(@PathVariable String documento) {
        log.info("GET /solicitudes/empleado/{} - Obteniendo solicitudes del empleado", documento);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudesPorEmpleado(documento));
    }

    // ============================================================
    // READ - Por Estado
    // ============================================================
    @GetMapping("/estado/{estadoId}")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudesPorEstado(@PathVariable Long estadoId) {
        log.info("GET /solicitudes/estado/{} - Obteniendo solicitudes por estado", estadoId);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudesPorEstado(estadoId));
    }

    @GetMapping("/estados")
    public ResponseEntity<List<EstadoSolicitudDTO>> obtenerTodosLosEstados() {
        log.info("GET /solicitudes/estados - Obteniendo todos los estados");
        List<EstadoSolicitudDTO> estadosDTO = estadoSolicitudRepository.findAll().stream()
                .map(this::convertirEstadoADTO).collect(Collectors.toList());
        return ResponseEntity.ok(estadosDTO);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoSolicitudDTO>> obtenerTodosLosTipos() {
        log.info("GET /solicitudes/tipos - Obteniendo todos los tipos");
        List<TipoSolicitudDTO> tiposDTO = tipoSolicitudRepository.findAll().stream()
                .map(this::convertirTipoADTO).collect(Collectors.toList());
        return ResponseEntity.ok(tiposDTO);
    }

    @GetMapping("/areas")
    public ResponseEntity<List<AreaDTO>> obtenerTodasLasAreas() {
        log.info("GET /solicitudes/areas - Obteniendo todas las áreas");
        List<AreaDTO> areasDTO = areaRepository.findAll().stream()
                .map(this::convertirAreaADTO).collect(Collectors.toList());
        return ResponseEntity.ok(areasDTO);
    }

    @GetMapping("/procesos")
    public ResponseEntity<List<ProcesoDTO>> obtenerTodosLosProcesos() {
        log.info("GET /solicitudes/procesos - Obteniendo todos los procesos");
        List<ProcesoDTO> procesosDTO = procesoRepository.findAll().stream()
                .map(this::convertirProcesoADTO).collect(Collectors.toList());
        return ResponseEntity.ok(procesosDTO);
    }

    @GetMapping("/vicepresidencias")
    public ResponseEntity<List<MacroprocesoDTO>> obtenerTodasLasVicepresidencias() {
        log.info("GET /solicitudes/vicepresidencias - Obteniendo todos los macroprocesos/vicepresidencias");
        List<MacroprocesoDTO> macroprocesosDTO = macroprocesoRepository.findAll().stream()
                .map(this::convertirMacroprocesoADTO).collect(Collectors.toList());
        return ResponseEntity.ok(macroprocesosDTO);
    }

    @GetMapping("/cargos")
    public ResponseEntity<List<CargoDTO>> obtenerTodosLosCargos() {
        log.info("GET /solicitudes/cargos - Obteniendo todos los cargos");
        List<CargoDTO> cargosDTO = cargoRepository.findAll().stream()
                .map(this::convertirCargoADTO).collect(Collectors.toList());
        return ResponseEntity.ok(cargosDTO);
    }

    @GetMapping("/prioridades")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodasLasPrioridades() {
        log.info("GET /solicitudes/prioridades - Obteniendo todas las prioridades");
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT lv.id_lista_valor, lv.descripcion FROM com_lista_valores lv " +
                "JOIN com_listas_listavalores llv ON lv.id_lista_valor = llv.id_lista_valor " +
                "WHERE llv.id_lista = 15 ORDER BY lv.orden")
                .getResultList();
        List<Map<String, Object>> list = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ((Number) row[0]).longValue());
            map.put("nombre", row[1]);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<SolicitudResponseDTO> obtenerDetalleConRequerimientos(@PathVariable Long id) {
        log.info("📋 GET /solicitudes/{}/detalle - Obteniendo detalle con requerimientos", id);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudPorId(id));
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> actualizarSolicitud(
            @PathVariable Long id, @Valid @RequestBody SolicitudRequestDTO request) {
        log.info("PUT /solicitudes/{} - Actualizando solicitud", id);
        return ResponseEntity.ok(solicitudService.actualizarSolicitud(id, request));
    }

    @PostMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstadoSolicitud(
            @PathVariable Long id,
            @RequestParam Long nuevoEstadoId,
            @RequestParam(required = false) String observacion) {
        log.info("POST /solicitudes/{}/estado - Cambiando estado a {}", id, nuevoEstadoId);
        return ResponseEntity.ok(solicitudService.cambiarEstadoSolicitud(id, nuevoEstadoId, observacion));
    }

    @PostMapping("/{id}/prioridad")
    public ResponseEntity<SolicitudResponseDTO> actualizarPrioridad(
            @PathVariable Long id, @RequestParam String prioridad) {
        log.info("POST /solicitudes/{}/prioridad - Actualizando prioridad a {}", id, prioridad);
        return ResponseEntity.ok(solicitudService.actualizarPrioridad(id, prioridad));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        log.info("DELETE /solicitudes/{} - Eliminando solicitud", id);
        solicitudService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================
    @GetMapping("/contar/estado/{estadoId}")
    public ResponseEntity<Long> contarSolicitudesPorEstado(@PathVariable Long estadoId) {
        log.info("GET /solicitudes/contar/estado/{} - Contando solicitudes por estado", estadoId);
        return ResponseEntity.ok(solicitudService.contarSolicitudesPorEstado(estadoId));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<AuditoriaDTO>> obtenerHistorialCambios(@PathVariable Long id) {
        log.info("📜 GET /solicitudes/{}/historial - Obteniendo historial de cambios", id);
        return ResponseEntity.ok(solicitudService.obtenerHistorialCambios(id));
    }

    @GetMapping("/mis-solicitudes/{documento}")
    public ResponseEntity<Page<SolicitudResponseDTO>> obtenerMisSolicitudes(
            @PathVariable String documento,
            @PageableDefault(size = 10, sort = {"fechaCreacion"}, direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("📋 GET /solicitudes/mis-solicitudes/{} - Obteniendo solicitudes del empleado", documento);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudesPorEmpleadoPaginado(documento, pageable));
    }

    @GetMapping("/mis-solicitudes/correo/{correo}")
    public ResponseEntity<Page<SolicitudResponseDTO>> obtenerMisSolicitudesPorCorreo(
            @PathVariable String correo,
            @PageableDefault(size = 100, sort = {"fechaCreacion"}, direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("📋 GET /solicitudes/mis-solicitudes/correo/{} - Obteniendo solicitudes del empleado por correo", correo);
        return ResponseEntity.ok(solicitudService.obtenerSolicitudesPorCorreoPaginado(correo, pageable));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPDF(@PathVariable Long id) {
        log.info("📄 GET /solicitudes/{}/pdf - Generando PDF para descarga", id);
        try {
            byte[] pdfBytes = solicitudService.generarPDF(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "solicitud_" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            log.error("❌ Error al generar PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/pdf/ver")
    public ResponseEntity<byte[]> verPDF(@PathVariable Long id) {
        log.info("📄 GET /solicitudes/{}/pdf/ver - Generando PDF para visualización", id);
        try {
            byte[] pdfBytes = solicitudService.generarPDF(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            log.error("❌ Error al generar PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================================
    // MÉTODOS AUXILIARES DE CONVERSIÓN
    // ============================================================
    private EstadoSolicitudDTO convertirEstadoADTO(EstadoSolicitud estado) {
        EstadoSolicitudDTO dto = new EstadoSolicitudDTO();
        dto.setId(estado.getId());
        dto.setCodigo(estado.getCodigo());
        dto.setNombre(estado.getNombre());
        dto.setColor(estado.getColor());
        dto.setFase(estado.getFase());
        dto.setActivo(estado.getActivo());
        return dto;
    }

    private TipoSolicitudDTO convertirTipoADTO(TipoSolicitud tipo) {
        TipoSolicitudDTO dto = new TipoSolicitudDTO();
        dto.setId(tipo.getId());
        dto.setCodigo(tipo.getCodigo());
        dto.setNombre(tipo.getNombre() != null ? tipo.getNombre().toUpperCase() : null);
        dto.setActivo(tipo.getActivo());
        return dto;
    }

    private AreaDTO convertirAreaADTO(Area area) {
        AreaDTO dto = new AreaDTO();
        dto.setId(area.getId());
        dto.setCodigo(null);
        dto.setNombre(area.getNombre());
        dto.setActivo(true);
        return dto;
    }

    private ProcesoDTO convertirProcesoADTO(Proceso proceso) {
        ProcesoDTO dto = new ProcesoDTO();
        dto.setId(proceso.getId());
        dto.setCodigo(null);
        dto.setNombre(proceso.getNombre());
        dto.setActivo(true);
        return dto;
    }

    private MacroprocesoDTO convertirMacroprocesoADTO(Macroproceso macroproceso) {
        MacroprocesoDTO dto = new MacroprocesoDTO();
        dto.setId(macroproceso.getId());
        dto.setCodigo(null);
        dto.setNombre(macroproceso.getNombre());
        dto.setActivo(true);
        return dto;
    }

    private CargoDTO convertirCargoADTO(Cargo cargo) {
        CargoDTO dto = new CargoDTO();
        dto.setId(cargo.getId());
        dto.setCodigo(null);
        dto.setNombre(cargo.getNombre());
        dto.setActivo(true);
        return dto;
    }
}

package com.asmetsalud.nexus.solicitudes.controller;

import com.asmetsalud.nexus.solicitudes.dto.*;
import com.asmetsalud.nexus.solicitudes.entity.Area;
import com.asmetsalud.nexus.solicitudes.entity.Cargo;
import com.asmetsalud.nexus.solicitudes.entity.EstadoSolicitud;
import com.asmetsalud.nexus.solicitudes.entity.Macroproceso;
import com.asmetsalud.nexus.solicitudes.entity.Proceso;
import com.asmetsalud.nexus.solicitudes.entity.TipoSolicitud;
import com.asmetsalud.nexus.solicitudes.repository.AreaRepository;
import com.asmetsalud.nexus.solicitudes.repository.CargoRepository;
import com.asmetsalud.nexus.solicitudes.repository.EstadoSolicitudRepository;
import com.asmetsalud.nexus.solicitudes.repository.MacroprocesoRepository;
import com.asmetsalud.nexus.solicitudes.repository.ProcesoRepository;
import com.asmetsalud.nexus.solicitudes.repository.TipoSolicitudRepository;
import com.asmetsalud.nexus.solicitudes.service.SolicitudService;
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
@CrossOrigin(origins = "http://localhost:4200")
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
        Page<SolicitudResponseDTO> response = solicitudService.obtenerTodasLasSolicitudes(pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // READ - Por ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorId(@PathVariable Long id) {
        log.info("GET /solicitudes/{} - Obteniendo solicitud por ID", id);
        SolicitudResponseDTO response = solicitudService.obtenerSolicitudPorId(id);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // READ - Por Código
    // ============================================================
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorCodigo(@PathVariable String codigo) {
        log.info("GET /solicitudes/codigo/{} - Obteniendo solicitud por código", codigo);
        SolicitudResponseDTO response = solicitudService.obtenerSolicitudPorCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // READ - Por Empleado
    // ============================================================
    @GetMapping("/empleado/{documento}")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudesPorEmpleado(
            @PathVariable String documento) {
        log.info("GET /solicitudes/empleado/{} - Obteniendo solicitudes del empleado", documento);
        List<SolicitudResponseDTO> response = solicitudService.obtenerSolicitudesPorEmpleado(documento);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // READ - Por Estado
    // ============================================================
    @GetMapping("/estado/{estadoId}")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudesPorEstado(
            @PathVariable Long estadoId) {
        log.info("GET /solicitudes/estado/{} - Obteniendo solicitudes por estado", estadoId);
        List<SolicitudResponseDTO> response = solicitudService.obtenerSolicitudesPorEstado(estadoId);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // READ - Obtener todos los estados
    // ============================================================
    @GetMapping("/estados")
    public ResponseEntity<List<EstadoSolicitudDTO>> obtenerTodosLosEstados() {
        log.info("GET /solicitudes/estados - Obteniendo todos los estados");
        List<EstadoSolicitud> estados = estadoSolicitudRepository.findAll();
        List<EstadoSolicitudDTO> estadosDTO = estados.stream()
                .map(this::convertirEstadoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(estadosDTO);
    }

    // ============================================================
    // READ - Obtener todos los tipos
    // ============================================================
    @GetMapping("/tipos")
    public ResponseEntity<List<TipoSolicitudDTO>> obtenerTodosLosTipos() {
        log.info("GET /solicitudes/tipos - Obteniendo todos los tipos");
        List<TipoSolicitud> tipos = tipoSolicitudRepository.findAll();
        List<TipoSolicitudDTO> tiposDTO = tipos.stream()
                .map(this::convertirTipoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tiposDTO);
    }

    // ============================================================
    // READ - Obtener todas las áreas
    // ============================================================
    @GetMapping("/areas")
    public ResponseEntity<List<AreaDTO>> obtenerTodasLasAreas() {
        log.info("GET /solicitudes/areas - Obteniendo todas las áreas");
        List<Area> areas = areaRepository.findAll();
        List<AreaDTO> areasDTO = areas.stream()
                .map(this::convertirAreaADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(areasDTO);
    }

    // ============================================================
    // READ - Obtener todos los procesos
    // ============================================================
    @GetMapping("/procesos")
    public ResponseEntity<List<ProcesoDTO>> obtenerTodosLosProcesos() {
        log.info("GET /solicitudes/procesos - Obteniendo todos los procesos");
        List<Proceso> procesos = procesoRepository.findAll();
        List<ProcesoDTO> procesosDTO = procesos.stream()
                .map(this::convertirProcesoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(procesosDTO);
    }

    // ============================================================
    // READ - Obtener todas las vicepresidencias (macroprocesos)
    // ============================================================
    @GetMapping("/vicepresidencias")
    public ResponseEntity<List<MacroprocesoDTO>> obtenerTodasLasVicepresidencias() {
        log.info("GET /solicitudes/vicepresidencias - Obteniendo todos los macroprocesos/vicepresidencias");
        List<Macroproceso> macroprocesos = macroprocesoRepository.findAll();
        List<MacroprocesoDTO> macroprocesosDTO = macroprocesos.stream()
                .map(this::convertirMacroprocesoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(macroprocesosDTO);
    }

    // ============================================================
    // READ - Obtener todos los cargos
    // ============================================================
    @GetMapping("/cargos")
    public ResponseEntity<List<CargoDTO>> obtenerTodosLosCargos() {
        log.info("GET /solicitudes/cargos - Obteniendo todos los cargos");
        List<Cargo> cargos = cargoRepository.findAll();
        List<CargoDTO> cargosDTO = cargos.stream()
                .map(this::convertirCargoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cargosDTO);
    }

    // ============================================================
    // READ - Obtener todas las prioridades
    // ============================================================
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

    // ============================================================
    // READ - Detalle con requerimientos
    // ============================================================
    @GetMapping("/{id}/detalle")
    public ResponseEntity<SolicitudResponseDTO> obtenerDetalleConRequerimientos(@PathVariable Long id) {
        log.info("📋 GET /solicitudes/{}/detalle - Obteniendo detalle con requerimientos", id);
        SolicitudResponseDTO response = solicitudService.obtenerSolicitudPorId(id);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE - Actualizar solicitud
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> actualizarSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRequestDTO request) {
        log.info("PUT /solicitudes/{} - Actualizando solicitud", id);
        SolicitudResponseDTO response = solicitudService.actualizarSolicitud(id, request);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE - Cambiar estado
    // ============================================================
    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstadoSolicitud(
            @PathVariable Long id,
            @RequestParam Long nuevoEstadoId,
            @RequestParam(required = false) String observacion) {
        log.info("PATCH /solicitudes/{}/estado - Cambiando estado a {}", id, nuevoEstadoId);
        SolicitudResponseDTO response = solicitudService.cambiarEstadoSolicitud(id, nuevoEstadoId, observacion);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // UPDATE - Actualizar prioridad
    // ============================================================
    @PatchMapping("/{id}/prioridad")
    public ResponseEntity<SolicitudResponseDTO> actualizarPrioridad(
            @PathVariable Long id,
            @RequestParam String prioridad) {
        log.info("PATCH /solicitudes/{}/prioridad - Actualizando prioridad a {}", id, prioridad);
        SolicitudResponseDTO response = solicitudService.actualizarPrioridad(id, prioridad);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // DELETE - Eliminar solicitud
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        log.info("DELETE /solicitudes/{} - Eliminando solicitud", id);
        solicitudService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ESTADÍSTICAS - Contar por estado
    // ============================================================
    @GetMapping("/contar/estado/{estadoId}")
    public ResponseEntity<Long> contarSolicitudesPorEstado(@PathVariable Long estadoId) {
        log.info("GET /solicitudes/contar/estado/{} - Contando solicitudes por estado", estadoId);
        Long count = solicitudService.contarSolicitudesPorEstado(estadoId);
        return ResponseEntity.ok(count);
    }

    // ============================================================
    // READ - Historial de cambios de una solicitud
    // ============================================================
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<com.asmetsalud.nexus.solicitudes.dto.AuditoriaDTO>> obtenerHistorialCambios(@PathVariable Long id) {
        log.info("📜 GET /solicitudes/{}/historial - Obteniendo historial de cambios", id);
        List<com.asmetsalud.nexus.solicitudes.dto.AuditoriaDTO> historial = solicitudService.obtenerHistorialCambios(id);
        return ResponseEntity.ok(historial);
    }

    // ============================================================
    // READ - Mis solicitudes (por documento del empleado, paginado)
    // ============================================================
    @GetMapping("/mis-solicitudes/{documento}")
    public ResponseEntity<Page<SolicitudResponseDTO>> obtenerMisSolicitudes(
            @PathVariable String documento,
            @PageableDefault(size = 10, sort = {"fechaCreacion"}, direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("📋 GET /solicitudes/mis-solicitudes/{} - Obteniendo solicitudes del empleado", documento);
        Page<SolicitudResponseDTO> response = solicitudService.obtenerSolicitudesPorEmpleadoPaginado(documento, pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // READ - Mis solicitudes (por correo del empleado, paginado)
    // ============================================================
    @GetMapping("/mis-solicitudes/correo/{correo}")
    public ResponseEntity<Page<SolicitudResponseDTO>> obtenerMisSolicitudesPorCorreo(
            @PathVariable String correo,
            @PageableDefault(size = 100, sort = {"fechaCreacion"}, direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("📋 GET /solicitudes/mis-solicitudes/correo/{} - Obteniendo solicitudes del empleado por correo", correo);
        Page<SolicitudResponseDTO> response = solicitudService.obtenerSolicitudesPorCorreoPaginado(correo, pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // PDF - Descargar PDF
    // ============================================================
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

    // ============================================================
    // PDF - Ver PDF en navegador
    // ============================================================
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
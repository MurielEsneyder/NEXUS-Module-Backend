package com.asmetsalud.nexus.solicitudes.controller;

import com.asmetsalud.nexus.solicitudes.dto.*;
import com.asmetsalud.nexus.solicitudes.entity.EstadoSolicitud;
import com.asmetsalud.nexus.solicitudes.entity.TipoSolicitud;
import com.asmetsalud.nexus.solicitudes.repository.EstadoSolicitudRepository;
import com.asmetsalud.nexus.solicitudes.repository.TipoSolicitudRepository;
import com.asmetsalud.nexus.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final EstadoSolicitudRepository estadoSolicitudRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudRequestDTO request) {
        log.info("POST /solicitudes - Creando nueva solicitud");
        SolicitudResponseDTO response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SolicitudResponseDTO>> obtenerTodasLasSolicitudes(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /solicitudes - Obteniendo todas las solicitudes");
        Page<SolicitudResponseDTO> response = solicitudService.obtenerTodasLasSolicitudes(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorId(@PathVariable Long id) {
        log.info("GET /solicitudes/{} - Obteniendo solicitud por ID", id);
        SolicitudResponseDTO response = solicitudService.obtenerSolicitudPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorCodigo(@PathVariable String codigo) {
        log.info("GET /solicitudes/codigo/{} - Obteniendo solicitud por código", codigo);
        SolicitudResponseDTO response = solicitudService.obtenerSolicitudPorCodigo(codigo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/empleado/{documento}")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudesPorEmpleado(
            @PathVariable String documento) {
        log.info("GET /solicitudes/empleado/{} - Obteniendo solicitudes del empleado", documento);
        List<SolicitudResponseDTO> response = solicitudService.obtenerSolicitudesPorEmpleado(documento);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estadoId}")
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudesPorEstado(
            @PathVariable Long estadoId) {
        log.info("GET /solicitudes/estado/{} - Obteniendo solicitudes por estado", estadoId);
        List<SolicitudResponseDTO> response = solicitudService.obtenerSolicitudesPorEstado(estadoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estados")
    public ResponseEntity<List<EstadoSolicitudDTO>> obtenerTodosLosEstados() {
        log.info("GET /solicitudes/estados - Obteniendo todos los estados");
        List<EstadoSolicitud> estados = estadoSolicitudRepository.findAll();
        List<EstadoSolicitudDTO> estadosDTO = estados.stream()
                .map(this::convertirEstadoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(estadosDTO);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoSolicitudDTO>> obtenerTodosLosTipos() {
        log.info("GET /solicitudes/tipos - Obteniendo todos los tipos");
        List<TipoSolicitud> tipos = tipoSolicitudRepository.findAll();
        List<TipoSolicitudDTO> tiposDTO = tipos.stream()
                .map(this::convertirTipoADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tiposDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> actualizarSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRequestDTO request) {
        log.info("PUT /solicitudes/{} - Actualizando solicitud", id);
        SolicitudResponseDTO response = solicitudService.actualizarSolicitud(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstadoSolicitud(
            @PathVariable Long id,
            @RequestParam Long nuevoEstadoId,
            @RequestParam(required = false) String observacion) {
        log.info("PATCH /solicitudes/{}/estado - Cambiando estado a {}", id, nuevoEstadoId);
        SolicitudResponseDTO response = solicitudService.cambiarEstadoSolicitud(id, nuevoEstadoId, observacion);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        log.info("DELETE /solicitudes/{} - Eliminando solicitud", id);
        solicitudService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contar/estado/{estadoId}")
    public ResponseEntity<Long> contarSolicitudesPorEstado(@PathVariable Long estadoId) {
        log.info("GET /solicitudes/contar/estado/{} - Contando solicitudes por estado", estadoId);
        Long count = solicitudService.contarSolicitudesPorEstado(estadoId);
        return ResponseEntity.ok(count);
    }

    private EstadoSolicitudDTO convertirEstadoADTO(EstadoSolicitud estado) {
        EstadoSolicitudDTO dto = new EstadoSolicitudDTO();
        dto.setId(estado.getId());
        dto.setCodigo(estado.getCodigo());
        dto.setNombre(estado.getNombre());
        dto.setColor(estado.getColor());
        dto.setFase(estado.getFase());
        return dto;
    }

    private TipoSolicitudDTO convertirTipoADTO(TipoSolicitud tipo) {
        TipoSolicitudDTO dto = new TipoSolicitudDTO();
        dto.setId(tipo.getId());
        dto.setCodigo(tipo.getCodigo());
        dto.setNombre(tipo.getNombre());
        return dto;
    }
}
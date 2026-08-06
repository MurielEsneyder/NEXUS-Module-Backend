package com.asmetsalud.nexus.controller;

import com.asmetsalud.nexus.db1.model.Requerimiento;
import com.asmetsalud.nexus.db1.model.RequerimientoImagen;
import com.asmetsalud.nexus.db1.repository.RequerimientoImagenRepository;
import com.asmetsalud.nexus.db1.repository.RequerimientoRepository;
import com.asmetsalud.nexus.dto.ImagenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para gestionar las imágenes de los requerimientos.
 *
 * Endpoints mapeados en Oracle:
 *   285  POST   /api/requerimientos/{id}/imagenes        -> Agregar imágenes a un requerimiento
 *   287  GET    /api/imagenes/{id}                       -> Obtener imagen por ID
 *   290  GET    /api/requerimientos/{reqId}/imagenes/{imgId} -> Obtener imagen específica de un requerimiento
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class RequerimientoImagenController {

    private final RequerimientoRepository requerimientoRepository;
    private final RequerimientoImagenRepository requerimientoImagenRepository;

    // ============================================================
    // POST /api/requerimientos/{requerimientoId}/imagenes
    // Oracle mapping 285: Agregar imágenes a un requerimiento
    // ============================================================
    @PostMapping("/api/requerimientos/{requerimientoId}/imagenes")
    public ResponseEntity<?> agregarImagenes(
            @PathVariable Long requerimientoId,
            @RequestBody List<ImagenDTO> imagenesDTO) {

        log.info("POST /api/requerimientos/{}/imagenes - Agregando {} imagenes",
                requerimientoId, imagenesDTO != null ? imagenesDTO.size() : 0);

        Requerimiento requerimiento = requerimientoRepository.findById(requerimientoId)
                .orElseThrow(() -> new RuntimeException("Requerimiento no encontrado con ID: " + requerimientoId));

        if (imagenesDTO == null || imagenesDTO.isEmpty()) {
            return ResponseEntity.badRequest().body("Debe enviar al menos una imagen");
        }

        List<RequerimientoImagen> savedImages = new java.util.ArrayList<>();
        for (ImagenDTO imgDto : imagenesDTO) {
            if (imgDto.getUrl() != null && !imgDto.getUrl().trim().isEmpty()) {
                RequerimientoImagen reqImg = new RequerimientoImagen();
                reqImg.setRequerimiento(requerimiento);
                reqImg.setUrlImagen(imgDto.getUrl().trim());
                reqImg.setOrden(imgDto.getOrden() != null ? imgDto.getOrden() : 1);
                reqImg.setUsuarioRegistro(requerimiento.getUsuarioRegistro());
                savedImages.add(requerimientoImagenRepository.save(reqImg));
            }
        }

        log.info("POST /api/requerimientos/{}/imagenes - Guardadas {} imagenes exitosamente",
                requerimientoId, savedImages.size());

        List<ImagenDTO> response = savedImages.stream().map(img -> {
            ImagenDTO dto = new ImagenDTO();
            dto.setId(img.getId());
            dto.setUrl(img.getUrlImagen());
            dto.setOrden(img.getOrden());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // GET /api/requerimientos/{requerimientoId}/imagenes
    // Listar todas las imágenes de un requerimiento
    // ============================================================
    @GetMapping("/api/requerimientos/{requerimientoId}/imagenes")
    public ResponseEntity<List<ImagenDTO>> obtenerImagenesPorRequerimiento(
            @PathVariable Long requerimientoId) {

        log.info("GET /api/requerimientos/{}/imagenes - Consultando imagenes", requerimientoId);

        // Verificar que existe el requerimiento
        if (!requerimientoRepository.existsById(requerimientoId)) {
            return ResponseEntity.notFound().build();
        }

        List<RequerimientoImagen> imagenes = requerimientoImagenRepository
                .findByRequerimientoIdOrderByOrdenAsc(requerimientoId);

        List<ImagenDTO> response = imagenes.stream().map(img -> {
            ImagenDTO dto = new ImagenDTO();
            dto.setId(img.getId());
            dto.setUrl(img.getUrlImagen());
            dto.setOrden(img.getOrden());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET /api/imagenes/{id}
    // Oracle mapping 287: Obtener una imagen por su ID
    // ============================================================
    @GetMapping("/api/imagenes/{id}")
    public ResponseEntity<ImagenDTO> obtenerImagenPorId(@PathVariable Long id) {

        log.info("GET /api/imagenes/{} - Consultando imagen por ID", id);

        return requerimientoImagenRepository.findById(id)
                .map(img -> {
                    ImagenDTO dto = new ImagenDTO();
                    dto.setId(img.getId());
                    dto.setUrl(img.getUrlImagen());
                    dto.setOrden(img.getOrden());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // GET /api/requerimientos/{reqId}/imagenes/{imgId}
    // Oracle mapping 290: Obtener imagen específica de un requerimiento
    // ============================================================
    @GetMapping("/api/requerimientos/{requerimientoId}/imagenes/{imagenId}")
    public ResponseEntity<ImagenDTO> obtenerImagenEspecifica(
            @PathVariable Long requerimientoId,
            @PathVariable Long imagenId) {

        log.info("GET /api/requerimientos/{}/imagenes/{} - Consultando imagen especifica",
                requerimientoId, imagenId);

        return requerimientoImagenRepository.findById(imagenId)
                .filter(img -> img.getRequerimiento().getId().equals(requerimientoId))
                .map(img -> {
                    ImagenDTO dto = new ImagenDTO();
                    dto.setId(img.getId());
                    dto.setUrl(img.getUrlImagen());
                    dto.setOrden(img.getOrden());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // DELETE /api/requerimientos/{reqId}/imagenes/{imgId}
    // Eliminar una imagen específica
    // ============================================================
    @DeleteMapping("/api/requerimientos/{requerimientoId}/imagenes/{imagenId}")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long requerimientoId,
            @PathVariable Long imagenId) {

        log.info("DELETE /api/requerimientos/{}/imagenes/{} - Eliminando imagen",
                requerimientoId, imagenId);

        return requerimientoImagenRepository.findById(imagenId)
                .filter(img -> img.getRequerimiento().getId().equals(requerimientoId))
                .map(img -> {
                    requerimientoImagenRepository.delete(img);
                    log.info("DELETE - Imagen {} eliminada exitosamente del requerimiento {}",
                            imagenId, requerimientoId);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

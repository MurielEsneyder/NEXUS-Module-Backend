package com.asmetsalud.nexus.solicitudes.controller;

import com.asmetsalud.nexus.solicitudes.dto.CorreoSolicitudDTO;
import com.asmetsalud.nexus.solicitudes.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping("/enviar-correo-solicitud")
    public ResponseEntity<String> enviarCorreoSolicitud(@RequestBody CorreoSolicitudDTO payload) {
        log.info("Recibida petición para enviar correo. Solicitud: {}", payload.getNumeroSolicitud());
        try {
            notificacionService.enviarNotificacionConPdf(payload);
            return ResponseEntity.ok("Correo enviado exitosamente");
        } catch (Exception e) {
            log.error("Error al procesar la petición de envío de correo", e);
            return ResponseEntity.internalServerError().body("Error al enviar correo: " + e.getMessage());
        }
    }
}

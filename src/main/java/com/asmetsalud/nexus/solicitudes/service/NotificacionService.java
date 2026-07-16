package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.CorreoSolicitudDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Nombre del microservicio registrado en Eureka
    private static final String COMMONS_SERVICE_URL = "http://COMMONS-SPARTAV2/api/correo/enviar-correo";

    public void enviarNotificacionConPdf(CorreoSolicitudDTO datos) {
        log.info("Iniciando envío de correo a: {} a través de commons", datos.getCorreoDestinatario());

        try {
            Map<String, Object> payload = new HashMap<>();
            
            // Reemplaza esto con un correo válido o parametrizado si commons lo exige
            payload.put("fromAdd", "notificaciones@asmetsalud.com"); 
            payload.put("from", "Sistema Nexus");
            payload.put("to", Collections.singletonList(datos.getCorreoDestinatario()));
            
            payload.put("subject", "Solicitud de Requerimiento de Desarrollo - " + datos.getNumeroSolicitud());
            payload.put("title", "Notificación de Sistema Nexus");
            payload.put("body", buildHtmlBody(datos));

            if (datos.getPdfBase64() != null && !datos.getPdfBase64().isEmpty()) {
                String base64Data = datos.getPdfBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }

                Map<String, String> file = new HashMap<>();
                file.put("name", "Solicitud_" + datos.getNumeroSolicitud() + ".pdf");
                file.put("type", "application/pdf");
                file.put("content", base64Data);

                payload.put("files", Collections.singletonList(file));
            }

            // Convertir a String JSON porque el CorreoController recibe @RequestBody String
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            log.info("Enviando petición HTTP a sv2-commons...");
            String response = restTemplate.postForObject(COMMONS_SERVICE_URL, request, String.class);
            log.info("✅ Respuesta de commons: {}", response);

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar y enviar el correo a través de commons", e);
            throw new RuntimeException("Error al comunicarse con sv2-commons para el envío de correo", e);
        }
    }

    private String buildHtmlBody(CorreoSolicitudDTO datos) {
        return "Hola <strong>" + datos.getNombreSolicitante() + "</strong>,<br><br>" +
               "Su solicitud ha sido registrada exitosamente en el sistema.<br><br>" +
               "<b>Número Solicitud:</b> " + datos.getNumeroSolicitud() + "<br>" +
               "<b>Modalidad:</b> " + datos.getModalidad() + "<br><br>" +
               "Se adjunta a este correo el documento PDF con todos los detalles registrados.<br><br>" +
               "<i style=\"color: #777; font-size: 12px;\">Este es un mensaje automático, por favor no responda.</i>";
    }
}

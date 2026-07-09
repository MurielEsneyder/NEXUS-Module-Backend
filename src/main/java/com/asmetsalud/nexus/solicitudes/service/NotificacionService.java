package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.CorreoSolicitudDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final JavaMailSender javaMailSender;

    public void enviarNotificacionConPdf(CorreoSolicitudDTO datos) {
        log.info("Iniciando envío de correo a: {}", datos.getCorreoDestinatario());

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(datos.getCorreoDestinatario());
            helper.setSubject("Solicitud de Requerimiento de Desarrollo - " + datos.getNumeroSolicitud());

            // Construir el cuerpo HTML
            String htmlBody = buildHtmlBody(datos);
            helper.setText(htmlBody, true);

            // Decodificar Base64 del PDF
            if (datos.getPdfBase64() != null && !datos.getPdfBase64().isEmpty()) {
                String base64Data = datos.getPdfBase64();
                // Remover prefijo data:application/pdf;base64, si existe
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }

                byte[] pdfBytes = Base64.getDecoder().decode(base64Data);
                ByteArrayResource pdfAttachment = new ByteArrayResource(pdfBytes);
                
                String nombrePdf = "Solicitud_" + datos.getNumeroSolicitud() + ".pdf";
                helper.addAttachment(nombrePdf, pdfAttachment, "application/pdf");
            }

            javaMailSender.send(message);
            log.info("✅ Correo enviado exitosamente a {}", datos.getCorreoDestinatario());

        } catch (MessagingException e) {
            log.error("❌ Error al armar el mensaje de correo", e);
            throw new RuntimeException("Error al enviar el correo de notificación", e);
        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar y enviar el correo", e);
            throw new RuntimeException("Error inesperado al enviar el correo", e);
        }
    }

    private String buildHtmlBody(CorreoSolicitudDTO datos) {
        return "<html>" +
                "<body style=\"font-family: Arial, sans-serif; color: #333;\">" +
                "<div style=\"background-color: #3bafb6; padding: 20px; color: white;\">" +
                "  <h2>Notificación de Sistema Nexus</h2>" +
                "</div>" +
                "<div style=\"padding: 20px;\">" +
                "  <p>Hola <strong>" + datos.getNombreSolicitante() + "</strong>,</p>" +
                "  <p>Su solicitud ha sido registrada exitosamente en el sistema.</p>" +
                "  <table style=\"border-collapse: collapse; width: 100%; max-width: 600px; margin-top: 20px;\">" +
                "    <tr>" +
                "      <th style=\"border: 1px solid #ddd; padding: 8px; text-align: left; background-color: #f2f2f2;\">Número Solicitud</th>" +
                "      <td style=\"border: 1px solid #ddd; padding: 8px;\">" + datos.getNumeroSolicitud() + "</td>" +
                "    </tr>" +
                "    <tr>" +
                "      <th style=\"border: 1px solid #ddd; padding: 8px; text-align: left; background-color: #f2f2f2;\">Modalidad</th>" +
                "      <td style=\"border: 1px solid #ddd; padding: 8px;\">" + datos.getModalidad() + "</td>" +
                "    </tr>" +
                "  </table>" +
                "  <br/>" +
                "  <p>Se adjunta a este correo el documento PDF con todos los detalles registrados.</p>" +
                "  <p style=\"color: #777; font-size: 12px;\">Este es un mensaje automático, por favor no responda.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}

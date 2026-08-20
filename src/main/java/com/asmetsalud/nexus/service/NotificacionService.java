package com.asmetsalud.nexus.service;

import com.asmetsalud.nexus.dto.CorreoSolicitudDTO;
import com.asmetsalud.nexus.dto.ImagenDTO;
import com.asmetsalud.nexus.dto.RequerimientoResponseDTO;
import com.asmetsalud.nexus.dto.SolicitudResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String COMMONS_SERVICE_URL = "http://COMMONS-SPARTAV2/api/correo/enviar-correo";

    public void enviarNotificacionConPdf(CorreoSolicitudDTO datos) {
        log.info("Iniciando envío de correo a: {} a través de commons", datos.getCorreoDestinatario());

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("fromAdd", "solicitudesdesarrollo@asmetsalud.com");
            payload.put("from", "Sistema Nexus");
            payload.put("to", Collections.singletonList(datos.getCorreoDestinatario()));
            payload.put("subject", "Solicitud de Requerimiento de Desarrollo - " + datos.getNumeroSolicitud());
            payload.put("title", "Notificación de Sistema Nexus");
            payload.put("body", buildHtmlBody(datos));

            List<Map<String, String>> filesList = new ArrayList<>();

            // 1. Adjuntar documento PDF
            if (datos.getPdfBase64() != null && !datos.getPdfBase64().isEmpty()) {
                String base64Data = datos.getPdfBase64();
                if (base64Data.contains(",")) {
                    base64Data = base64Data.split(",")[1];
                }
                Map<String, String> file = new HashMap<>();
                file.put("name", "Solicitud_" + datos.getNumeroSolicitud() + ".pdf");
                file.put("type", "text/plain");
                file.put("content", base64Data);
                filesList.add(file);
            }

            // 2. Adjuntar imágenes de los requerimientos como archivos adjuntos
            if (datos.getSolicitudCompleta() != null && datos.getSolicitudCompleta().getRequerimientos() != null) {
                for (RequerimientoResponseDTO req : datos.getSolicitudCompleta().getRequerimientos()) {
                    if (req.getImagenesUrls() != null && !req.getImagenesUrls().isEmpty()) {
                        int imgIdx = 1;
                        for (ImagenDTO imgDto : req.getImagenesUrls()) {
                            String urlStr = imgDto.getUrl();
                            if (urlStr != null && !urlStr.trim().isEmpty()) {
                                String base64Content = null;
                                String ext = "png";
                                if (urlStr.startsWith("data:image/")) {
                                    int commaIdx = urlStr.indexOf(",");
                                    if (commaIdx > 0) {
                                        String meta = urlStr.substring(0, commaIdx);
                                        if (meta.contains("jpeg") || meta.contains("jpg")) ext = "jpg";
                                        else if (meta.contains("gif")) ext = "gif";
                                        else if (meta.contains("webp")) ext = "webp";
                                        base64Content = urlStr.substring(commaIdx + 1).replaceAll("\\s+", "");
                                    }
                                } else if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                                    try {
                                        byte[] bytes = new java.net.URL(urlStr).openStream().readAllBytes();
                                        base64Content = Base64.getEncoder().encodeToString(bytes);
                                        if (urlStr.toLowerCase().endsWith(".jpg") || urlStr.toLowerCase().endsWith(".jpeg")) ext = "jpg";
                                    } catch (Exception e) {
                                        log.warn("No se pudo obtener imagen remota para adjuntar: {}", urlStr);
                                    }
                                }

                                if (base64Content != null) {
                                    Map<String, String> imgFile = new HashMap<>();
                                    String codeStr = (req.getCodigo() != null && !req.getCodigo().isEmpty()) ? req.getCodigo() : ("REQ_" + req.getId());
                                    imgFile.put("name", codeStr + "_Imagen_" + imgIdx + "." + ext);
                                    imgFile.put("type", "text/plain");
                                    imgFile.put("content", base64Content);
                                    filesList.add(imgFile);
                                }
                            }
                            imgIdx++;
                        }
                    }
                }
            }

            if (!filesList.isEmpty()) {
                payload.put("files", filesList);
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept-Version", "v1");
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            log.info("Enviando petición HTTP a sv2-commons con {} archivos adjuntos...", filesList.size());
            String response = restTemplate.postForObject(COMMONS_SERVICE_URL, request, String.class);
            log.info("✅ Respuesta de commons: {}", response);

        } catch (Exception e) {
            log.error("❌ Error inesperado al procesar y enviar el correo a través de commons", e);
            throw new RuntimeException("Error al comunicarse con sv2-commons para el envío de correo", e);
        }
    }

    private String getLogoBase64() {
        try (InputStream is = getClass().getResourceAsStream("/asmet_logo.png")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo asmet_logo.png desde el classpath: {}", e.getMessage());
        }
        return null;
    }

    private String buildHtmlBody(CorreoSolicitudDTO datos) {
        SolicitudResponseDTO sol = datos.getSolicitudCompleta();
        String logoBase64 = getLogoBase64();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #333333; background-color: #f4f6f8; margin: 0; padding: 20px;\">");
        html.append("<div style=\"max-width: 800px; margin: 0 auto; background-color: #ffffff; border: 1px solid #dcdcdc; border-radius: 6px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);\">");

        // HEADER LOGO + DOCUMENT TITLE
        html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 15px;\">");
        html.append("<tr>");
        html.append("<td style=\"width: 40%; vertical-align: middle;\">");
        if (logoBase64 != null) {
            html.append("<img src=\"").append(logoBase64).append("\" alt=\"Asmet Salud Logo\" style=\"height: 55px; width: auto;\">");
        } else {
            html.append("<h2 style=\"color: #3bafb6; margin: 0;\">ASMET SALUD EPS</h2>");
        }
        html.append("</td>");
        html.append("<td style=\"width: 60%; text-align: right; vertical-align: middle; font-size: 10px; color: #555555; font-weight: bold; line-height: 1.4;\">");
        html.append("MACROPROCESO TRANSFORMACIÓN DIGITAL E INFORMACIÓN<br>");
        html.append("PROCESO DESARROLLO TECNOLÓGICO<br>");
        html.append("REQUERIMIENTO DE DESARROLLO<br>");
        html.append("<span style=\"color: #3bafb6;\">TDI-DT-F-08 Ver 02</span>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        // TITLE BANNER
        html.append("<div style=\"background-color: #3bafb6; color: #ffffff; text-align: center; font-weight: bold; font-size: 14px; padding: 10px; border-radius: 4px; margin-bottom: 20px; letter-spacing: 0.5px;\">");
        html.append("REQUERIMIENTO DE DESARROLLO");
        html.append("</div>");

        if (sol != null) {
            // INFORMACIÓN GENERAL
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">");
            html.append("<tr><th colspan=\"2\" style=\"background-color: #3bafb6; color: #ffffff; text-align: center; padding: 8px; font-size: 12px; border: 1px solid #3bafb6;\">INFORMACIÓN GENERAL</th></tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px; width: 50%;\"><b>Solicitud del proceso:</b> ").append(val(sol.getSolicitudProceso())).append("</td>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px; width: 50%;\"><b>Código Solicitud:</b> ").append(val(sol.getCodigo())).append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px;\"><b>Proceso solicitante:</b> ").append(val(sol.getSolicitudProceso())).append("</td>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px;\"><b>Fecha de solicitud:</b> ").append(sol.getFechaCreacion() != null ? sol.getFechaCreacion().toString() : "-").append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px;\"><b>Solicitado por:</b> ").append(val(sol.getEmpleadoNombre())).append(" (").append(val(sol.getEmpleadoCargo())).append(")</td>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px;\"><b>Correo / Sede:</b> ").append(val(sol.getEmpleadoCorreo())).append(" / ").append(val(sol.getEmpleadoSede())).append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px;\"><b>Tipo de Solicitud:</b> ").append(sol.getTipoSolicitud() != null ? val(sol.getTipoSolicitud().getNombre()) : "-").append("</td>");
            html.append("<td style=\"border: 1px solid #dddddd; padding: 8px;\"><b>Prioridad / Estado:</b> ").append(val(sol.getPrioridad())).append(" / ").append(sol.getEstado() != null ? val(sol.getEstado().getNombre()) : "-").append("</td>");
            html.append("</tr>");
            html.append("</table>");

            // 1. IMPACTO
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">");
            html.append("<tr><th style=\"background-color: #f0f4f8; color: #333333; text-align: left; padding: 8px; font-size: 12px; border: 1px solid #cccccc;\">1. IMPACTO DEL REQUERIMIENTO</th></tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #cccccc; padding: 12px; background-color: #fafafa; line-height: 1.5;\">");
            html.append("<b>Impacto especificado:</b><br>").append(escapeHtml(sol.getImpacto())).append("<br><br>");
            if (sol.getObservaciones() != null && !sol.getObservaciones().trim().isEmpty()) {
                html.append("<b>Observaciones adicionales:</b><br>").append(escapeHtml(sol.getObservaciones()));
            }
            html.append("</td></tr></table>");

            // 2. ESTIMACIÓN DE RECURSOS
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">");
            html.append("<tr><th colspan=\"5\" style=\"background-color: #3bafb6; color: #ffffff; text-align: center; padding: 8px; font-size: 12px; border: 1px solid #3bafb6;\">2. ESTIMACIÓN DE RECURSOS (PROYECTO MÓDULO DE COACTIVOS Y ARRESTOS)</th></tr>");
            html.append("<tr style=\"background-color: #f0f0f0; font-weight: bold; text-align: center;\">");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Equipo Desarrollador</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Nº Personas</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Salario por persona</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Duración (meses)</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Total por Integrante</td>");
            html.append("</tr>");
            String[][] roles = {
                {"Ing. Desarrollador", "0", "$ 0", "0", "$ 0"},
                {"Analista pruebas", "0", "$ 0", "0", "$ 0"},
                {"Prof. Funcional", "0", "$ 0", "0", "$ 0"},
                {"Líder Proyecto", "0", "$ 0", "0", "$ 0"},
                {"Especialista proceso", "0", "$ 0", "0", "$ 0"},
                {"Soporte", "0", "$ 0", "0", "$ 0"}
            };
            for (String[] r : roles) {
                html.append("<tr>");
                html.append("<td style=\"border: 1px solid #ddd; padding: 5px;\">").append(r[0]).append("</td>");
                html.append("<td style=\"border: 1px solid #ddd; padding: 5px; text-align: center;\">").append(r[1]).append("</td>");
                html.append("<td style=\"border: 1px solid #ddd; padding: 5px;\">").append(r[2]).append("</td>");
                html.append("<td style=\"border: 1px solid #ddd; padding: 5px; text-align: center;\">").append(r[3]).append("</td>");
                html.append("<td style=\"border: 1px solid #ddd; padding: 5px;\">").append(r[4]).append("</td>");
                html.append("</tr>");
            }
            html.append("<tr style=\"background-color: #e6f7f8; font-weight: bold;\">");
            html.append("<td colspan=\"4\" style=\"border: 1px solid #3bafb6; padding: 6px; text-align: right;\">TOTAL ESTIMADO:</td>");
            html.append("<td style=\"border: 1px solid #3bafb6; padding: 6px;\">$ 0</td>");
            html.append("</tr>");
            html.append("</table>");

            // 3. REQUERIMIENTOS FUNCIONALES
            List<RequerimientoResponseDTO> funcionales = sol.getRequerimientos() != null ?
                    sol.getRequerimientos().stream().filter(r -> r.getTipoRequerimiento() != null && r.getTipoRequerimiento() == 0).collect(Collectors.toList()) : List.of();
            appendRequerimientosHtmlTable(html, "3. REQUERIMIENTOS FUNCIONALES", funcionales);

            // 4. REQUISITOS NO FUNCIONALES
            List<RequerimientoResponseDTO> noFuncionales = sol.getRequerimientos() != null ?
                    sol.getRequerimientos().stream().filter(r -> r.getTipoRequerimiento() != null && r.getTipoRequerimiento() == 1).collect(Collectors.toList()) : List.of();
            appendRequerimientosHtmlTable(html, "4. REQUISITOS NO FUNCIONALES", noFuncionales);

            // 5. REQUISITOS DE SEGURIDAD DE LA INFORMACIÓN
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">");
            html.append("<tr><th style=\"background-color: #f0f4f8; color: #333333; text-align: left; padding: 8px; font-size: 12px; border: 1px solid #cccccc;\">5. REQUISITOS DE SEGURIDAD DE LA INFORMACIÓN</th></tr>");
            html.append("<tr><td style=\"border: 1px solid #cccccc; padding: 12px; background-color: #fafafa; line-height: 1.6;\">");
            html.append("<ul style=\"margin: 0; padding-left: 18px;\">");
            html.append("<li><b>Autentificar adecuadamente:</b> La información confidencial y los sistemas informáticos sólo deben ser accesibles por las personas con los roles y permisos definidos.</li>");
            html.append("<li><b>No utilizar campos ocultos:</b> Evitar almacenar información sensible en campos ocultos que permitan manipular el funcionamiento interno.</li>");
            html.append("<li><b>Comprobar las entradas:</b> Verificar y controlar que los datos introducidos estén dentro del rango y formato válido.</li>");
            html.append("<li><b>Valores límite de salida:</b> Controlar la salida de los métodos para que el resultado esté dentro de parámetros definidos.</li>");
            html.append("<li><b>Formato de salida:</b> No alterar los formatos de salida para evitar errores asociados con manejo de buffer.</li>");
            html.append("<li><b>Controles de seguridad:</b> Declarar como privados o finales los métodos que realicen controles de seguridad.</li>");
            html.append("<li><b>Pruebas seguras:</b> Evitar el uso de datos reales de carácter personal en pruebas anteriores a la implantación.</li>");
            html.append("<li><b>Control de acceso indirecto:</b> Utilizar mapas de referencias indirectas en lugar de exponer URLs o rutas directas a objetos del servidor.</li>");
            html.append("<li><b>Código seguro:</b> Evitar generar código ejecutable a partir de valores ingresados por el usuario.</li>");
            html.append("<li><b>Control de salidas:</b> Garantizar que los datos obtenidos correspondan exactamente a lo solicitado en los requerimientos.</li>");
            html.append("</ul>");
            html.append("</td></tr></table>");

            // CLASIFICACIÓN DE LA INFORMACIÓN Y CONTROL DE CAMBIOS
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">");
            html.append("<tr><th colspan=\"3\" style=\"background-color: #3bafb6; color: #ffffff; text-align: center; padding: 8px; font-size: 12px; border: 1px solid #3bafb6;\">CLASIFICACIÓN DE LA INFORMACIÓN Y CONTROL DE CAMBIOS</th></tr>");
            html.append("<tr style=\"background-color: #f0f0f0; font-weight: bold; text-align: center;\">");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; width: 15%;\">VERSIÓN</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; width: 25%;\">FECHA</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; width: 60%;\">DESCRIPCIÓN DEL CAMBIO</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; text-align: center;\">1</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; text-align: center;\">3/12/2024</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Este documento reemplaza el FORMATO SGI-DT-F-03 en su versión 7 por cambio de estructura.</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; text-align: center;\">2</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px; text-align: center;\">15/08/2025</td>");
            html.append("<td style=\"border: 1px solid #ddd; padding: 6px;\">Se actualiza con la inclusión de la metodología para el cálculo del Retorno sobre la Inversión para la viabilidad del proyecto.</td>");
            html.append("</tr>");
            html.append("</table>");

            // CONTROL DE CALIDAD (FOOTER TABLE)
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-top: 25px; font-size: 10px;\">");
            html.append("<tr>");
            html.append("<td style=\"border: 1px solid #ccc; padding: 6px; width: 33%;\"><b>Elaborado por:</b> Profesional Funcional de Desarrollador</td>");
            html.append("<td style=\"border: 1px solid #ccc; padding: 6px; width: 33%;\"><b>Revisado por:</b> Prof. Innovación y Optimización de Procesos</td>");
            html.append("<td style=\"border: 1px solid #ccc; padding: 6px; width: 34%;\"><b>Aprobado por:</b> Director de Transformación Digital</td>");
            html.append("</tr>");
            html.append("<tr style=\"background-color: #f0f0f0;\">");
            html.append("<td colspan=\"2\" style=\"border: 1px solid #ccc; padding: 6px; font-weight: bold; text-align: center;\">SISTEMA DE GESTIÓN DE CALIDAD</td>");
            html.append("<td style=\"border: 1px solid #ccc; padding: 6px; text-align: center;\">Aprobado en agosto de 2025</td>");
            html.append("</tr>");
            html.append("</table>");
        } else {
            html.append("<p>Notificación para solicitud <b>").append(datos.getNumeroSolicitud()).append("</b> de <b>").append(datos.getNombreSolicitante()).append("</b>.</p>");
        }

        html.append("<br><div style=\"text-align: center; font-size: 11px; color: #777777; font-style: italic; margin-top: 15px;\">");
        html.append("Este es un mensaje automático generado por el Sistema Nexus - ASMET SALUD EPS. Por favor no responda a este correo.");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    private void appendRequerimientosHtmlTable(StringBuilder html, String title, List<RequerimientoResponseDTO> reqs) {
        html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 10px;\">");
        html.append("<tr><th style=\"background-color: #3bafb6; color: #ffffff; text-align: center; padding: 8px; font-size: 12px; border: 1px solid #3bafb6; text-transform: uppercase;\">").append(title).append("</th></tr>");
        html.append("</table>");

        if (reqs == null || reqs.isEmpty()) {
            html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">");
            html.append("<tr><td style=\"border: 1px solid #ddd; padding: 10px; text-align: center; color: #777; background-color: #fafafa;\">No hay registros para esta sección.</td></tr>");
            html.append("</table>");
        } else {
            for (RequerimientoResponseDTO req : reqs) {
                String reqCodeStr = (req.getCodigo() != null && !req.getCodigo().isEmpty()) ? req.getCodigo() : ("REQ_" + req.getId());

                html.append("<table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px; border: 1px solid #3bafb6;\">");
                
                // Cabecera del cuadro del requerimiento (Teal)
                html.append("<tr style=\"background-color: #3bafb6; color: #ffffff;\">");
                html.append("<td style=\"padding: 7px 10px; font-weight: bold; font-size: 12px; width: 30%;\">Requerimiento N°: ").append(escapeHtml(reqCodeStr)).append("</td>");
                html.append("<td style=\"padding: 7px 10px; font-weight: bold; font-size: 11px; text-align: right; width: 70%;\">Cargo Impactado: ").append(escapeHtml(req.getCargoImpactado())).append("</td>");
                html.append("</tr>");

                // Fila Objetivo
                html.append("<tr>");
                html.append("<td colspan=\"2\" style=\"border-bottom: 1px solid #e0e0e0; padding: 8px 10px; background-color: #ffffff;\">");
                html.append("<b style=\"color: #00767c;\">Objetivo de la solicitud:</b><br>").append(escapeHtml(req.getObjetivo()));
                html.append("</td>");
                html.append("</tr>");

                // Fila Detalle su necesidad
                html.append("<tr>");
                html.append("<td colspan=\"2\" style=\"border-bottom: 1px solid #e0e0e0; padding: 8px 10px; background-color: #fafafa;\">");
                html.append("<b style=\"color: #00767c;\">Detalle su necesidad:</b><br>").append(escapeHtml(req.getDetalle()));
                html.append("</td>");
                html.append("</tr>");

                // Fila de imágenes específicas (directamente dentro del cuadro de este requerimiento)
                if (req.getImagenesUrls() != null && !req.getImagenesUrls().isEmpty()) {
                    html.append("<tr>");
                    html.append("<td colspan=\"2\" style=\"padding: 12px; background-color: #ffffff;\">");
                    html.append("<div style=\"font-weight: bold; color: #3bafb6; font-size: 11px; margin-bottom: 10px;\">");
                    html.append("📷 Imágenes descriptivas asociadas al ").append(escapeHtml(reqCodeStr)).append(":");
                    html.append("</div>");

                    int idx = 1;
                    int total = req.getImagenesUrls().size();
                    for (ImagenDTO imgDto : req.getImagenesUrls()) {
                        String src = imgDto.getUrl();
                        if (src != null && !src.trim().isEmpty()) {
                            html.append("<div style=\"margin-bottom: 12px; background-color: #f9fbfd; border: 1px solid #cbd5e1; border-radius: 6px; padding: 10px; text-align: center;\">");
                            html.append("<div style=\"font-size: 10px; color: #475569; font-weight: bold; margin-bottom: 6px; text-align: left;\">");
                            html.append("Imagen ").append(idx).append(" de ").append(total).append(" (").append(escapeHtml(reqCodeStr)).append(")");
                            html.append("</div>");
                            html.append("<img src=\"").append(src).append("\" style=\"max-width: 100%; max-height: 450px; display: block; margin: 0 auto; border: 1px solid #94a3b8; border-radius: 4px;\">");
                            html.append("</div>");
                        }
                        idx++;
                    }

                    html.append("</td>");
                    html.append("</tr>");
                }

                html.append("</table>");
            }
        }
    }

    private String val(String s) {
        return (s != null && !s.trim().isEmpty()) ? escapeHtml(s) : "-";
    }

    private String escapeHtml(String text) {
        if (text == null) return "-";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;")
                   .replace("\n", "<br>");
    }
}

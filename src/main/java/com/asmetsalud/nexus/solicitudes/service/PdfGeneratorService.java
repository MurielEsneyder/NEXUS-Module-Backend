package com.asmetsalud.nexus.solicitudes.service;

import com.asmetsalud.nexus.solicitudes.dto.RequerimientoResponseDTO;
import com.asmetsalud.nexus.solicitudes.dto.SolicitudResponseDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPCell;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarPdfSolicitud(SolicitudResponseDTO solicitud) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            document.setMargins(40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ============================================================
            // TÍTULO
            // ============================================================
            Paragraph title = new Paragraph("SOLICITUD DE DESARROLLO",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // ============================================================
            // DATOS DE LA SOLICITUD
            // ============================================================
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Sección 1: Información General
            Paragraph section1 = new Paragraph("INFORMACIÓN GENERAL", sectionFont);
            section1.setAlignment(Element.ALIGN_LEFT);
            document.add(section1);
            document.add(new Paragraph(" "));

            addField(document, "Código:", solicitud.getCodigo(), boldFont, normalFont);
            addField(document, "Fecha de creación:", solicitud.getFechaCreacion() != null ?
                            solicitud.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
                    boldFont, normalFont);
            addField(document, "Solicitante:", solicitud.getEmpleadoNombre(), boldFont, normalFont);
            addField(document, "Cargo:", solicitud.getEmpleadoCargo(), boldFont, normalFont);
            addField(document, "Sede:", solicitud.getEmpleadoSede(), boldFont, normalFont);
            addField(document, "Proceso:", solicitud.getSolicitudProceso(), boldFont, normalFont);
            addField(document, "Tipo:", solicitud.getTipoSolicitud() != null ?
                    solicitud.getTipoSolicitud().getNombre() : "", boldFont, normalFont);
            addField(document, "Estado:", solicitud.getEstado() != null ?
                    solicitud.getEstado().getNombre() : "", boldFont, normalFont);

            document.add(new Paragraph(" "));

            // Sección 2: Impacto
            Paragraph section2 = new Paragraph("IMPACTO", sectionFont);
            section2.setAlignment(Element.ALIGN_LEFT);
            document.add(section2);
            document.add(new Paragraph(" "));

            Paragraph impacto = new Paragraph(solicitud.getImpacto() != null ? solicitud.getImpacto() : "",
                    normalFont);
            impacto.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(impacto);
            document.add(new Paragraph(" "));

            // Sección 3: Observaciones
            if (solicitud.getObservaciones() != null && !solicitud.getObservaciones().isEmpty()) {
                Paragraph section3 = new Paragraph("OBSERVACIONES", sectionFont);
                section3.setAlignment(Element.ALIGN_LEFT);
                document.add(section3);
                document.add(new Paragraph(" "));

                Paragraph obs = new Paragraph(solicitud.getObservaciones(), normalFont);
                obs.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(obs);
                document.add(new Paragraph(" "));
            }

            // Sección 4: Requerimientos
            if (solicitud.getRequerimientos() != null && !solicitud.getRequerimientos().isEmpty()) {
                Paragraph section4 = new Paragraph("REQUERIMIENTOS", sectionFont);
                section4.setAlignment(Element.ALIGN_LEFT);
                document.add(section4);
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{15, 20, 40, 25});

                // Encabezados
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                table.addCell(createCell("Código", headerFont));
                table.addCell(createCell("Tipo", headerFont));
                table.addCell(createCell("Objetivo", headerFont));
                table.addCell(createCell("Cargo Impactado", headerFont));

                // Datos
                for (RequerimientoResponseDTO req : solicitud.getRequerimientos()) {
                    table.addCell(createCell(req.getCodigo(), normalFont));
                    table.addCell(createCell(req.getTipoRequerimiento() == 0 ? "Funcional" : "No Funcional", normalFont));
                    table.addCell(createCell(req.getObjetivo(), normalFont));
                    table.addCell(createCell(req.getCargoImpactado() != null ? req.getCargoImpactado() : "N/A", normalFont));
                }

                document.add(table);
            }

            // ============================================================
            // PIE DE PÁGINA
            // ============================================================
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Documento generado automáticamente por el sistema HyL Sparta - ASMET SALUD EPS",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    private void addField(Document document, String label, String value, Font boldFont, Font normalFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", boldFont));
        p.add(new Chunk(value != null ? value : "", normalFont));
        document.add(p);
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(4);
        cell.setBorderWidth(0.5f);
        return cell;
    }
}
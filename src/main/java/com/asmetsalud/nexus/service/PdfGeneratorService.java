package com.asmetsalud.nexus.service;

import com.asmetsalud.nexus.dto.RequerimientoResponseDTO;
import com.asmetsalud.nexus.dto.SolicitudResponseDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que genera el PDF de una solicitud de desarrollo replicando
 * fielmente el diseño del frontend (jsPDF): cabecera teal, secciones con
 * encabezados grises, tablas de requerimientos y sección de seguridad.
 */
@Service
public class PdfGeneratorService {

    // ─── Colores (mismos que el frontend) ────────────────────────────────────
    private static final Color COLOR_TEAL        = new Color(59, 175, 182);
    private static final Color COLOR_HEADER_GRAY = new Color(240, 240, 240);
    private static final Color COLOR_BORDER      = new Color(200, 200, 200);
    private static final Color COLOR_BLACK       = new Color(0, 0, 0);
    private static final Color COLOR_WHITE       = new Color(255, 255, 255);
    private static final Color COLOR_DARK_GRAY   = new Color(80, 80, 80);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
            java.util.Locale.forLanguageTag("es-CO"));

    // ─── Fuentes ─────────────────────────────────────────────────────────────
    private Font fontHeader()      { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, COLOR_WHITE); }
    private Font fontHeaderSmall() { return FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_WHITE); }
    private Font fontSection()     { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  10, COLOR_BLACK); }
    private Font fontNormal()      { return FontFactory.getFont(FontFactory.HELVETICA,       10, COLOR_BLACK); }
    private Font fontSmall()       { return FontFactory.getFont(FontFactory.HELVETICA,        9, COLOR_DARK_GRAY); }
    private Font fontSmallBold()   { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, COLOR_DARK_GRAY); }
    private Font fontFooter()      { return FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, COLOR_DARK_GRAY); }

    // =========================================================================
    // MÉTODO PRINCIPAL
    // =========================================================================
    public byte[] generarPdfSolicitud(SolicitudResponseDTO solicitud) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);
            document.setMargins(30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            PdfContentByte cb = writer.getDirectContent();

            // ── 1. CABECERA ──────────────────────────────────────────────────
            addHeader(document, cb, solicitud);

            // ── 2. INFORMACIÓN DEL COLABORADOR ───────────────────────────────
            addSectionTable(document, "INFORMACIÓN DEL COLABORADOR",
                    new String[][] {
                            { "Nombre: " + val(solicitud.getEmpleadoNombre()),
                              "Correo: "  + val(solicitud.getEmpleadoCorreo()) },
                            { "Cargo: "   + val(solicitud.getEmpleadoCargo()),
                              "Sede: "    + val(solicitud.getEmpleadoSede()) }
                    });

            // ── 3. INFORMACIÓN DE LA SOLICITUD ───────────────────────────────
            String proceso       = val(solicitud.getSolicitudProceso());
            String tipo          = solicitud.getTipoSolicitud()  != null ? solicitud.getTipoSolicitud().getNombre()  : "-";
            String estado        = solicitud.getEstado()         != null ? solicitud.getEstado().getNombre()         : "Pendiente";
            String prioridad     = val(solicitud.getPrioridad());

            addSectionTable(document, "INFORMACIÓN DE LA SOLICITUD",
                    new String[][] {
                            { "Solicitud del Proceso: " + proceso,  "Tipo de Solicitud: " + tipo },
                            { "Prioridad: " + prioridad,            "Estado: " + estado },
                            { "Coordinador: No asignado",           "Funcional Asignado: No asignado" }
                    });

            // ── 4. IMPACTO DEL REQUERIMIENTO ─────────────────────────────────
            String impacto = solicitud.getImpacto() != null && !solicitud.getImpacto().trim().isEmpty()
                    ? solicitud.getImpacto()
                    : "No se especificó impacto.";
            addSectionTextBlock(document, "IMPACTO DEL REQUERIMIENTO", impacto, fontNormal());

            // ── 5. OBSERVACIONES ─────────────────────────────────────────────
            if (solicitud.getObservaciones() != null && !solicitud.getObservaciones().trim().isEmpty()) {
                addSectionTextBlock(document, "OBSERVACIONES", solicitud.getObservaciones(), fontNormal());
            }

            // ── 6. REQUERIMIENTOS FUNCIONALES ────────────────────────────────
            List<RequerimientoResponseDTO> funcionales = (solicitud.getRequerimientos() != null)
                    ? solicitud.getRequerimientos().stream()
                        .filter(r -> r.getTipoRequerimiento() != null && r.getTipoRequerimiento() == 0)
                        .collect(Collectors.toList())
                    : List.of();
            addRequerimientosTable(document, "REQUERIMIENTOS FUNCIONALES", funcionales);

            // ── 7. REQUERIMIENTOS NO FUNCIONALES ─────────────────────────────
            List<RequerimientoResponseDTO> noFuncionales = (solicitud.getRequerimientos() != null)
                    ? solicitud.getRequerimientos().stream()
                        .filter(r -> r.getTipoRequerimiento() != null && r.getTipoRequerimiento() == 1)
                        .collect(Collectors.toList())
                    : List.of();
            addRequerimientosTable(document, "REQUERIMIENTOS NO FUNCIONALES", noFuncionales);

            // ── 8. REQUISITOS DE SEGURIDAD ───────────────────────────────────
            String seguridad =
                    "• Autentificación adecuada y control de accesos.\n" +
                    "• No uso de campos ocultos para información sensible.\n" +
                    "• Comprobación y validación de las entradas.\n" +
                    "• Control de límites de valores de salida.\n" +
                    "• Asegurar métodos de controles de seguridad privados/finales.\n" +
                    "• Evitar uso de datos reales de carácter personal en pruebas.";
            addSectionTextBlock(document, "REQUISITOS DE SEGURIDAD", seguridad, fontSmall());

            // ── 9. PIE DE PÁGINA ─────────────────────────────────────────────
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph(
                    "Documento generado automáticamente por el sistema HyL Sparta - ASMET SALUD EPS",
                    fontFooter());
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // HELPERS DE CONSTRUCCIÓN
    // =========================================================================

    /**
     * Dibuja la cabecera teal con el título a la izquierda y
     * número de solicitud + fecha a la derecha, igual que el frontend.
     */
    private void addHeader(Document document, PdfContentByte cb, SolicitudResponseDTO solicitud)
            throws DocumentException {

        // Tabla de una sola fila con fondo teal
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{65, 35});
        header.setSpacingAfter(8);

        // Celda izquierda: título
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBackgroundColor(COLOR_TEAL);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(8);
        leftCell.addElement(new Phrase("ASMET SALUD - REQUERIMIENTO DE DESARROLLO", fontHeader()));
        header.addCell(leftCell);

        // Celda derecha: número y fecha
        String fechaStr = solicitud.getFechaCreacion() != null
                ? solicitud.getFechaCreacion().format(DATE_FMT)
                : "No registrada";
        String rightText = "Solicitud: " + val(solicitud.getCodigo()) + "  |  Fecha: " + fechaStr;

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(COLOR_TEAL);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(8);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(new Phrase(rightText, fontHeaderSmall()));
        header.addCell(rightCell);

        document.add(header);
    }

    /**
     * Sección con encabezado gris de dos columnas (misma estructura que
     * los bloques de información del frontend).
     */
    private void addSectionTable(Document document, String title, String[][] rows)
            throws DocumentException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(6);

        // Fila de encabezado (colspan 2)
        PdfPCell headCell = new PdfPCell(new Phrase(title, fontSection()));
        headCell.setColspan(2);
        headCell.setBackgroundColor(COLOR_HEADER_GRAY);
        headCell.setBorder(Rectangle.NO_BORDER);
        headCell.setPadding(5);
        table.addCell(headCell);

        // Filas de datos
        for (String[] row : rows) {
            for (String cell : row) {
                PdfPCell c = new PdfPCell(new Phrase(cell, fontNormal()));
                c.setBorder(Rectangle.NO_BORDER);
                c.setPadding(4);
                table.addCell(c);
            }
        }

        document.add(table);
    }

    /**
     * Sección con encabezado gris y un bloque de texto libre debajo.
     */
    private void addSectionTextBlock(Document document, String title, String body, Font bodyFont)
            throws DocumentException {

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(6);

        // Encabezado
        PdfPCell headCell = new PdfPCell(new Phrase(title, fontSection()));
        headCell.setBackgroundColor(COLOR_HEADER_GRAY);
        headCell.setBorder(Rectangle.NO_BORDER);
        headCell.setPadding(5);
        table.addCell(headCell);

        // Cuerpo
        PdfPCell bodyCell = new PdfPCell(new Phrase(body, bodyFont));
        bodyCell.setBorder(Rectangle.NO_BORDER);
        bodyCell.setPadding(4);
        table.addCell(bodyCell);

        document.add(table);
    }

    /**
     * Tabla de requerimientos con columnas: ID | Objetivo / Cargo | Detalles.
     * Replicada fielmente del frontend.
     */
    private void addRequerimientosTable(Document document, String title,
                                        List<RequerimientoResponseDTO> reqs)
            throws DocumentException {

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{12, 44, 44});
        table.setSpacingBefore(4);
        table.setSpacingAfter(6);

        // ── Fila de título (colspan 3, fondo gris, sin borde) ────────────────
        PdfPCell titleCell = new PdfPCell(new Phrase(title, fontSection()));
        titleCell.setColspan(3);
        titleCell.setBackgroundColor(COLOR_HEADER_GRAY);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(5);
        table.addCell(titleCell);

        // ── Fila de columnas ─────────────────────────────────────────────────
        Font colFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_BLACK);
        for (String col : new String[]{"ID", "Objetivo / Cargo Impactado", "Detalles"}) {
            PdfPCell c = new PdfPCell(new Phrase(col, colFont));
            c.setBackgroundColor(COLOR_WHITE);
            c.setBorderColor(COLOR_BORDER);
            c.setBorderWidth(0.5f);
            c.setPadding(4);
            table.addCell(c);
        }

        // ── Filas de datos ────────────────────────────────────────────────────
        if (reqs == null || reqs.isEmpty()) {
            String msgEmpty = "No hay " + title.toLowerCase() + " registrados.";
            PdfPCell empty = new PdfPCell(new Phrase(msgEmpty, fontNormal()));
            empty.setColspan(3);
            empty.setBorderColor(COLOR_BORDER);
            empty.setBorderWidth(0.5f);
            empty.setPadding(4);
            table.addCell(empty);
        } else {
            for (RequerimientoResponseDTO req : reqs) {
                // ID
                PdfPCell idCell = new PdfPCell(new Phrase(val(req.getCodigo()), fontNormal()));
                idCell.setBorderColor(COLOR_BORDER);
                idCell.setBorderWidth(0.5f);
                idCell.setPadding(4);
                table.addCell(idCell);

                // Objetivo + Cargo Impactado
                String objText = "Objetivo: " + val(req.getObjetivo()) +
                        "\nCargo Impactado: " + val(req.getCargoImpactado());
                PdfPCell objCell = new PdfPCell(new Phrase(objText, fontNormal()));
                objCell.setBorderColor(COLOR_BORDER);
                objCell.setBorderWidth(0.5f);
                objCell.setPadding(4);
                table.addCell(objCell);

                // Detalle
                PdfPCell detCell = new PdfPCell(new Phrase("Detalle: " + val(req.getDetalle()), fontNormal()));
                detCell.setBorderColor(COLOR_BORDER);
                detCell.setBorderWidth(0.5f);
                detCell.setPadding(4);
                table.addCell(detCell);
            }
        }

        document.add(table);
    }

    /** Devuelve el valor o "-" si es nulo/vacío. */
    private String val(String s) {
        return (s != null && !s.trim().isEmpty()) ? s : "-";
    }
}

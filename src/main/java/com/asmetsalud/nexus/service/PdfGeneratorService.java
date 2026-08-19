package com.asmetsalud.nexus.service;

import com.asmetsalud.nexus.dto.ImagenDTO;
import com.asmetsalud.nexus.dto.RequerimientoResponseDTO;
import com.asmetsalud.nexus.dto.SolicitudResponseDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que genera el PDF oficial de una solicitud de desarrollo (Formato TDI-DT-F-08 Ver 02)
 * replicando exactamente la misma estructura, colores, cabecera institucional con logo,
 * tablas y ubicación de imágenes debajo de cada requerimiento que el correo electrónico.
 */
@Service
@Slf4j
public class PdfGeneratorService {

    // ─── Colores Institucionales ──────────────────────────────────────────────
    private static final Color COLOR_TEAL        = new Color(59, 175, 182);  // #3bafb6
    private static final Color COLOR_DARK_TEAL   = new Color(0, 118, 124);   // #00767c
    private static final Color COLOR_LIGHT_TEAL  = new Color(230, 247, 248); // #e6f7f8
    private static final Color COLOR_HEADER_GRAY = new Color(240, 244, 248); // #f0f4f8
    private static final Color COLOR_BORDER      = new Color(220, 220, 220); // #dcdcdc
    private static final Color COLOR_BLACK       = new Color(51, 51, 51);    // #333333
    private static final Color COLOR_WHITE       = new Color(255, 255, 255);
    private static final Color COLOR_DARK_GRAY   = new Color(80, 80, 80);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy",
            java.util.Locale.forLanguageTag("es-CO"));

    // ─── Fuentes ─────────────────────────────────────────────────────────────
    private Font fontHeaderTitle() { return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_WHITE); }
    private Font fontDocHeader()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8, COLOR_DARK_GRAY); }
    private Font fontDocHeaderSub() { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8, COLOR_TEAL); }
    private Font fontTableHead()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  9, COLOR_WHITE); }
    private Font fontTableHeadDark(){ return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  9, COLOR_BLACK); }
    private Font fontLabelBold()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8, COLOR_DARK_TEAL); }
    private Font fontNormal()       { return FontFactory.getFont(FontFactory.HELVETICA,       8, COLOR_BLACK); }
    private Font fontSmall()        { return FontFactory.getFont(FontFactory.HELVETICA,       7, COLOR_DARK_GRAY); }
    private Font fontSmallBold()    { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  7, COLOR_DARK_GRAY); }
    private Font fontFooter()       { return FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, COLOR_DARK_GRAY); }

    // =========================================================================
    // MÉTODO PRINCIPAL DE GENERACIÓN
    // =========================================================================
    public byte[] generarPdfSolicitud(SolicitudResponseDTO solicitud) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);
            document.setMargins(25, 25, 25, 25);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // 1. CABECERA INSTITUCIONAL CON LOGO Y FORMATO TDI-DT-F-08
            addHeaderTable(document, solicitud);

            // 2. BANNER DE TÍTULO PRINCIPAL
            addTitleBanner(document);

            if (solicitud != null) {
                // 3. INFORMACIÓN GENERAL
                addInformacionGeneral(document, solicitud);

                // 4. IMPACTO DEL REQUERIMIENTO
                addImpactoSection(document, solicitud);

                // 5. ESTIMACIÓN DE RECURSOS
                addEstimacionRecursosSection(document);

                // 6. REQUERIMIENTOS FUNCIONALES (Cajas por requerimiento con sus imágenes embebidas)
                List<RequerimientoResponseDTO> funcionales = (solicitud.getRequerimientos() != null)
                        ? solicitud.getRequerimientos().stream()
                            .filter(r -> r.getTipoRequerimiento() != null && r.getTipoRequerimiento() == 0)
                            .collect(Collectors.toList())
                        : List.of();
                addRequerimientosBoxes(document, "3. REQUERIMIENTOS FUNCIONALES", funcionales);

                // 7. REQUISITOS NO FUNCIONALES (Cajas por requerimiento con sus imágenes embebidas)
                List<RequerimientoResponseDTO> noFuncionales = (solicitud.getRequerimientos() != null)
                        ? solicitud.getRequerimientos().stream()
                            .filter(r -> r.getTipoRequerimiento() != null && r.getTipoRequerimiento() == 1)
                            .collect(Collectors.toList())
                        : List.of();
                addRequerimientosBoxes(document, "4. REQUISITOS NO FUNCIONALES", noFuncionales);

                // 8. REQUISITOS DE SEGURIDAD DE LA INFORMACIÓN (10 NORMAS)
                addSeguridadSection(document);

                // 9. CLASIFICACIÓN DE LA INFORMACIÓN Y CONTROL DE CAMBIOS
                addControlCambiosSection(document);

                // 10. CONTROL DE CALIDAD (PIE DE PÁGINA DE FIRMAS / EVALUACIÓN)
                addControlCalidadSection(document);

            } else {
                Paragraph p = new Paragraph("No hay datos disponibles para generar la solicitud.", fontNormal());
                document.add(p);
            }

            // Pie de página final
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph(
                    "Este es un documento oficial generado por el Sistema Nexus - ASMET SALUD EPS",
                    fontFooter());
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar el PDF de solicitud", e);
            throw new RuntimeException("Error al generar el PDF de solicitud: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // CONSTRUCCIÓN DE SECCIONES INDIVIDUALES
    // =========================================================================

    private void addHeaderTable(Document document, SolicitudResponseDTO solicitud) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 60});
        table.setSpacingAfter(10);

        // Celda Izquierda: Logo Asmet Salud
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image logo = getLogoImage();
        if (logo != null) {
            leftCell.addElement(logo);
        } else {
            Paragraph titleFallback = new Paragraph("ASMET SALUD EPS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_TEAL));
            leftCell.addElement(titleFallback);
        }
        table.addCell(leftCell);

        // Celda Derecha: Metadatos del Formato TDI-DT-F-08
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph meta = new Paragraph();
        meta.setAlignment(Element.ALIGN_RIGHT);
        meta.add(new Chunk("MACROPROCESO TRANSFORMACIÓN DIGITAL E INFORMACIÓN\n", fontDocHeader()));
        meta.add(new Chunk("PROCESO DESARROLLO TECNOLÓGICO\n", fontDocHeader()));
        meta.add(new Chunk("REQUERIMIENTO DE DESARROLLO\n", fontDocHeader()));
        meta.add(new Chunk("TDI-DT-F-08 Ver 02", fontDocHeaderSub()));
        rightCell.addElement(meta);
        table.addCell(rightCell);

        document.add(table);
    }

    private void addTitleBanner(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);

        PdfPCell cell = new PdfPCell(new Phrase("REQUERIMIENTO DE DESARROLLO", fontHeaderTitle()));
        cell.setBackgroundColor(COLOR_TEAL);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        document.add(table);
    }

    private void addInformacionGeneral(Document document, SolicitudResponseDTO sol) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});
        table.setSpacingAfter(10);

        // Encabezado
        PdfPCell head = new PdfPCell(new Phrase("INFORMACIÓN GENERAL", fontTableHead()));
        head.setColspan(2);
        head.setBackgroundColor(COLOR_TEAL);
        head.setPadding(5);
        head.setHorizontalAlignment(Element.ALIGN_CENTER);
        head.setBorderColor(COLOR_TEAL);
        table.addCell(head);

        String fechaStr = sol.getFechaCreacion() != null ? sol.getFechaCreacion().format(DATE_FMT) : "-";
        String solicitante = val(sol.getEmpleadoNombre()) + " (" + val(sol.getEmpleadoCargo()) + ")";
        String correoSede = val(sol.getEmpleadoCorreo()) + " / " + val(sol.getEmpleadoSede());
        String tipo = sol.getTipoSolicitud() != null ? val(sol.getTipoSolicitud().getNombre()) : "-";
        String estado = sol.getEstado() != null ? val(sol.getEstado().getNombre()) : "-";
        String prioridadEstado = val(sol.getPrioridad()) + " / " + estado;

        addTwoCellRow(table, "Solicitud del proceso: " + val(sol.getSolicitudProceso()), "Código Solicitud: " + val(sol.getCodigo()));
        addTwoCellRow(table, "Proceso solicitante: " + val(sol.getSolicitudProceso()), "Fecha de solicitud: " + fechaStr);
        addTwoCellRow(table, "Solicitado por: " + solicitante, "Correo / Sede: " + correoSede);
        addTwoCellRow(table, "Tipo de Solicitud: " + tipo, "Prioridad / Estado: " + prioridadEstado);

        document.add(table);
    }

    private void addImpactoSection(Document document, SolicitudResponseDTO sol) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);

        PdfPCell head = new PdfPCell(new Phrase("1. IMPACTO DEL REQUERIMIENTO", fontTableHeadDark()));
        head.setBackgroundColor(COLOR_HEADER_GRAY);
        head.setPadding(5);
        head.setBorderColor(COLOR_BORDER);
        table.addCell(head);

        Paragraph body = new Paragraph();
        body.add(new Chunk("Impacto especificado:\n", fontLabelBold()));
        body.add(new Chunk(val(sol.getImpacto()) + "\n\n", fontNormal()));

        if (sol.getObservaciones() != null && !sol.getObservaciones().trim().isEmpty()) {
            body.add(new Chunk("Observaciones adicionales:\n", fontLabelBold()));
            body.add(new Chunk(sol.getObservaciones().trim(), fontNormal()));
        }

        PdfPCell contentCell = new PdfPCell(body);
        contentCell.setPadding(8);
        contentCell.setBorderColor(COLOR_BORDER);
        contentCell.setBackgroundColor(COLOR_WHITE);
        table.addCell(contentCell);

        document.add(table);
    }

    private void addEstimacionRecursosSection(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 20, 15, 20});
        table.setSpacingAfter(10);

        PdfPCell head = new PdfPCell(new Phrase("2. ESTIMACIÓN DE RECURSOS (PROYECTO MÓDULO DE COACTIVOS Y ARRESTOS)", fontTableHead()));
        head.setColspan(5);
        head.setBackgroundColor(COLOR_TEAL);
        head.setPadding(5);
        head.setHorizontalAlignment(Element.ALIGN_CENTER);
        head.setBorderColor(COLOR_TEAL);
        table.addCell(head);

        String[] subheaders = {"Equipo Desarrollador", "Nº Personas", "Salario por persona", "Duración (meses)", "Total por Integrante"};
        for (String sh : subheaders) {
            PdfPCell c = new PdfPCell(new Phrase(sh, fontTableHeadDark()));
            c.setBackgroundColor(COLOR_HEADER_GRAY);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(4);
            c.setBorderColor(COLOR_BORDER);
            table.addCell(c);
        }

        String[][] roles = {
                {"Ing. Desarrollador", "0", "$ 0", "0", "$ 0"},
                {"Analista pruebas", "0", "$ 0", "0", "$ 0"},
                {"Prof. Funcional", "0", "$ 0", "0", "$ 0"},
                {"Líder Proyecto", "0", "$ 0", "0", "$ 0"},
                {"Especialista proceso", "0", "$ 0", "0", "$ 0"},
                {"Soporte", "0", "$ 0", "0", "$ 0"}
        };

        for (String[] r : roles) {
            PdfPCell c0 = new PdfPCell(new Phrase(r[0], fontNormal())); c0.setPadding(4); c0.setBorderColor(COLOR_BORDER); table.addCell(c0);
            PdfPCell c1 = new PdfPCell(new Phrase(r[1], fontNormal())); c1.setHorizontalAlignment(Element.ALIGN_CENTER); c1.setPadding(4); c1.setBorderColor(COLOR_BORDER); table.addCell(c1);
            PdfPCell c2 = new PdfPCell(new Phrase(r[2], fontNormal())); c2.setPadding(4); c2.setBorderColor(COLOR_BORDER); table.addCell(c2);
            PdfPCell c3 = new PdfPCell(new Phrase(r[3], fontNormal())); c3.setHorizontalAlignment(Element.ALIGN_CENTER); c3.setPadding(4); c3.setBorderColor(COLOR_BORDER); table.addCell(c3);
            PdfPCell c4 = new PdfPCell(new Phrase(r[4], fontNormal())); c4.setPadding(4); c4.setBorderColor(COLOR_BORDER); table.addCell(c4);
        }

        PdfPCell totLbl = new PdfPCell(new Phrase("TOTAL ESTIMADO:", fontLabelBold()));
        totLbl.setColspan(4);
        totLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totLbl.setBackgroundColor(COLOR_LIGHT_TEAL);
        totLbl.setPadding(5);
        totLbl.setBorderColor(COLOR_TEAL);
        table.addCell(totLbl);

        PdfPCell totVal = new PdfPCell(new Phrase("$ 0", fontLabelBold()));
        totVal.setBackgroundColor(COLOR_LIGHT_TEAL);
        totVal.setPadding(5);
        totVal.setBorderColor(COLOR_TEAL);
        table.addCell(totVal);

        document.add(table);
    }

    /**
     * Construye las cajas de requerimientos (Funcionales / No Funcionales) con su propio cuadro
     * e imágenes incrustadas DIRECTAMENTE DENTRO del cuadro del requerimiento que le corresponde.
     */
    private void addRequerimientosBoxes(Document document, String sectionTitle, List<RequerimientoResponseDTO> reqs) throws DocumentException {
        // Encabezado de la sección
        PdfPTable sectionTable = new PdfPTable(1);
        sectionTable.setWidthPercentage(100);
        sectionTable.setSpacingAfter(6);

        PdfPCell sectionHead = new PdfPCell(new Phrase(sectionTitle, fontTableHead()));
        sectionHead.setBackgroundColor(COLOR_TEAL);
        sectionHead.setPadding(5);
        sectionHead.setHorizontalAlignment(Element.ALIGN_CENTER);
        sectionHead.setBorderColor(COLOR_TEAL);
        sectionTable.addCell(sectionHead);
        document.add(sectionTable);

        if (reqs == null || reqs.isEmpty()) {
            PdfPTable emptyTable = new PdfPTable(1);
            emptyTable.setWidthPercentage(100);
            emptyTable.setSpacingAfter(10);
            PdfPCell emptyCell = new PdfPCell(new Phrase("No hay registros para esta sección.", fontNormal()));
            emptyCell.setPadding(8);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setBorderColor(COLOR_BORDER);
            emptyTable.addCell(emptyCell);
            document.add(emptyTable);
            return;
        }

        for (RequerimientoResponseDTO req : reqs) {
            String reqCodeStr = (req.getCodigo() != null && !req.getCodigo().isEmpty()) ? req.getCodigo() : ("REQ_" + req.getId());

            PdfPTable boxTable = new PdfPTable(2);
            boxTable.setWidthPercentage(100);
            boxTable.setWidths(new float[]{40, 60});
            boxTable.setSpacingAfter(12);

            // Cabecera del cuadro del requerimiento (Teal)
            PdfPCell h1 = new PdfPCell(new Phrase("Requerimiento N°: " + reqCodeStr, fontTableHead()));
            h1.setBackgroundColor(COLOR_TEAL);
            h1.setPadding(5);
            h1.setBorderColor(COLOR_TEAL);
            boxTable.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("Cargo Impactado: " + val(req.getCargoImpactado()), fontTableHead()));
            h2.setBackgroundColor(COLOR_TEAL);
            h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            h2.setPadding(5);
            h2.setBorderColor(COLOR_TEAL);
            boxTable.addCell(h2);

            // Fila Objetivo
            Paragraph objP = new Paragraph();
            objP.add(new Chunk("Objetivo de la solicitud:\n", fontLabelBold()));
            objP.add(new Chunk(val(req.getObjetivo()), fontNormal()));

            PdfPCell objCell = new PdfPCell(objP);
            objCell.setColspan(2);
            objCell.setPadding(6);
            objCell.setBorderColor(COLOR_BORDER);
            objCell.setBackgroundColor(COLOR_WHITE);
            boxTable.addCell(objCell);

            // Fila Detalle su necesidad
            Paragraph detP = new Paragraph();
            detP.add(new Chunk("Detalle su necesidad:\n", fontLabelBold()));
            detP.add(new Chunk(val(req.getDetalle()), fontNormal()));

            PdfPCell detCell = new PdfPCell(detP);
            detCell.setColspan(2);
            detCell.setPadding(6);
            detCell.setBorderColor(COLOR_BORDER);
            detCell.setBackgroundColor(COLOR_HEADER_GRAY);
            boxTable.addCell(detCell);

            // Fila de Imágenes del requerimiento (si aplica, justo dentro del cuadro)
            if (req.getImagenesUrls() != null && !req.getImagenesUrls().isEmpty()) {
                PdfPCell imgContainerCell = new PdfPCell();
                imgContainerCell.setColspan(2);
                imgContainerCell.setPadding(8);
                imgContainerCell.setBorderColor(COLOR_BORDER);
                imgContainerCell.setBackgroundColor(COLOR_WHITE);

                Paragraph imgTitle = new Paragraph("📷 Imágenes descriptivas asociadas al " + reqCodeStr + ":\n\n", fontLabelBold());
                imgContainerCell.addElement(imgTitle);

                int idx = 1;
                int total = req.getImagenesUrls().size();

                for (ImagenDTO imgDto : req.getImagenesUrls()) {
                    String src = imgDto.getUrl();
                    if (src != null && !src.trim().isEmpty()) {
                        try {
                            Image img = null;
                            if (src.startsWith("data:image")) {
                                String base64Data = src.substring(src.indexOf(",") + 1).replaceAll("\\s+", "");
                                byte[] decodedBytes = Base64.getMimeDecoder().decode(base64Data);
                                img = Image.getInstance(decodedBytes);
                            } else if (src.startsWith("http://") || src.startsWith("https://")) {
                                img = Image.getInstance(new java.net.URL(src));
                            }

                            if (img != null) {
                                Paragraph imgHeader = new Paragraph("Requerimiento " + reqCodeStr + " - Imagen " + idx + " de " + total, fontSmallBold());
                                imgHeader.setSpacingAfter(4f);
                                imgContainerCell.addElement(imgHeader);

                                img.scaleToFit(500f, 400f);
                                img.setAlignment(Element.ALIGN_CENTER);
                                img.setSpacingAfter(10f);
                                imgContainerCell.addElement(img);
                            }
                        } catch (Exception e) {
                            log.warn("No se pudo incrustar imagen en PDF para req {}: {}", reqCodeStr, e.getMessage());
                        }
                    }
                    idx++;
                }

                boxTable.addCell(imgContainerCell);
            }

            document.add(boxTable);
        }
    }

    private void addSeguridadSection(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);

        PdfPCell head = new PdfPCell(new Phrase("5. REQUISITOS DE SEGURIDAD DE LA INFORMACIÓN", fontTableHeadDark()));
        head.setBackgroundColor(COLOR_HEADER_GRAY);
        head.setPadding(5);
        head.setBorderColor(COLOR_BORDER);
        table.addCell(head);

        String[] normas = {
                "Autentificar adecuadamente: La información confidencial y los sistemas informáticos sólo deben ser accesibles por las personas con los roles y permisos definidos.",
                "No utilizar campos ocultos: Evitar almacenar información sensible en campos ocultos que permitan manipular el funcionamiento interno.",
                "Comprobar las entradas: Verificar y controlar que los datos introducidos estén dentro del rango y formato válido.",
                "Valores límite de salida: Controlar la salida de los métodos para que el resultado esté dentro de parámetros definidos.",
                "Formato de salida: No alterar los formatos de salida para evitar errores asociados con manejo de buffer.",
                "Controles de seguridad: Declarar como privados o finales los métodos que realicen controles de seguridad.",
                "Pruebas seguras: Evitar el uso de datos reales de carácter personal en pruebas anteriores a la implantación.",
                "Control de acceso indirecto: Utilizar mapas de referencias indirectas en lugar de exponer URLs o rutas directas a objetos del servidor.",
                "Código seguro: Evitar generar código ejecutable a partir de valores ingresados por el usuario.",
                "Control de salidas: Garantizar que los datos obtenidos correspondan exactamente a lo solicitado en los requerimientos."
        };

        Paragraph listP = new Paragraph();
        for (String norma : normas) {
            String[] parts = norma.split(":", 2);
            listP.add(new Chunk("• " + parts[0] + ":", fontSmallBold()));
            if (parts.length > 1) {
                listP.add(new Chunk(parts[1] + "\n", fontSmall()));
            }
        }

        PdfPCell bodyCell = new PdfPCell(listP);
        bodyCell.setPadding(8);
        bodyCell.setBorderColor(COLOR_BORDER);
        bodyCell.setBackgroundColor(COLOR_WHITE);
        table.addCell(bodyCell);

        document.add(table);
    }

    private void addControlCambiosSection(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 25, 60});
        table.setSpacingAfter(10);

        PdfPCell head = new PdfPCell(new Phrase("CLASIFICACIÓN DE LA INFORMACIÓN Y CONTROL DE CAMBIOS", fontTableHead()));
        head.setColspan(3);
        head.setBackgroundColor(COLOR_TEAL);
        head.setPadding(5);
        head.setHorizontalAlignment(Element.ALIGN_CENTER);
        head.setBorderColor(COLOR_TEAL);
        table.addCell(head);

        String[] subheaders = {"VERSIÓN", "FECHA", "DESCRIPCIÓN DEL CAMBIO"};
        for (String sh : subheaders) {
            PdfPCell c = new PdfPCell(new Phrase(sh, fontTableHeadDark()));
            c.setBackgroundColor(COLOR_HEADER_GRAY);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(4);
            c.setBorderColor(COLOR_BORDER);
            table.addCell(c);
        }

        addControlCambiosRow(table, "1", "3/12/2024", "Este documento reemplaza el FORMATO SGI-DT-F-03 en su versión 7 por cambio de estructura.");
        addControlCambiosRow(table, "2", "15/08/2025", "Se actualiza con la inclusión de la metodología para el cálculo del Retorno sobre la Inversión para la viabilidad del proyecto.");

        document.add(table);
    }

    private void addControlCalidadSection(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{33, 33, 34});
        table.setSpacingBefore(10);
        table.setSpacingAfter(5);

        PdfPCell c1 = new PdfPCell(new Phrase("Elaborado por:\nProfesional Funcional de Desarrollador", fontSmall()));
        c1.setPadding(5); c1.setBorderColor(COLOR_BORDER); table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase("Revisado por:\nProf. Innovación y Optimización de Procesos", fontSmall()));
        c2.setPadding(5); c2.setBorderColor(COLOR_BORDER); table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase("Aprobado por:\nDirector de Transformación Digital", fontSmall()));
        c3.setPadding(5); c3.setBorderColor(COLOR_BORDER); table.addCell(c3);

        PdfPCell sub1 = new PdfPCell(new Phrase("SISTEMA DE GESTIÓN DE CALIDAD", fontSmallBold()));
        sub1.setColspan(2);
        sub1.setHorizontalAlignment(Element.ALIGN_CENTER);
        sub1.setBackgroundColor(COLOR_HEADER_GRAY);
        sub1.setPadding(4);
        sub1.setBorderColor(COLOR_BORDER);
        table.addCell(sub1);

        PdfPCell sub2 = new PdfPCell(new Phrase("Aprobado en agosto de 2025", fontSmall()));
        sub2.setHorizontalAlignment(Element.ALIGN_CENTER);
        sub2.setBackgroundColor(COLOR_HEADER_GRAY);
        sub2.setPadding(4);
        sub2.setBorderColor(COLOR_BORDER);
        table.addCell(sub2);

        document.add(table);
    }

    // =========================================================================
    // UTILIDADES DE FORMATO DE TABLAS E IMÁGENES
    // =========================================================================

    private void addTwoCellRow(PdfPTable table, String leftText, String rightText) {
        PdfPCell c1 = new PdfPCell(new Phrase(leftText, fontNormal()));
        c1.setPadding(5);
        c1.setBorderColor(COLOR_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(rightText, fontNormal()));
        c2.setPadding(5);
        c2.setBorderColor(COLOR_BORDER);
        table.addCell(c2);
    }

    private void addControlCambiosRow(PdfPTable table, String v, String f, String desc) {
        PdfPCell c1 = new PdfPCell(new Phrase(v, fontNormal()));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setPadding(4); c1.setBorderColor(COLOR_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(f, fontNormal()));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setPadding(4); c2.setBorderColor(COLOR_BORDER);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(desc, fontNormal()));
        c3.setPadding(4); c3.setBorderColor(COLOR_BORDER);
        table.addCell(c3);
    }

    private Image getLogoImage() {
        try (InputStream is = getClass().getResourceAsStream("/asmet_logo.png")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                Image img = Image.getInstance(bytes);
                img.scaleToFit(140f, 45f);
                return img;
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo de la cabecera en PdfGeneratorService: {}", e.getMessage());
        }
        return null;
    }

    private String val(String s) {
        return (s != null && !s.trim().isEmpty()) ? s : "-";
    }
}

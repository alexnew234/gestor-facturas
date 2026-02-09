package com.example.demo.servicio;

import com.example.demo.modelo.Factura;
import jakarta.servlet.http.HttpServletResponse; // Importante
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// --- IMPORTS PARA EL PDF ---
import java.awt.Color;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
// ------------------------------------

@Service
public class FacturaService {

    private List<Factura> repositorio = new ArrayList<>();
    private Long nextId = 1L;

    @Autowired
    private JavaMailSender mailSender;

    // ... (Tus métodos findAll, findById, guardar, borrar, enviarEmail, buscar, calcularTotal, contarPendientes, contarTotal SIGUEN IGUAL) ...
    // NO LOS BORRES, déjalos como los tenías.

    public List<Factura> findAll() { return repositorio; }

    public Factura findById(Long id) {
        return repositorio.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }

    public void guardar(Factura f) {
        double iva = f.getBaseImponible() * (f.getPorcentajeIva() / 100);
        double irpf = f.getBaseImponible() * (f.getPorcentajeIrpf() / 100);
        double total = f.getBaseImponible() + iva - irpf;
        f.setImporteTotal(Math.round(total * 100.0) / 100.0);

        if (f.getId() == null) {
            f.setId(nextId++);
            repositorio.add(f);
        } else {
            repositorio.removeIf(e -> e.getId().equals(f.getId()));
            repositorio.add(f);
        }
    }

    public void borrar(Long id) { repositorio.removeIf(f -> f.getId().equals(id)); }

    public void enviarFacturaPorEmail(Factura f) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(f.getClienteEmail());

            // Asunto
            message.setSubject("Nueva Factura Disponible - " + f.getConcepto());

            // Cuerpo del mensaje (FORMATO DETALLADO)
            message.setText("Estimado/a " + f.getClienteNombre() + ",\n\n" +
                    "Adjuntamos el resumen de su nueva factura:\n\n" +
                    "----------------------------------------------------\n" +
                    "CONCEPTO: " + f.getConcepto() + "\n" +
                    "----------------------------------------------------\n\n" +
                    "Base Imponible: " + f.getBaseImponible() + " €\n" +
                    "Total a Pagar:  " + f.getImporteTotal() + " €\n\n" +
                    "Gracias por confiar en nosotros.");

            mailSender.send(message);
            System.out.println("Email enviado correctamente a " + f.getClienteEmail());
        } catch (Exception e) {
            e.printStackTrace(); // O System.err.println...
        }
    }

    public List<Factura> buscar(String consulta) {
        String texto = consulta.toLowerCase();
        return repositorio.stream()
                .filter(f -> f.getClienteNombre().toLowerCase().contains(texto) || f.getConcepto().toLowerCase().contains(texto))
                .toList();
    }

    public Double calcularTotalFacturado(List<Factura> facturas) {
        double total = 0.0;
        for (Factura f : facturas) {
            if (f.getImporteTotal() != null) {
                total += f.getImporteTotal();
            }
        }
        return Math.round(total * 100.0) / 100.0; // Redondeo bonito
    }

    public long contarFacturasPendientes(List<Factura> facturas) {
        // Usamos la lista que nos pasan, no 'repositorio'
        return facturas.stream().filter(f -> !f.isPagada()).count();
    }

    public long contarTotalFacturas(List<Factura> facturas) {
        // Contamos el tamaño de la lista que nos pasan
        return facturas.size();
    }

    public void generarPdf(Factura factura, HttpServletResponse response) {
        try {
            // 1. Configuración y Márgenes
            Document document = new Document(PageSize.A4, 36, 36, 54, 54); // Márgenes más elegantes
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

            // --- ESTILOS ---
            // Colores profesionales (Azul oscuro y Gris)
            Color colorPrincipal = new Color(44, 62, 80);   // Azul oscuro elegante
            Color colorSecundario = new Color(127, 140, 141); // Gris para detalles
            Color colorFondoTabla = new Color(236, 240, 241); // Gris muy claro para cabeceras

            // Fuentes
            Font fuenteTituloGigante = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, colorPrincipal);
            Font fuenteCabeceraTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, colorPrincipal);
            Font fuenteCeldaNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fuenteCeldaImporte = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fuenteEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, colorSecundario);
            Font fuenteValor = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fuenteTotalGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE);

            document.open();

            // --- SECCIÓN 1: CABECERA (Logo y Datos Empresa) ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1}); // 50% y 50%

            // Celda Izquierda: LOGO
            PdfPCell cellLogo = new PdfPCell();
            cellLogo.setBorder(Rectangle.NO_BORDER);
            cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);

            // Intentamos cargar el logo si existe
            if (factura.getNombreLogo() != null && !factura.getNombreLogo().isEmpty()) {
                try {
                    // Asume que la carpeta 'upload' está en la raíz del proyecto
                    Image logo = Image.getInstance("upload/" + factura.getNombreLogo());
                    logo.scaleToFit(150, 80); // Tamaño máximo
                    cellLogo.addElement(logo);
                } catch (Exception e) {
                    // Si falla el logo, ponemos texto
                    cellLogo.addElement(new Paragraph("SaaS Facturas", fuenteTituloGigante));
                }
            } else {
                cellLogo.addElement(new Paragraph("SaaS Facturas", fuenteTituloGigante));
            }
            headerTable.addCell(cellLogo);

            // Celda Derecha: DATOS FACTURA
            PdfPCell cellDatosEmpresa = new PdfPCell();
            cellDatosEmpresa.setBorder(Rectangle.NO_BORDER);
            cellDatosEmpresa.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDatosEmpresa.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph pNumero = new Paragraph("FACTURA Nº " + factura.getId(), fuenteTituloGigante);
            pNumero.setAlignment(Element.ALIGN_RIGHT);
            cellDatosEmpresa.addElement(pNumero);

            Paragraph pFecha = new Paragraph("Fecha de Emisión: " + factura.getFechaEmision(), fuenteEtiqueta);
            pFecha.setAlignment(Element.ALIGN_RIGHT);
            cellDatosEmpresa.addElement(pFecha);

            headerTable.addCell(cellDatosEmpresa);
            document.add(headerTable);

            // --- SECCIÓN 2: SEPARADOR Y DATOS DEL CLIENTE ---
            Paragraph separador = new Paragraph(" ");
            separador.setSpacingAfter(20);
            document.add(separador);

            // Tabla para datos del cliente con un fondo suave
            PdfPTable clienteTable = new PdfPTable(1);
            clienteTable.setWidthPercentage(100);
            PdfPCell cellClienteHeader = new PdfPCell(new Phrase("FACTURADO A:", fuenteEtiqueta));
            cellClienteHeader.setBorder(Rectangle.NO_BORDER);
            cellClienteHeader.setPaddingBottom(5);
            clienteTable.addCell(cellClienteHeader);

            PdfPCell cellClienteDatos = new PdfPCell();
            cellClienteDatos.setBorder(Rectangle.NO_BORDER);
            cellClienteDatos.addElement(new Paragraph(factura.getClienteNombre(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            cellClienteDatos.addElement(new Paragraph("NIF/CIF: " + factura.getClienteNif(), fuenteValor));
            cellClienteDatos.addElement(new Paragraph("Email: " + factura.getClienteEmail(), fuenteValor));
            cellClienteDatos.setPaddingBottom(20);
            clienteTable.addCell(cellClienteDatos);

            document.add(clienteTable);

            // --- SECCIÓN 3: TABLA DE CONCEPTOS ---
            PdfPTable table = new PdfPTable(2); // 2 columnas: Concepto y Precio
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1}); // La columna de concepto es 3 veces más ancha
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);

            // Cabeceras
            PdfPCell cellHeader1 = new PdfPCell(new Phrase("CONCEPTO / DESCRIPCIÓN", fuenteCabeceraTabla));
            cellHeader1.setBackgroundColor(colorFondoTabla);
            cellHeader1.setBorderColor(colorPrincipal);
            cellHeader1.setPadding(10);
            table.addCell(cellHeader1);

            PdfPCell cellHeader2 = new PdfPCell(new Phrase("IMPORTE", fuenteCabeceraTabla));
            cellHeader2.setBackgroundColor(colorFondoTabla);
            cellHeader2.setBorderColor(colorPrincipal);
            cellHeader2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellHeader2.setPadding(10);
            table.addCell(cellHeader2);

            // Datos (Fila 1)
            PdfPCell cellConcepto = new PdfPCell(new Phrase(factura.getConcepto(), fuenteCeldaNormal));
            cellConcepto.setPadding(10);
            cellConcepto.setBorderColor(colorSecundario);
            table.addCell(cellConcepto);

            PdfPCell cellImporte = new PdfPCell(new Phrase(String.format("%.2f €", factura.getBaseImponible()), fuenteCeldaImporte));
            cellImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellImporte.setPadding(10);
            cellImporte.setBorderColor(colorSecundario);
            table.addCell(cellImporte);

            // (Opcional) Filas vacías para dar sensación de "hoja"
            for(int i=0; i<3; i++){
                PdfPCell empty = new PdfPCell(new Phrase(" "));
                empty.setPadding(10);
                empty.setBorderColor(colorSecundario);
                table.addCell(empty);
                table.addCell(empty);
            }

            document.add(table);

            // --- SECCIÓN 4: TOTALES (El toque profesional final) ---
            // Usamos una tabla alineada a la derecha para los totales
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40); // Solo ocupa el 40% del ancho
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{1, 1});

            // Helper para añadir filas de totales
            addTotalRow(totalsTable, "Base Imponible:", String.format("%.2f €", factura.getBaseImponible()), fuenteEtiqueta, fuenteValor, false);

            // IVA
            double ivaImporte = factura.getBaseImponible() * (factura.getPorcentajeIva() / 100.0);
            addTotalRow(totalsTable, "IVA (" + factura.getPorcentajeIva() + "%):", String.format("+ %.2f €", ivaImporte), fuenteEtiqueta, fuenteValor, false);

            // IRPF (si aplica)
            if (factura.getPorcentajeIrpf() > 0) {
                double irpfImporte = factura.getBaseImponible() * (factura.getPorcentajeIrpf() / 100.0);
                addTotalRow(totalsTable, "IRPF (" + factura.getPorcentajeIrpf() + "%):", String.format("- %.2f €", irpfImporte), fuenteEtiqueta, fuenteValor, false);
            }

            // TOTAL FINAL (Caja azul)
            PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            cellTotalLabel.setBackgroundColor(colorPrincipal);
            cellTotalLabel.setPadding(10);
            cellTotalLabel.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cellTotalLabel);

            PdfPCell cellTotalValue = new PdfPCell(new Phrase(String.format("%.2f €", factura.getImporteTotal()), fuenteTotalGrande));
            cellTotalValue.setBackgroundColor(colorPrincipal);
            cellTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValue.setPadding(10);
            cellTotalValue.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cellTotalValue);

            document.add(totalsTable);

            // Pie de página simple
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Gracias por su confianza. Documento generado automáticamente.", fuenteEtiqueta);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);


            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar pequeño para añadir filas a la tabla de totales
    private void addTotalRow(PdfPTable table, String label, String value, Font fontLabel, Font fontValue, boolean isLast) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fontLabel));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPaddingTop(5);
        c1.setPaddingBottom(5);
        if(isLast) c1.setPaddingBottom(15);

        PdfPCell c2 = new PdfPCell(new Phrase(value, fontValue));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPaddingTop(5);
        c2.setPaddingBottom(5);
        if(isLast) c2.setPaddingBottom(15);

        table.addCell(c1);
        table.addCell(c2);
    }

}
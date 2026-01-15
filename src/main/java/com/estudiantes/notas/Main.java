package com.estudiantes.notas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

// Imports de iText 7 para PDF Profesional
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.geom.Rectangle;

public class Main {
    private static List<Asignatura> listaAsignaturas = new ArrayList<>();

    // Manejador para la paginación (Pie de página)
    private static class FooterHandler implements IEventHandler {
        @Override
        public void handleEvent(com.itextpdf.kernel.events.Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            com.itextpdf.kernel.pdf.PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            pdfCanvas.beginText()
                    .setFontAndSize(pdfDoc.getDefaultFont(), 9)
                    .moveText(30, 20)
                    .showText(String.valueOf(pageNumber))
                    .endText()
                    .release();
        }
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Gestión Académica - Reportes en PDF");
        shell.setSize(700, 800);
        shell.setLayout(new GridLayout(1, false));

        // --- REFERENCIAS A WIDGETS ---
        final Combo comboAsignaturas;
        final Text txtNuevaAsignatura;
        final Text txtNombre, txtApellido, txtT1, txtT2, txtT3, txtParcial, txtFinal;
        final org.eclipse.swt.widgets.Table tableUI;
        final Text txtInforme;

        // --- 1. SECCIÓN: ASIGNATURAS ---
        txtNuevaAsignatura = createAsignaturaSection(shell);

        // --- 2. SECCIÓN: NOTAS ---
        Map<String, Control> notasWidgets = createNotasSection(shell);
        comboAsignaturas = (Combo) notasWidgets.get("combo");
        txtNombre = (Text) notasWidgets.get("nombre");
        txtApellido = (Text) notasWidgets.get("apellido");
        txtT1 = (Text) notasWidgets.get("t1");
        txtT2 = (Text) notasWidgets.get("t2");
        txtT3 = (Text) notasWidgets.get("t3");
        txtParcial = (Text) notasWidgets.get("parcial");
        txtFinal = (Text) notasWidgets.get("final");

        // --- 3. SECCIÓN: TABLA ---
        tableUI = createTableSection(shell);

        // --- 4. SECCIÓN: BOTONES Y RESULTADOS ---
        txtInforme = createActionSection(shell, tableUI, comboAsignaturas, txtNombre, txtApellido, txtT1, txtT2, txtT3, txtParcial, txtFinal, txtNuevaAsignatura);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /**
     * Crea la sección para definir nuevas asignaturas.
     */
    private static Text createAsignaturaSection(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("1. Definir Asignaturas");
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        group.setLayout(new GridLayout(3, false));

        new Label(group, SWT.NONE).setText("Nombre Asignatura:");
        Text txt = new Text(group, SWT.BORDER);
        txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btn = new Button(group, SWT.PUSH);
        btn.setText("Crear");
        
        return txt;
    }

    /**
     * Crea la sección para registro de calificaciones.
     */
    private static Map<String, Control> createNotasSection(Shell shell) {
        Map<String, Control> widgets = new HashMap<>();
        Group group = new Group(shell, SWT.NONE);
        group.setText("2. Registrar Notas");
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        group.setLayout(new GridLayout(2, false));

        new Label(group, SWT.NONE).setText("Seleccionar Asignatura:");
        Combo combo = new Combo(group, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        widgets.put("combo", combo);

        Composite compositeInput = new Composite(group, SWT.NONE);
        compositeInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        compositeInput.setLayout(new GridLayout(4, false));

        widgets.put("nombre", createLabelAndText(compositeInput, "Nombre:"));
        widgets.put("apellido", createLabelAndText(compositeInput, "Apellido:"));
        widgets.put("t1", createLabelAndText(compositeInput, "T1:"));
        widgets.put("t2", createLabelAndText(compositeInput, "T2:"));
        widgets.put("t3", createLabelAndText(compositeInput, "T3:"));
        widgets.put("parcial", createLabelAndText(compositeInput, "Parcial:"));
        widgets.put("final", createLabelAndText(compositeInput, "Final:"));

        return widgets;
    }

    private static Text createLabelAndText(Composite parent, String labelText) {
        new Label(parent, SWT.NONE).setText(labelText);
        Text text = new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        return text;
    }

    /**
     * Crea la tabla de visualización de datos.
     */
    private static org.eclipse.swt.widgets.Table createTableSection(Shell shell) {
        org.eclipse.swt.widgets.Table table = new org.eclipse.swt.widgets.Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String[] headers = {"Asignatura", "Alumno", "T1", "T2", "T3", "Parc.", "Final", "Prom."};
        for (String h : headers) {
            org.eclipse.swt.widgets.TableColumn col = new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
            col.setText(h);
            col.setWidth(80);
        }
        return table;
    }

    /**
     * Configura los botones de acción y el área de texto del informe.
     */
    private static Text createActionSection(Shell shell, org.eclipse.swt.widgets.Table table, Combo combo, 
                                          Text txtNom, Text txtApe, Text t1, Text t2, Text t3, Text parc, Text fin, Text txtNewAsig) {
        
        // El botón "Registrar" estaba en la sección de notas, pero para refactorizar la lógica lo pondremos aquí
        // Primero recuperamos el parent de los widgets de notas para añadir el botón si es necesario, 
        // o mejor, buscamos el compositeInput dentro del groupNotas.
        // Por simplicidad, añadiremos los listeners a los botones que ya existen o se crean aquí.
        
        // Re-localizamos el botón de registro (que estaba en createNotasSection original)
        // Pero para no romper el layout, lo creamos aquí dentro del composite correcto
        Button btnAddNota = new Button(t1.getParent(), SWT.PUSH);
        btnAddNota.setText("Registrar Calificaciones");
        btnAddNota.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

        Composite compositeButtons = new Composite(shell, SWT.NONE);
        compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        compositeButtons.setLayout(new GridLayout(2, true));

        Button btnGenerar = new Button(compositeButtons, SWT.PUSH);
        btnGenerar.setText("Ver en Pantalla");
        btnGenerar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnExportarPDF = new Button(compositeButtons, SWT.PUSH);
        btnExportarPDF.setText("Descargar PDF");
        btnExportarPDF.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Text txtInforme = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        txtInforme.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtInforme.setEditable(false);

        // --- ASOCIACIÓN DE EVENTOS ---
        setupEventListeners(shell, table, combo, txtNom, txtApe, t1, t2, t3, parc, fin, txtNewAsig, btnGenerar, btnExportarPDF, btnAddNota, txtInforme);

        return txtInforme;
    }

    private static void setupEventListeners(Shell shell, org.eclipse.swt.widgets.Table table, Combo combo, 
                                          Text txtNom, Text txtApe, Text t1, Text t2, Text t3, Text parc, Text fin, 
                                          Text txtNewAsig, Button btnGen, Button btnPdf, Button btnAdd, Text txtInf) {
        
        // Listener para añadir asignatura
        // Buscamos el botón de la asignatura - Por refactorización lo capturamos del parent
        Button btnAsig = (Button) txtNewAsig.getParent().getChildren()[2];
        btnAsig.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String nombre = txtNewAsig.getText().trim();
                if (!nombre.isEmpty()) {
                    listaAsignaturas.add(new Asignatura(nombre));
                    combo.add(nombre);
                    txtNewAsig.setText("");
                }
            }
        });

        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = combo.getSelectionIndex();
                if (index == -1) return;
                try {
                    NotaAlumno nota = new NotaAlumno(txtNom.getText(), txtApe.getText(),
                            Double.parseDouble(t1.getText()), Double.parseDouble(t2.getText()),
                            Double.parseDouble(t3.getText()), Double.parseDouble(parc.getText()),
                            Double.parseDouble(fin.getText()));

                    Asignatura seleccionada = listaAsignaturas.get(index);
                    seleccionada.agregarNota(nota);

                    org.eclipse.swt.widgets.TableItem item = new org.eclipse.swt.widgets.TableItem(table, SWT.NONE);
                    item.setText(new String[]{seleccionada.getNombre(), nota.getNombreAlumno(),
                            t1.getText(), t2.getText(), t3.getText(),
                            parc.getText(), fin.getText(), String.format("%.2f", nota.getPromedioCuatrimestre())});

                } catch (Exception ex) {
                    System.out.println("Error en datos");
                }
            }
        });

        btnGen.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                txtInf.setText(generarTextoInforme(listaAsignaturas));
            }
        });

        btnPdf.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!listaAsignaturas.isEmpty()) guardarComoPDF(listaAsignaturas, shell);
            }
        });
    }

    private static String generarTextoInforme(List<Asignatura> asignaturas) {
        StringBuilder sb = new StringBuilder("Reporte rápido:\n");
        for (Asignatura a : asignaturas) {
            sb.append(a.getNombre()).append(" - Media: ").append(String.format("%.2f", a.getMedia())).append("\n");
        }
        return sb.toString();
    }

    private static void guardarComoPDF(List<Asignatura> asignaturas, Shell shell) {
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream("Reporte_Academico_Pro.pdf"));
            PdfDocument pdf = new PdfDocument(writer);
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHandler());
            Document documento = new Document(pdf);

            // Estilos de colores
            DeviceRgb azulOscuro = new DeviceRgb(0, 51, 102);
            DeviceRgb azulClaro = new DeviceRgb(0, 102, 204);
            DeviceRgb blanco = new DeviceRgb(255, 255, 255);
            DeviceRgb grisFondo = new DeviceRgb(240, 240, 240);

            // 1. PORTADA E ÍNDICE
            documento.add(new Paragraph("SISTEMA DE GESTIÓN ACADÉMICA")
                    .setBold().setFontSize(24).setFontColor(azulOscuro).setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("INFORME DE RENDIMIENTO PROFESIONAL")
                    .setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("\n\n\n"));
            
            documento.add(new Paragraph("ÍNDICE DEL INFORME")
                    .setBold().setFontSize(14).setUnderline().setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("1. Seguimiento y Estado de Cursos ........................... Pág. 1")
                    .setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("2. Reporte Individual por Estudiante ........................ Pág. 2")
                    .setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("3. Auditoría y Resumen General .............................. Pág. X")
                    .setTextAlignment(TextAlignment.CENTER));
            documento.add(new AreaBreak());

            // 2. SECCIÓN: SEGUIMIENTO DE CURSOS
            documento.add(new Paragraph("1. SEGUIMIENTO DE CURSOS Y ASIGNATURAS")
                    .setBold().setFontSize(14).setFontColor(azulOscuro));
            documento.add(new Paragraph("Análisis del rendimiento colectivo por cada materia registrada.\n\n"));

            Table tablaCursos = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 3}));
            tablaCursos.setWidth(UnitValue.createPercentValue(100));
            
            String[] headersCursos = {"Asignatura", "Promedio Clase", "Estado", "Nivel de Seguimiento"};
            for (String h : headersCursos) {
                tablaCursos.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(blanco))
                        .setBackgroundColor(azulClaro));
            }

            for (Asignatura asig : asignaturas) {
                double media = asig.getMedia();
                String estadoStr = (media >= 7.0) ? "BIEN" : "MEJORA REQUERIDA";
                DeviceRgb colorEstado = (media >= 7.0) ? new DeviceRgb(0, 100, 0) : new DeviceRgb(150, 0, 0);
                
                tablaCursos.addCell(new Cell().add(new Paragraph(asig.getNombre().toUpperCase())));
                tablaCursos.addCell(new Cell().add(new Paragraph(String.format("%.2f", media)).setTextAlignment(TextAlignment.CENTER)));
                tablaCursos.addCell(new Cell().add(new Paragraph(estadoStr).setBold().setFontColor(colorEstado)));
                tablaCursos.addCell(new Cell().add(new Paragraph(media >= 8.5 ? "Excelente" : (media >= 7 ? "Estable" : "Crítico"))));
            }
            documento.add(tablaCursos);
            documento.add(new AreaBreak());

            // 3. SECCIÓN: REPORTE POR ESTUDIANTE
            documento.add(new Paragraph("2. REPORTE DETALLADO POR ESTUDIANTE")
                    .setBold().setFontSize(14).setFontColor(azulOscuro));
            documento.add(new Paragraph("Desglose de calificaciones y seguimiento individualizado.\n\n"));

            // Agrupar datos por alumno
            Map<String, List<Object[]>> datosAlumnos = new TreeMap<>();
            for (Asignatura a : asignaturas) {
                for (NotaAlumno n : a.getNotas()) {
                    String nombreCompleto = n.getNombreAlumno() + " " + n.getApellidoAlumno();
                    datosAlumnos.computeIfAbsent(nombreCompleto, k -> new ArrayList<>()).add(new Object[]{a.getNombre(), n});
                }
            }

            for (Map.Entry<String, List<Object[]>> entry : datosAlumnos.entrySet()) {
                documento.add(new Paragraph("Estudiante: " + entry.getKey().toUpperCase())
                        .setBold().setBackgroundColor(grisFondo).setPadding(5));
                
                Table tablaIndiv = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1, 1, 1, 1.5f, 2}));
                tablaIndiv.setWidth(UnitValue.createPercentValue(100));
                
                String[] headersIndiv = {"Materia", "T1", "T2", "T3", "Par.", "Fin.", "Prom.", "Seguimiento"};
                for (String h : headersIndiv) {
                    tablaIndiv.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontSize(9).setFontColor(blanco))
                            .setBackgroundColor(azulOscuro));
                }

                double sumaPromedios = 0;
                for (Object[] row : entry.getValue()) {
                    String asigNombre = (String) row[0];
                    NotaAlumno nota = (NotaAlumno) row[1];
                    double prom = nota.getPromedioCuatrimestre();
                    sumaPromedios += prom;

                    String seg = (prom >= 7.0) ? "Bien" : "Mal";
                    DeviceRgb colSeg = (prom >= 7.0) ? new DeviceRgb(0, 128, 0) : new DeviceRgb(200, 0, 0);

                    tablaIndiv.addCell(new Paragraph(asigNombre).setFontSize(8));
                    tablaIndiv.addCell(new Paragraph(String.valueOf(nota.getT1())).setFontSize(8));
                    tablaIndiv.addCell(new Paragraph(String.valueOf(nota.getT2())).setFontSize(8));
                    tablaIndiv.addCell(new Paragraph(String.valueOf(nota.getT3())).setFontSize(8));
                    tablaIndiv.addCell(new Paragraph(String.valueOf(nota.getParcial())).setFontSize(8));
                    tablaIndiv.addCell(new Paragraph(String.valueOf(nota.getNotaFinal())).setFontSize(8));
                    tablaIndiv.addCell(new Paragraph(String.format("%.2f", prom)).setFontSize(8).setBold());
                    tablaIndiv.addCell(new Cell().add(new Paragraph(seg).setFontSize(8).setBold()).setFontColor(colSeg));
                }
                documento.add(tablaIndiv);
                double promGral = sumaPromedios / entry.getValue().size();
                documento.add(new Paragraph("Promedio General Alumno: " + String.format("%.2f", promGral))
                        .setFontSize(9).setItalic().setTextAlignment(TextAlignment.RIGHT));
                documento.add(new Paragraph("\n"));
            }

            documento.add(new AreaBreak());

            // 4. SECCIÓN: AUDITORÍA Y RESUMEN GENERAL
            documento.add(new Paragraph("3. AUDITORÍA Y RESUMEN GENERAL")
                    .setBold().setFontSize(14).setFontColor(azulOscuro));
            
            List<String> alumnosBien = new ArrayList<>();
            List<String> alumnosMal = new ArrayList<>();
            double sumaGlobal = 0;
            int totalRegistros = 0;

            for (Map.Entry<String, List<Object[]>> entry : datosAlumnos.entrySet()) {
                double suma = 0;
                for (Object[] obj : entry.getValue()) {
                    suma += ((NotaAlumno) obj[1]).getPromedioCuatrimestre();
                    sumaGlobal += ((NotaAlumno) obj[1]).getPromedioCuatrimestre();
                    totalRegistros++;
                }
                double mediaAlumno = suma / entry.getValue().size();
                if (mediaAlumno >= 7.0) alumnosBien.add(entry.getKey());
                else alumnosMal.add(entry.getKey());
            }

            documento.add(new Paragraph("Estadísticas de Población Estudiantil:").setBold().setMarginTop(10));
            Table tablaResumen = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            tablaResumen.setWidth(UnitValue.createPercentValue(60));
            
            tablaResumen.addCell(new Cell().add(new Paragraph("Total Alumnos:")));
            tablaResumen.addCell(new Cell().add(new Paragraph(String.valueOf(datosAlumnos.size()))));
            tablaResumen.addCell(new Cell().add(new Paragraph("Promedio General Sistema:")));
            tablaResumen.addCell(new Cell().add(new Paragraph(String.format("%.2f", sumaGlobal/totalRegistros))));
            documento.add(tablaResumen);

            documento.add(new Paragraph("\nAlumnos con Rendimiento Satisfactorio (BIEN):").setBold().setFontColor(new DeviceRgb(0, 100, 0)));
            documento.add(new Paragraph(alumnosBien.isEmpty() ? "Ninguno" : String.join(", ", alumnosBien)).setFontSize(10));

            documento.add(new Paragraph("\nAlumnos en Situación de Riesgo (MAL):").setBold().setFontColor(new DeviceRgb(150, 0, 0)));
            documento.add(new Paragraph(alumnosMal.isEmpty() ? "Ninguno" : String.join(", ", alumnosMal)).setFontSize(10));

            documento.close();
            MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
            mb.setMessage("Reporte Académico Profesional generado: Reporte_Academico_Pro.pdf");
            mb.open();

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
            mb.setMessage("Error al generar el PDF: " + e.getMessage());
            mb.open();
        }
    }
}
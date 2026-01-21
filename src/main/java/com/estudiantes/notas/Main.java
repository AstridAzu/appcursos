package com.estudiantes.notas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.text.SimpleDateFormat;

// Imports de JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.util.TableOrder;

// Imports de iText 7 para PDF Profesional
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
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

        // --- 0. SECCIÓN: AYUDA ---
        createHelpSection(shell);

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

        Button btnEditar = new Button(compositeButtons, SWT.PUSH);
        btnEditar.setText("Editar Seleccionado");
        btnEditar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnEliminar = new Button(compositeButtons, SWT.PUSH);
        btnEliminar.setText("Eliminar Seleccionado");
        btnEliminar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnGraficoBarras = new Button(compositeButtons, SWT.PUSH);
        btnGraficoBarras.setText("Gráfico de Barras");
        btnGraficoBarras.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnGraficoCircular = new Button(compositeButtons, SWT.PUSH);
        btnGraficoCircular.setText("Gráfico Circular");
        btnGraficoCircular.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Text txtInforme = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        txtInforme.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtInforme.setEditable(false);

        // --- ASOCIACIÓN DE EVENTOS ---
        setupEventListeners(shell, table, combo, txtNom, txtApe, t1, t2, t3, parc, fin, txtNewAsig, btnGenerar, btnExportarPDF, btnEditar, btnEliminar, btnGraficoBarras, btnGraficoCircular, btnAddNota, txtInforme);

        return txtInforme;
    }

    private static void setupEventListeners(Shell shell, org.eclipse.swt.widgets.Table table, Combo combo, 
                                          Text txtNom, Text txtApe, Text t1, Text t2, Text t3, Text parc, Text fin, 
                                          Text txtNewAsig, Button btnGen, Button btnPdf, Button btnEdit, Button btnDel, 
                                          Button btnGrafBar, Button btnGrafPie, Button btnAdd, Text txtInf) {
        
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

        final org.eclipse.swt.widgets.TableItem[] itemParaEditar = { null };

        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = combo.getSelectionIndex();
                if (index == -1) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING);
                    mb.setMessage("Por favor seleccione una asignatura");
                    mb.open();
                    return;
                }
                try {
                    String nombre = txtNom.getText();
                    String apellido = txtApe.getText();
                    double t1Val = Double.parseDouble(t1.getText());
                    double t2Val = Double.parseDouble(t2.getText());
                    double t3Val = Double.parseDouble(t3.getText());
                    double parcVal = Double.parseDouble(parc.getText());
                    double finVal = Double.parseDouble(fin.getText());

                    Asignatura seleccionada = listaAsignaturas.get(index);

                    if (itemParaEditar[0] != null) {
                        // Modo edición
                        Object[] data = (Object[]) itemParaEditar[0].getData();
                        Asignatura asigAntigua = (Asignatura) data[0];
                        NotaAlumno notaExistente = (NotaAlumno) data[1];

                        // Si cambió la asignatura, removemos de la antigua y añadimos a la nueva
                        if (asigAntigua != seleccionada) {
                            asigAntigua.getNotas().remove(notaExistente);
                            seleccionada.agregarNota(notaExistente);
                        }

                        // Actualizamos los datos del objeto nota
                        updateNota(notaExistente, nombre, apellido, t1Val, t2Val, t3Val, parcVal, finVal);

                        // Actualizamos la fila en la tabla
                        itemParaEditar[0].setText(new String[]{seleccionada.getNombre(), nombre,
                                t1.getText(), t2.getText(), t3.getText(),
                                parc.getText(), fin.getText(), String.format("%.2f", notaExistente.getPromedioCuatrimestre())});
                        
                        itemParaEditar[0].setData(new Object[]{seleccionada, notaExistente});
                        
                        btnAdd.setText("Registrar Calificaciones");
                        itemParaEditar[0] = null;
                        
                    } else {
                        // Modo creación
                        NotaAlumno nota = new NotaAlumno(nombre, apellido, t1Val, t2Val, t3Val, parcVal, finVal);
                        seleccionada.agregarNota(nota);

                        org.eclipse.swt.widgets.TableItem item = new org.eclipse.swt.widgets.TableItem(table, SWT.NONE);
                        item.setText(new String[]{seleccionada.getNombre(), nota.getNombreAlumno(),
                                t1.getText(), t2.getText(), t3.getText(),
                                parc.getText(), fin.getText(), String.format("%.2f", nota.getPromedioCuatrimestre())});
                        item.setData(new Object[]{seleccionada, nota});
                    }

                    // Limpiar campos
                    txtNom.setText(""); txtApe.setText(""); t1.setText(""); t2.setText(""); t3.setText(""); parc.setText(""); fin.setText("");

                } catch (Exception ex) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
                    mb.setMessage("Error en los datos ingresados: " + ex.getMessage());
                    mb.open();
                }
            }
        });

        btnEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index == -1) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                    mb.setMessage("Seleccione una fila de la tabla para editar");
                    mb.open();
                    return;
                }
                
                org.eclipse.swt.widgets.TableItem item = table.getItem(index);
                itemParaEditar[0] = item;
                Object[] data = (Object[]) item.getData();
                Asignatura asig = (Asignatura) data[0];
                NotaAlumno nota = (NotaAlumno) data[1];

                // Llenar los campos
                for (int i = 0; i < combo.getItemCount(); i++) {
                    if (combo.getItem(i).equals(asig.getNombre())) {
                        combo.select(i);
                        break;
                    }
                }
                txtNom.setText(nota.getNombreAlumno());
                txtApe.setText(nota.getApellidoAlumno());
                t1.setText(String.valueOf(nota.getT1()));
                t2.setText(String.valueOf(nota.getT2()));
                t3.setText(String.valueOf(nota.getT3()));
                parc.setText(String.valueOf(nota.getParcial()));
                fin.setText(String.valueOf(nota.getNotaFinal()));

                btnAdd.setText("Actualizar Calificaciones");
            }
        });

        btnDel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = table.getSelectionIndex();
                if (index == -1) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                    mb.setMessage("Seleccione una fila de la tabla para eliminar");
                    mb.open();
                    return;
                }

                MessageBox confirm = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                confirm.setMessage("¿Está seguro de que desea eliminar esta nota?");
                if (confirm.open() == SWT.YES) {
                    org.eclipse.swt.widgets.TableItem item = table.getItem(index);
                    Object[] data = (Object[]) item.getData();
                    Asignatura asig = (Asignatura) data[0];
                    NotaAlumno nota = (NotaAlumno) data[1];

                    asig.getNotas().remove(nota);
                    table.remove(index);
                    
                    if (itemParaEditar[0] == item) {
                        itemParaEditar[0] = null;
                        btnAdd.setText("Registrar Calificaciones");
                        txtNom.setText(""); txtApe.setText(""); t1.setText(""); t2.setText(""); t3.setText(""); parc.setText(""); fin.setText("");
                    }
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

        btnGrafBar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (listaAsignaturas.isEmpty()) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                    mb.setMessage("No hay datos para mostrar el gráfico");
                    mb.open();
                    return;
                }
                mostrarGraficoBarras(shell);
            }
        });

        btnGrafPie.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (listaAsignaturas.isEmpty()) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                    mb.setMessage("No hay datos para mostrar el gráfico");
                    mb.open();
                    return;
                }
                mostrarGraficoCircular(shell);
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
            documento.add(new Paragraph("3. Estadísticas Visuales ................................... Pág. 3")
                    .setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("4. Auditoría y Resumen General .............................. Pág. X")
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

            // 4. SECCIÓN: ESTADÍSTICAS VISUALES
            documento.add(new Paragraph("3. ESTADÍSTICAS VISUALES (RENDIMIENTO)")
                    .setBold().setFontSize(14).setFontColor(azulOscuro));
            documento.add(new Paragraph("Desglose académico por categorías de rendimiento.\n\n"));

            DefaultCategoryDataset datasetMultiple = new DefaultCategoryDataset();
            llenarDatasetCategorias(datasetMultiple, asignaturas);

            // 3.1 Gráfico de Barras
            documento.add(new Paragraph("3.1 Comparativa de Rendimiento (Barras)").setBold().setFontSize(12));
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Distribución por Materias",
                    "Asignaturas",
                    "Cantidad de Alumnos",
                    datasetMultiple,
                    PlotOrientation.VERTICAL,
                    true, true, false);
            
            configurarColoresBarras(barChart);

            BufferedImage barImage = barChart.createBufferedImage(500, 300);
            ByteArrayOutputStream baosBar = new ByteArrayOutputStream();
            ImageIO.write(barImage, "png", baosBar);
            Image itextBarImage = new Image(ImageDataFactory.create(baosBar.toByteArray()));
            itextBarImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            documento.add(itextBarImage);

            documento.add(new Paragraph("\n"));

            // 3.2 Gráfico Circular (Múltiple)
            documento.add(new Paragraph("3.2 Composición Porcentual (Circular)").setBold().setFontSize(12));
            JFreeChart multipleChart = ChartFactory.createMultiplePieChart(
                    "Balance Académico",
                    datasetMultiple,
                    TableOrder.BY_COLUMN,
                    true, true, false);
            
            configurarColoresCircular(multipleChart);

            BufferedImage chartImage = multipleChart.createBufferedImage(500, 350);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            Image itextImage = new Image(ImageDataFactory.create(baos.toByteArray()));
            itextImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            documento.add(itextImage);

            documento.add(new AreaBreak());

            // 5. SECCIÓN: AUDITORÍA Y RESUMEN GENERAL
            documento.add(new Paragraph("4. AUDITORÍA Y RESUMEN GENERAL")
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
                else if (mediaAlumno < 5.0) alumnosMal.add(entry.getKey());
            }

            documento.add(new Paragraph("Estadísticas de Población Estudiantil:").setBold().setMarginTop(10));
            Table tablaResumen = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            tablaResumen.setWidth(UnitValue.createPercentValue(60));
            
            tablaResumen.addCell(new Cell().add(new Paragraph("Total Alumnos:")));
            tablaResumen.addCell(new Cell().add(new Paragraph(String.valueOf(datosAlumnos.size()))));
            tablaResumen.addCell(new Cell().add(new Paragraph("Promedio General Sistema:")));
            tablaResumen.addCell(new Cell().add(new Paragraph(totalRegistros > 0 ? String.format("%.2f", sumaGlobal/totalRegistros) : "0.00")));
            documento.add(tablaResumen);

            documento.add(new Paragraph("\nAlumnos con Rendimiento Destacado (Notable/Sobresaliente):").setBold().setFontColor(new DeviceRgb(0, 100, 0)));
            documento.add(new Paragraph(alumnosBien.isEmpty() ? "Ninguno" : String.join(", ", alumnosBien)).setFontSize(10));

            documento.add(new Paragraph("\nAlumnos en Situación Crítica (Suspenso):").setBold().setFontColor(new DeviceRgb(150, 0, 0)));
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

    private static void updateNota(NotaAlumno nota, String nom, String ape, double t1, double t2, double t3, double parc, double fin) {
        nota.setNombreAlumno(nom);
        nota.setApellidoAlumno(ape);
        nota.setT1(t1);
        nota.setT2(t2);
        nota.setT3(t3);
        nota.setParcial(parc);
        nota.setNotaFinal(fin);
    }

    /**
     * Crea un botón de ayuda con un menú desplegable.
     */
    private static void createHelpSection(Shell shell) {
        Composite compositeHelp = new Composite(shell, SWT.NONE);
        compositeHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        compositeHelp.setLayout(new GridLayout(1, false));

        Button btnHelp = new Button(compositeHelp, SWT.PUSH);
        // Usamos un icono de sistema para la ayuda
        btnHelp.setImage(shell.getDisplay().getSystemImage(SWT.ICON_QUESTION));
        btnHelp.setToolTipText("Menú de Ayuda");
        btnHelp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

        // Menú desplegable
        Menu helpMenu = new Menu(shell, SWT.POP_UP);

        MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
        aboutItem.setText("Acerca de");
        aboutItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                mb.setText("Acerca de");
                mb.setMessage("Sistema de Gestión Académica\nHerramienta integral para el seguimiento de notas y generación de reportes.");
                mb.open();
            }
        });

        MenuItem versionItem = new MenuItem(helpMenu, SWT.PUSH);
        versionItem.setText("Versión");
        versionItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                mb.setText("Versión");
                mb.setMessage("Sistema v1.2.0 - Stable Release");
                mb.open();
            }
        });

        MenuItem guideItem = new MenuItem(helpMenu, SWT.PUSH);
        guideItem.setText("Guía de Usuario");
        guideItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
                mb.setText("Guía de Usuario");
                mb.setMessage("Pasos rápidos:\n1. Use 'Definir Asignaturas' para crear materias.\n2. 'Registrar Notas' para añadir alumnos y sus calificaciones.\n3. 'Ver en Pantalla' para previsualizar el informe.\n4. 'Descargar PDF' para generar el documento profesional.");
                mb.open();
            }
        });

        // Mostrar el menú al hacer clic en el botón
        btnHelp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                org.eclipse.swt.graphics.Rectangle rect = btnHelp.getBounds();
                org.eclipse.swt.graphics.Point pt = new org.eclipse.swt.graphics.Point(rect.x, rect.y + rect.height);
                pt = btnHelp.getParent().toDisplay(pt);
                helpMenu.setLocation(pt.x, pt.y);
                helpMenu.setVisible(true);
            }
        });
    }

    private static void mostrarGraficoBarras(Shell parent) {
        Shell shellGrafico = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        shellGrafico.setText("Rendimiento por Asignatura (Barras)");
        shellGrafico.setLayout(new GridLayout(1, false));
        shellGrafico.setSize(800, 600);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        llenarDatasetCategorias(dataset, listaAsignaturas);

        JFreeChart barChart = ChartFactory.createBarChart(
                "Distribución de Calificaciones",
                "Asignaturas",
                "Cantidad de Alumnos",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        
        configurarColoresBarras(barChart);

        try {
            BufferedImage bufferedImage = barChart.createBufferedImage(750, 500);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(parent.getDisplay(), new ByteArrayInputStream(os.toByteArray()));

            Label labelGrafico = new Label(shellGrafico, SWT.NONE);
            labelGrafico.setImage(image);
            labelGrafico.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

            shellGrafico.addDisposeListener(e -> image.dispose());
            shellGrafico.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mostrarGraficoCircular(Shell parent) {
        Shell shellGrafico = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        shellGrafico.setText("Rendimiento por Asignatura (Circular)");
        shellGrafico.setLayout(new GridLayout(1, false));
        shellGrafico.setSize(800, 600);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        llenarDatasetCategorias(dataset, listaAsignaturas);

        JFreeChart multiplePieChart = ChartFactory.createMultiplePieChart(
                "Distribución Detallada por Asignatura",
                dataset,
                TableOrder.BY_COLUMN,
                true, true, false);

        configurarColoresCircular(multiplePieChart);

        try {
            BufferedImage bufferedImage = multiplePieChart.createBufferedImage(750, 500);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(parent.getDisplay(), new ByteArrayInputStream(os.toByteArray()));

            Label labelGrafico = new Label(shellGrafico, SWT.NONE);
            labelGrafico.setImage(image);
            labelGrafico.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

            shellGrafico.addDisposeListener(e -> image.dispose());
            
            shellGrafico.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void llenarDatasetCategorias(DefaultCategoryDataset dataset, List<Asignatura> asignaturas) {
        for (Asignatura asig : asignaturas) {
            long sob = asig.getNotas().stream().filter(n -> n.getPromedioCuatrimestre() >= 9.0).count();
            long not = asig.getNotas().stream().filter(n -> n.getPromedioCuatrimestre() >= 7.0 && n.getPromedioCuatrimestre() < 9.0).count();
            long apr = asig.getNotas().stream().filter(n -> n.getPromedioCuatrimestre() >= 5.0 && n.getPromedioCuatrimestre() < 7.0).count();
            long sus = asig.getNotas().stream().filter(n -> n.getPromedioCuatrimestre() < 5.0).count();

            dataset.addValue(sob, "Sobresaliente", asig.getNombre());
            dataset.addValue(not, "Notable", asig.getNombre());
            dataset.addValue(apr, "Aprobado", asig.getNombre());
            dataset.addValue(sus, "Suspendido", asig.getNombre());
        }
    }

    private static void configurarColoresBarras(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        
        // Colores por serie
        renderer.setSeriesPaint(0, new java.awt.Color(0, 153, 76));  // Verde (Sob)
        renderer.setSeriesPaint(1, new java.awt.Color(51, 153, 255)); // Azul (Not)
        renderer.setSeriesPaint(2, new java.awt.Color(255, 204, 0));  // Ambar (Apr)
        renderer.setSeriesPaint(3, new java.awt.Color(204, 0, 0));    // Rojo (Sus)
        
        // Mostrar etiquetas de valor sobre las barras
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        
        // Configurar el eje Y para que muestre números enteros y empiece en 0
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
        
        // Ajustar el margen entre las barras de las asignaturas
        renderer.setItemMargin(0.1);
        
        // Fondo más limpio
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
    }

    private static void configurarColoresCircular(JFreeChart chart) {
        if (chart.getPlot() instanceof MultiplePiePlot) {
            MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
            JFreeChart subchart = plot.getPieChart();
            PiePlot subplot = (PiePlot) subchart.getPlot();
            subplot.setSectionPaint("Sobresaliente", new java.awt.Color(0, 153, 76));
            subplot.setSectionPaint("Notable", new java.awt.Color(51, 153, 255));
            subplot.setSectionPaint("Aprobado", new java.awt.Color(255, 204, 0));
            subplot.setSectionPaint("Suspendido", new java.awt.Color(204, 0, 0));
        }
    }
}

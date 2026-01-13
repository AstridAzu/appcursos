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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

/**
 * Clase principal que gestiona la interfaz de usuario (GUI) con SWT.
 * Permite la creación de asignaturas, el registro de notas detalladas
 * y la generación de un informe consolidado con promedios.
 */
public class Main {
    /** Lista global que almacena las asignaturas y sus datos asociados. */
    private static List<Asignatura> listaAsignaturas = new ArrayList<>();

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Gestión Académica - Notas por Asignatura");
        shell.setSize(700, 800);
        shell.setLayout(new GridLayout(1, false));

        // --- SECCIÓN 1: AGREGAR ASIGNATURAS ---
        Group groupAsignatura = new Group(shell, SWT.NONE);
        groupAsignatura.setText("1. Definir Asignaturas (Clases)");
        groupAsignatura.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        groupAsignatura.setLayout(new GridLayout(3, false));

        new Label(groupAsignatura, SWT.NONE).setText("Nombre Asignatura:");
        Text txtNuevaAsignatura = new Text(groupAsignatura, SWT.BORDER);
        txtNuevaAsignatura.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnAddAsignatura = new Button(groupAsignatura, SWT.PUSH);
        btnAddAsignatura.setText("Crear Asignatura");

        // --- SECCIÓN 2: AGREGAR NOTAS A ALUMNOS ---
        Group groupNotas = new Group(shell, SWT.NONE);
        groupNotas.setText("2. Registrar Notas de Alumnos");
        groupNotas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        groupNotas.setLayout(new GridLayout(2, false));

        new Label(groupNotas, SWT.NONE).setText("Seleccionar Asignatura:");
        Combo comboAsignaturas = new Combo(groupNotas, SWT.READ_ONLY);
        comboAsignaturas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite compositeInput = new Composite(groupNotas, SWT.NONE);
        compositeInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        compositeInput.setLayout(new GridLayout(4, false));

        new Label(compositeInput, SWT.NONE).setText("Nombre:");
        Text txtNombre = new Text(compositeInput, SWT.BORDER);
        txtNombre.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(compositeInput, SWT.NONE).setText("Apellido:");
        Text txtApellido = new Text(compositeInput, SWT.BORDER);
        txtApellido.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // --- FILA DE NOTAS ---
        new Label(compositeInput, SWT.NONE).setText("T1:");
        Text txtT1 = new Text(compositeInput, SWT.BORDER);
        txtT1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(compositeInput, SWT.NONE).setText("T2:");
        Text txtT2 = new Text(compositeInput, SWT.BORDER);
        txtT2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(compositeInput, SWT.NONE).setText("T3:");
        Text txtT3 = new Text(compositeInput, SWT.BORDER);
        txtT3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(compositeInput, SWT.NONE).setText("Parcial:");
        Text txtParcial = new Text(compositeInput, SWT.BORDER);
        txtParcial.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(compositeInput, SWT.NONE).setText("Final:");
        Text txtFinal = new Text(compositeInput, SWT.BORDER);
        txtFinal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(compositeInput, SWT.NONE); // Espaciador

        Button btnAddNota = new Button(compositeInput, SWT.PUSH);
        btnAddNota.setText("Registrar Calificaciones");
        btnAddNota.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

        // --- VISTA PREVIA (TABLA) ---
        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String[] headers = {"Asignatura", "Alumno", "T1", "T2", "T3", "Parc.", "Final", "Prom."};
        int[] widths = {100, 120, 40, 40, 40, 50, 50, 60};
        for (int i = 0; i < headers.length; i++) {
            TableColumn col = new TableColumn(table, SWT.NONE);
            col.setText(headers[i]);
            col.setWidth(widths[i]);
        }

        // --- BOTONES DE ACCIÓN ---
        Composite compositeButtons = new Composite(shell, SWT.NONE);
        compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        compositeButtons.setLayout(new GridLayout(2, true));

        Button btnGenerar = new Button(compositeButtons, SWT.PUSH);
        btnGenerar.setText("Generar Informe en Pantalla");
        btnGenerar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnExportarXML = new Button(compositeButtons, SWT.PUSH);
        btnExportarXML.setText("Exportar Datos a XML");
        btnExportarXML.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Text txtInforme = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        txtInforme.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        txtInforme.setEditable(false);
        txtInforme.setFont(new Font(display, "Consolas", 10, SWT.NORMAL));

        // --- LÓGICA DE EVENTOS ---

        btnAddAsignatura.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String nombre = txtNuevaAsignatura.getText().trim();
                if (!nombre.isEmpty()) {
                    Asignatura nueva = new Asignatura(nombre);
                    listaAsignaturas.add(nueva);
                    comboAsignaturas.add(nombre);
                    txtNuevaAsignatura.setText("");
                }
            }
        });

        btnAddNota.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = comboAsignaturas.getSelectionIndex();
                if (index == -1) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING);
                    mb.setMessage("Primero selecciona una asignatura.");
                    mb.open();
                    return;
                }

                try {
                    String nombre = txtNombre.getText().trim();
                    String apellido = txtApellido.getText().trim();
                    double t1 = Double.parseDouble(txtT1.getText().trim());
                    double t2 = Double.parseDouble(txtT2.getText().trim());
                    double t3 = Double.parseDouble(txtT3.getText().trim());
                    double parcial = Double.parseDouble(txtParcial.getText().trim());
                    double notaFinal = Double.parseDouble(txtFinal.getText().trim());

                    double[] notasValidar = {t1, t2, t3, parcial, notaFinal};
                    for (double n : notasValidar) {
                        if (n < 0 || n > 10) throw new NumberFormatException();
                    }

                    NotaAlumno notaAlumno = new NotaAlumno(nombre, apellido, t1, t2, t3, parcial, notaFinal);
                    Asignatura seleccionada = listaAsignaturas.get(index);
                    seleccionada.agregarNota(notaAlumno);

                    // Actualizar Tabla UI
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setText(new String[]{
                        seleccionada.getNombre(), 
                        nombre + " " + apellido, 
                        String.valueOf(t1), String.valueOf(t2), String.valueOf(t3), 
                        String.valueOf(parcial), String.valueOf(notaFinal),
                        String.format("%.2f", notaAlumno.getPromedioCuatrimestre())
                    });

                    // Limpiar
                    txtNombre.setText(""); txtApellido.setText("");
                    txtT1.setText(""); txtT2.setText(""); txtT3.setText("");
                    txtParcial.setText(""); txtFinal.setText("");
                    txtNombre.setFocus();

                } catch (NumberFormatException ex) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
                    mb.setMessage("Datos inválidos. Asegúrate de poner notas de 0 a 10.");
                    mb.open();
                }
            }
        });

        btnGenerar.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                txtInforme.setText(generarTextoInforme(listaAsignaturas));
            }
        });

        btnExportarXML.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (listaAsignaturas.isEmpty()) {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING);
                    mb.setMessage("No hay datos para exportar.");
                    mb.open();
                    return;
                }
                guardarComoXML(listaAsignaturas, shell);
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /**
     * Procesa los datos de las asignaturas almacenadas y genera una representación
     * textual del informe académico.
     * 
     * @param asignaturas Lista de asignaturas con sus respectivos alumnos y notas.
     * @return Una cadena formateada con el desglose de notas y promedios.
     */
    private static String generarTextoInforme(List<Asignatura> asignaturas) {
        if (asignaturas.isEmpty()) return "No hay datos registrados.";

        StringBuilder sb = new StringBuilder();
        sb.append("INFORME ACADÉMICO DETALLADO\n");
        sb.append("========================================================================\n");
        sb.append(String.format("%-20s | %-5s | %-5s | %-5s | %-5s | %-5s | %-7s%n", 
                "Alumno", "T1", "T2", "T3", "Parc.", "Final", "PROM.IND"));
        sb.append("------------------------------------------------------------------------\n\n");

        double sumaTotalPromedios = 0;
        int totalAlumnos = 0;

        for (Asignatura asig : asignaturas) {
            sb.append("ASIGNATURA: ").append(asig.getNombre().toUpperCase()).append("\n");
            
            if (asig.getNotas().isEmpty()) {
                sb.append("  (Sin registros)\n");
            } else {
                for (NotaAlumno n : asig.getNotas()) {
                    sb.append(String.format("%-20s | %5.2f | %5.2f | %5.2f | %5.2f | %5.2f | %7.2f%n", 
                            n.getNombreAlumno() + " " + n.getApellidoAlumno(),
                            n.getT1(), n.getT2(), n.getT3(), n.getParcial(), n.getNotaFinal(),
                            n.getPromedioCuatrimestre()));
                    sumaTotalPromedios += n.getPromedioCuatrimestre();
                    totalAlumnos++;
                }
                sb.append(String.format(">> PROMEDIO ASIGNATURA [%s]: %.2f%n", asig.getNombre(), asig.getMedia()));
            }
            sb.append("\n");
        }

        if (totalAlumnos > 0) {
            double promedioGeneral = sumaTotalPromedios / totalAlumnos;
            sb.append("========================================================================\n");
            sb.append(String.format("PROMEDIO GENERAL DE LA INSTITUCIÓN: %.2f%n", promedioGeneral));
            sb.append("========================================================================\n");
        }

        return sb.toString();
    }

    /**
     * Exporta toda la estructura de asignaturas y alumnos a un archivo XML.
     * 
     * @param asignaturas Lista de datos a exportar.
     * @param shell Ventana principal para mostrar mensajes de confirmación o error.
     */
    private static void guardarComoXML(List<Asignatura> asignaturas, Shell shell) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Raíz
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Academia");
            doc.appendChild(rootElement);

            for (Asignatura asig : asignaturas) {
                Element asigNode = doc.createElement("Asignatura");
                asigNode.setAttribute("nombre", asig.getNombre());
                asigNode.setAttribute("media_clase", String.format("%.2f", asig.getMedia()));
                rootElement.appendChild(asigNode);

                for (NotaAlumno nota : asig.getNotas()) {
                    Element alumnoNode = doc.createElement("Alumno");
                    alumnoNode.setAttribute("nombre", nota.getNombreAlumno());
                    alumnoNode.setAttribute("apellido", nota.getApellidoAlumno());
                    asigNode.appendChild(alumnoNode);

                    // Notas individuales
                    addXmlChild(doc, alumnoNode, "T1", String.valueOf(nota.getT1()));
                    addXmlChild(doc, alumnoNode, "T2", String.valueOf(nota.getT2()));
                    addXmlChild(doc, alumnoNode, "T3", String.valueOf(nota.getT3()));
                    addXmlChild(doc, alumnoNode, "Parcial", String.valueOf(nota.getParcial()));
                    addXmlChild(doc, alumnoNode, "Final", String.valueOf(nota.getNotaFinal()));
                    addXmlChild(doc, alumnoNode, "PromedioIndividual", String.format("%.2f", nota.getPromedioCuatrimestre()));
                }
            }

            // Guardar el archivo
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("datos_academicos.xml"));

            transformer.transform(source, result);

            MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
            mb.setMessage("Datos guardados automáticamente en: datos_academicos.xml");
            mb.open();

        } catch (Exception e) {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
            mb.setMessage("Error al generar XML: " + e.getMessage());
            mb.open();
        }
    }

    /**
     * Método auxiliar para añadir elementos hijos a un nodo XML.
     */
    private static void addXmlChild(Document doc, Element parent, String name, String value) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(value));
        parent.appendChild(child);
    }
}

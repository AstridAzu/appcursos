package com.estudiantes.notas;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una asignatura o curso académico.
 * Contiene una lista de alumnos con sus respectivas notas y gestiona el promedio de la clase.
 */
public class Asignatura {
    private String nombre;
    private List<NotaAlumno> notas;

    /**
     * Crea una nueva asignatura.
     * @param nombre El nombre identificativo de la asignatura.
     */
    public Asignatura(String nombre) {
        this.nombre = nombre;
        this.notas = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public List<NotaAlumno> getNotas() {
        return notas;
    }

    /**
     * Registra el historial de notas de un alumno en esta asignatura.
     * @param nota Objeto que contiene las notas del alumno.
     */
    public void agregarNota(NotaAlumno nota) {
        this.notas.add(nota);
    }

    /**
     * Calcula la nota media de todos los alumnos inscritos en esta asignatura.
     * @return El promedio de los promedios individuales de los alumnos.
     */
    public double getMedia() {
        if (notas.isEmpty()) return 0.0;
        return notas.stream()
                .mapToDouble(NotaAlumno::getPromedioCuatrimestre)
                .average()
                .orElse(0.0);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
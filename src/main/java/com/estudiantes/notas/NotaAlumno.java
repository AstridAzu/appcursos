package com.estudiantes.notas;

/**
 * Representa las calificaciones detalladas de un alumno en una asignatura.
 * Almacena las notas de tareas, parcial y examen final.
 */
public class NotaAlumno {
    private String nombreAlumno;
    private String apellidoAlumno;
    private double t1, t2, t3, parcial, notaFinal;

    /**
     * Construye un nuevo registro de notas para un alumno.
     * 
     * @param nombreAlumno Nombre del estudiante.
     * @param apellidoAlumno Apellido del estudiante.
     * @param t1 Nota de la Tarea 1.
     * @param t2 Nota de la Tarea 2.
     * @param t3 Nota de la Tarea 3.
     * @param parcial Nota del examen parcial.
     * @param notaFinal Nota del examen final.
     */
    public NotaAlumno(String nombreAlumno, String apellidoAlumno, double t1, double t2, double t3, double parcial, double notaFinal) {
        this.nombreAlumno = nombreAlumno;
        this.apellidoAlumno = apellidoAlumno;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.parcial = parcial;
        this.notaFinal = notaFinal;
    }

    public String getNombreAlumno() { return nombreAlumno; }
    public String getApellidoAlumno() { return apellidoAlumno; }
    public double getT1() { return t1; }
    public double getT2() { return t2; }
    public double getT3() { return t3; }
    public double getParcial() { return parcial; }
    public double getNotaFinal() { return notaFinal; }

    /**
     * Calcula el promedio individual del alumno basándose en sus 5 notas.
     * @return El promedio aritmético de las calificaciones.
     */
    public double getPromedioCuatrimestre() {
        return (t1 + t2 + t3 + parcial + notaFinal) / 5.0;
    }

    @Override
    public String toString() {
        return String.format("%s %s (Promedio: %.2f)", nombreAlumno, apellidoAlumno, getPromedioCuatrimestre());
    }
}

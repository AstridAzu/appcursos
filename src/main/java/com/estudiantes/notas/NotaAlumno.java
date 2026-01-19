package com.estudiantes.notas;

/**
 * Representa las notas de un alumno individual en una asignatura.
 * Calcula automáticamente el promedio del cuatrimestre.
 */
public class NotaAlumno {
    private String nombreAlumno;
    private String apellidoAlumno;
    private double t1;
    private double t2;
    private double t3;
    private double parcial;
    private double notaFinal;

    /**
     * Constructor que inicializa todas las notas del alumno.
     * @param nombreAlumno Nombre del estudiante
     * @param apellidoAlumno Apellido del estudiante
     * @param t1 Nota del primer trabajo/tarea
     * @param t2 Nota del segundo trabajo/tarea
     * @param t3 Nota del tercer trabajo/tarea
     * @param parcial Nota del examen parcial
     * @param notaFinal Nota del examen final
     */
    public NotaAlumno(String nombreAlumno, String apellidoAlumno,
                      double t1, double t2, double t3,
                      double parcial, double notaFinal) {
        this.nombreAlumno = nombreAlumno;
        this.apellidoAlumno = apellidoAlumno;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.parcial = parcial;
        this.notaFinal = notaFinal;
    }

    // Getters
    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public String getApellidoAlumno() {
        return apellidoAlumno;
    }

    public double getT1() {
        return t1;
    }

    public double getT2() {
        return t2;
    }

    public double getT3() {
        return t3;
    }

    public double getParcial() {
        return parcial;
    }

    public double getNotaFinal() {
        return notaFinal;
    }

    // Setters
    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public void setApellidoAlumno(String apellidoAlumno) {
        this.apellidoAlumno = apellidoAlumno;
    }

    public void setT1(double t1) {
        this.t1 = t1;
    }

    public void setT2(double t2) {
        this.t2 = t2;
    }

    public void setT3(double t3) {
        this.t3 = t3;
    }

    public void setParcial(double parcial) {
        this.parcial = parcial;
    }

    public void setNotaFinal(double notaFinal) {
        this.notaFinal = notaFinal;
    }

    /**
     * Calcula el promedio ponderado del cuatrimestre.
     * Fórmula: (T1 + T2 + T3 + Parcial + Final) / 5
     * @return El promedio calculado
     */
    public double getPromedioCuatrimestre() {
        return (t1 + t2 + t3 + parcial + notaFinal) / 5.0;
    }

    @Override
    public String toString() {
        return nombreAlumno + " " + apellidoAlumno +
                " - Promedio: " + String.format("%.2f", getPromedioCuatrimestre());
    }
}
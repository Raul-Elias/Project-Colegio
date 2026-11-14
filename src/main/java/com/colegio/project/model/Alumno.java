package com.colegio.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    @Column(unique = true)
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El grado es obligatorio")
    private String grado;

    @NotNull(message = "El promedio es obligatorio")
    @DecimalMin(value = "0", message = "El promedio debe estar entre 0 y 20")
    @DecimalMax(value = "20", message = "El promedio debe estar entre 0 y 20")
    private Double promedio;

    private Double asistencia = 95.0;
    private String continuidad = "INDECISO"; // CONTINUA | NO_CONTINUA | INDECISO
    private String motivoNoContinua;
    private java.time.LocalDate fechaMatricula = java.time.LocalDate.now();

    public Double getAsistencia() { return asistencia; }
    public void setAsistencia(Double v) { this.asistencia = v; }
    public String getContinuidad() { return continuidad; }
    public void setContinuidad(String v) { this.continuidad = v; }
    public String getMotivoNoContinua() { return motivoNoContinua; }
    public void setMotivoNoContinua(String v) { this.motivoNoContinua = v; }
    public java.time.LocalDate getFechaMatricula() { return fechaMatricula; }
    public void setFechaMatricula(java.time.LocalDate v) { this.fechaMatricula = v; }

    public Alumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getGrado() { return grado; }
    public void setGrado(String grado) { this.grado = grado; }

    public Double getPromedio() { return promedio; }
    public void setPromedio(Double promedio) { this.promedio = promedio; }
}
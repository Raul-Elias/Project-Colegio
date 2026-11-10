package com.colegio.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;

// HU-08: un alumno queda matriculado en una sección durante un año escolar.
// El estado (ACTIVA, RETIRADA, TRASLADADA) lo usará HU-09 para retiro, traslado y reactivación.
@Entity
@Table(name = "matriculas")
public class Matricula {

    public static final String ACTIVA = "ACTIVA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    @Column(nullable = false)
    private Integer anioEscolar;

    @Column(nullable = false)
    private String estado = ACTIVA;

    @Column(nullable = false)
    private LocalDate fechaMatricula = LocalDate.now();

    public Matricula() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Seccion getSeccion() { return seccion; }
    public void setSeccion(Seccion seccion) { this.seccion = seccion; }

    public Integer getAnioEscolar() { return anioEscolar; }
    public void setAnioEscolar(Integer anioEscolar) { this.anioEscolar = anioEscolar; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaMatricula() { return fechaMatricula; }
    public void setFechaMatricula(LocalDate fechaMatricula) { this.fechaMatricula = fechaMatricula; }

    // Datos planos para la pantalla
    @JsonProperty("alumnoId")
    public Long getAlumnoId() { return alumno != null ? alumno.getId() : null; }

    @JsonProperty("alumnoDni")
    public String getAlumnoDni() { return alumno != null ? alumno.getDni() : null; }

    @JsonProperty("alumnoNombre")
    public String getAlumnoNombre() {
        return alumno != null ? alumno.getApellido() + ", " + alumno.getNombres() : null;
    }

    @JsonProperty("seccionId")
    public Long getSeccionId() { return seccion != null ? seccion.getId() : null; }

    @JsonProperty("gradoNombre")
    public String getGradoNombre() { return seccion != null ? seccion.getGradoNombre() : null; }

    @JsonProperty("seccionNombre")
    public String getSeccionNombre() { return seccion != null ? seccion.getNombre() : null; }
}

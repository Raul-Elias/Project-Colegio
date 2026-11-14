package com.colegio.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalTime;

// HU-10: un bloque del horario = curso dictado por un docente en una sección, un día y una franja horaria.
@Entity
@Table(name = "horarios")
public class Horario {

    public static final java.util.List<String> DIAS =
            java.util.List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anioEscolar;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @Column(nullable = false)
    private String diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    public Horario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAnioEscolar() { return anioEscolar; }
    public void setAnioEscolar(Integer anioEscolar) { this.anioEscolar = anioEscolar; }

    public Seccion getSeccion() { return seccion; }
    public void setSeccion(Seccion seccion) { this.seccion = seccion; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    // Datos planos para la pantalla
    @JsonProperty("seccionId")
    public Long getSeccionId() { return seccion != null ? seccion.getId() : null; }

    @JsonProperty("seccionNombre")
    public String getSeccionNombre() {
        return seccion != null ? seccion.getGradoNombre() + " - Sección " + seccion.getNombre() : null;
    }

    @JsonProperty("cursoId")
    public Long getCursoId() { return curso != null ? curso.getId() : null; }

    @JsonProperty("cursoNombre")
    public String getCursoNombre() { return curso != null ? curso.getNombre() : null; }

    @JsonProperty("docenteId")
    public Long getDocenteId() { return docente != null ? docente.getId() : null; }

    @JsonProperty("docenteNombre")
    public String getDocenteNombre() { return docente != null ? docente.getNombres() : null; }
}

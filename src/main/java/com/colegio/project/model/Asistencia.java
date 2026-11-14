package com.colegio.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

// HU-16: asistencia diaria. Un registro = el estado de un alumno (por su matrícula) en una fecha.
// Se guarda también la sección donde se tomó, para que el historial no cambie si luego lo trasladan.
@Entity
@Table(name = "asistencias",
       uniqueConstraints = @UniqueConstraint(columnNames = {"matricula_id", "fecha"}))
public class Asistencia {

    public static final String PRESENTE = "PRESENTE";
    public static final String TARDANZA = "TARDANZA";
    public static final String FALTA = "FALTA";
    public static final String JUSTIFICADA = "JUSTIFICADA";

    public static final List<String> ESTADOS = List.of(PRESENTE, TARDANZA, FALTA, JUSTIFICADA);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String estado = PRESENTE;

    @Column(length = 200)
    private String observacion;

    public Asistencia() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Matricula getMatricula() { return matricula; }
    public void setMatricula(Matricula matricula) { this.matricula = matricula; }

    public Seccion getSeccion() { return seccion; }
    public void setSeccion(Seccion seccion) { this.seccion = seccion; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    // Datos planos para la pantalla
    @JsonProperty("matriculaId")
    public Long getMatriculaId() { return matricula != null ? matricula.getId() : null; }

    @JsonProperty("seccionId")
    public Long getSeccionId() { return seccion != null ? seccion.getId() : null; }
}

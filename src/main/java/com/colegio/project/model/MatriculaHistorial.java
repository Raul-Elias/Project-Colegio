package com.colegio.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

// HU-09: registro de cada hecho de una matrícula (matrícula, retiro, traslado, reactivación).
// Las secciones se guardan como texto para que el historial no dependa de que la sección siga existiendo.
@Entity
@Table(name = "matricula_historial")
public class MatriculaHistorial {

    public static final String MATRICULA = "MATRICULA";
    public static final String RETIRO = "RETIRO";
    public static final String TRASLADO = "TRASLADO";
    public static final String REACTIVACION = "REACTIVACION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @Column(nullable = false)
    private String accion;

    private String seccionOrigen;
    private String seccionDestino;

    @Column(length = 500)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public MatriculaHistorial() {}

    public MatriculaHistorial(Matricula matricula, String accion, String seccionOrigen,
                              String seccionDestino, String motivo) {
        this.matricula = matricula;
        this.accion = accion;
        this.seccionOrigen = seccionOrigen;
        this.seccionDestino = seccionDestino;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public Matricula getMatricula() { return matricula; }
    public String getAccion() { return accion; }
    public String getSeccionOrigen() { return seccionOrigen; }
    public String getSeccionDestino() { return seccionDestino; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }
}

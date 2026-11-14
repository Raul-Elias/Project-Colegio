package com.colegio.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "encuestas")
public class Encuesta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dniAlumno;
    private String rol = "PADRE"; // PADRE | ALUMNO
    @NotNull @Min(1) @Max(5)
    private Integer puntaje;
    @Min(0) @Max(10)
    private Integer recomienda;
    private String tema = "General";
    @Column(length = 1000)
    private String comentario;
    private LocalDateTime fecha = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getDniAlumno() { return dniAlumno; }
    public void setDniAlumno(String v) { dniAlumno = v; }
    public String getRol() { return rol; }
    public void setRol(String v) { rol = v; }
    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer v) { puntaje = v; }
    public Integer getRecomienda() { return recomienda; }
    public void setRecomienda(Integer v) { recomienda = v; }
    public String getTema() { return tema; }
    public void setTema(String v) { tema = v; }
    public String getComentario() { return comentario; }
    public void setComentario(String v) { comentario = v; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime v) { fecha = v; }
}

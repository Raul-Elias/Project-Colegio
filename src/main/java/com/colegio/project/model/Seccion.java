package com.colegio.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

// Una sección (A, B, C...) pertenece a un grado. No se repite el nombre dentro del mismo grado.
@Entity
@Table(name = "secciones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "grado_id"}))
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "grado_id")
    private Grado grado;

    public Seccion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Grado getGrado() { return grado; }
    public void setGrado(Grado grado) { this.grado = grado; }

    // Datos planos para que la pantalla los muestre fácil
    @JsonProperty("gradoId")
    public Long getGradoId() { return grado != null ? grado.getId() : null; }

    @JsonProperty("gradoNombre")
    public String getGradoNombre() { return grado != null ? grado.etiqueta() : null; }
}

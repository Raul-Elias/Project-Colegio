package com.colegio.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
// El mismo nombre de grado puede existir en niveles distintos (1° Grado de Primaria y 1° Grado de Secundaria).
// Lo que no se repite es la pareja nombre + nivel.
@Table(name = "grados",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "nivel"}))
public class Grado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del grado es obligatorio")
    private String nombre;

    @NotBlank(message = "El nivel es obligatorio")
    @Pattern(regexp = "INICIAL|PRIMARIA|SECUNDARIA", message = "El nivel debe ser INICIAL, PRIMARIA o SECUNDARIA")
    private String nivel;

    public Grado() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    // Nombre y nivel juntos, ej. "1° Grado (Primaria)". No empieza con "get" para que no salga en el JSON.
    public String etiqueta() {
        if (nivel == null || nivel.isBlank()) return nombre;
        String n = nivel.substring(0, 1).toUpperCase() + nivel.substring(1).toLowerCase();
        return nombre + " (" + n + ")";
    }
}

package com.colegio.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String correo;
    private String password;
    private String rol;
    private Boolean primerIngreso = true;
    private Boolean terminosAceptados = false;

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getPrimerIngreso() { return primerIngreso; }
    public void setPrimerIngreso(Boolean primerIngreso) { this.primerIngreso = primerIngreso; }

    public Boolean getTerminosAceptados() { return terminosAceptados; }
    public void setTerminosAceptados(Boolean terminosAceptados) { this.terminosAceptados = terminosAceptados; }
}

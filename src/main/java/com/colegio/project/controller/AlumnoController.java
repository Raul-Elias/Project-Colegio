package com.colegio.project.controller;

import com.colegio.project.model.Alumno;
import com.colegio.project.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    @Autowired
    private AlumnoService alumnoService;
    @Autowired
    private com.colegio.project.service.SheetsSyncService sheets;

    @GetMapping
    public ResponseEntity<List<Alumno>> listar() {
        return ResponseEntity.ok(alumnoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Alumno alumno) {
        if (alumnoService.dniEnUso(alumno.getDni(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe un alumno con ese DNI"));
        }
        alumno.setId(null); 
        Alumno g = alumnoService.guardar(alumno);
        sheets.sync();
        return ResponseEntity.status(HttpStatus.CREATED).body(g);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Alumno datos) {
        Optional<Alumno> existente = alumnoService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El alumno no existe"));
        }
        if (alumnoService.dniEnUso(datos.getDni(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe otro alumno con ese DNI"));
        }
        Alumno alumno = existente.get();
        alumno.setDni(datos.getDni());
        alumno.setNombres(datos.getNombres());
        alumno.setApellido(datos.getApellido());
        alumno.setGrado(datos.getGrado());
        alumno.setPromedio(datos.getPromedio());
        if (datos.getAsistencia() != null) alumno.setAsistencia(datos.getAsistencia());
        if (datos.getContinuidad() != null) alumno.setContinuidad(datos.getContinuidad());
        if (datos.getMotivoNoContinua() != null) alumno.setMotivoNoContinua(datos.getMotivoNoContinua());
        Alumno g = alumnoService.guardar(alumno);
        sheets.sync();
        return ResponseEntity.ok(g);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (alumnoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El alumno no existe"));
        }
        alumnoService.eliminar(id);
        sheets.sync();
        return ResponseEntity.ok(Map.of("mensaje", "Alumno eliminado"));
    }
}
package com.colegio.project.controller;

import com.colegio.project.model.Docente;
import com.colegio.project.service.DocenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    @Autowired
    private DocenteService docenteService;

    @GetMapping
    public ResponseEntity<List<Docente>> listar() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Docente docente) {
        if (docenteService.dniEnUso(docente.getDni(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe un docente con ese DNI"));
        }
        docente.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteService.guardar(docente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Docente datos) {
        Optional<Docente> existente = docenteService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El docente no existe"));
        }
        if (docenteService.dniEnUso(datos.getDni(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe otro docente con ese DNI"));
        }
        Docente docente = existente.get();
        docente.setDni(datos.getDni());
        docente.setNombres(datos.getNombres());
        docente.setEspecialidad(datos.getEspecialidad());
        docente.setCorreo(datos.getCorreo());
        docente.setTelefono(datos.getTelefono());
        return ResponseEntity.ok(docenteService.guardar(docente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (docenteService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El docente no existe"));
        }
        docenteService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Docente eliminado"));
    }
}
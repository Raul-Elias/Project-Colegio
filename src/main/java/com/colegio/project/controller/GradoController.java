package com.colegio.project.controller;

import com.colegio.project.model.Grado;
import com.colegio.project.service.GradoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/grados")
public class GradoController {

    @Autowired
    private GradoService gradoService;

    @GetMapping
    public ResponseEntity<List<Grado>> listar() {
        return ResponseEntity.ok(gradoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Grado grado) {
        if (gradoService.nombreEnUso(grado.getNombre(), grado.getNivel(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe un grado con ese nombre en el nivel " + grado.getNivel()));
        }
        grado.setId(null);
        grado.setNombre(grado.getNombre().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(gradoService.guardar(grado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Grado datos) {
        Optional<Grado> existente = gradoService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El grado no existe"));
        }
        if (gradoService.nombreEnUso(datos.getNombre(), datos.getNivel(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe otro grado con ese nombre en el nivel " + datos.getNivel()));
        }
        Grado grado = existente.get();
        grado.setNombre(datos.getNombre().trim());
        grado.setNivel(datos.getNivel());
        return ResponseEntity.ok(gradoService.guardar(grado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (gradoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El grado no existe"));
        }
        if (gradoService.tieneDependencias(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "No se puede eliminar: el grado tiene secciones o cursos"));
        }
        gradoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Grado eliminado"));
    }
}

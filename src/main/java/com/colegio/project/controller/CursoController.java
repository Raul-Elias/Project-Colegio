package com.colegio.project.controller;

import com.colegio.project.model.Curso;
import com.colegio.project.model.Grado;
import com.colegio.project.service.CursoService;
import com.colegio.project.service.GradoService;
import com.colegio.project.service.HorarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private GradoService gradoService;

    @Autowired
    private HorarioService horarioService;


    // Datos que llegan desde la pantalla
    public static class CursoRequest {
        @NotBlank(message = "El nombre del curso es obligatorio")
        private String nombre;

        @NotNull(message = "Debe elegir un grado")
        private Long gradoId;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Long getGradoId() { return gradoId; }
        public void setGradoId(Long gradoId) { this.gradoId = gradoId; }
    }

    @GetMapping
    public ResponseEntity<List<Curso>> listar() {
        return ResponseEntity.ok(cursoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CursoRequest datos) {
        Optional<Grado> grado = gradoService.buscarPorId(datos.getGradoId());
        if (grado.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El grado elegido no existe"));
        }
        String nombre = datos.getNombre().trim();
        if (cursoService.nombreEnUso(nombre, datos.getGradoId(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe ese curso en el grado elegido"));
        }
        Curso curso = new Curso();
        curso.setNombre(nombre);
        curso.setGrado(grado.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.guardar(curso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest datos) {
        Optional<Curso> existente = cursoService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El curso no existe"));
        }
        Optional<Grado> grado = gradoService.buscarPorId(datos.getGradoId());
        if (grado.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El grado elegido no existe"));
        }
        String nombre = datos.getNombre().trim();
        if (cursoService.nombreEnUso(nombre, datos.getGradoId(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe ese curso en el grado elegido"));
        }
        Curso curso = existente.get();
        curso.setNombre(nombre);
        curso.setGrado(grado.get());
        return ResponseEntity.ok(cursoService.guardar(curso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (cursoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "El curso no existe"));
        }
        if (horarioService.cursoTieneHorarios(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "No se puede eliminar: el curso tiene horarios registrados"));
        }
        cursoService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Curso eliminado"));
    }
}

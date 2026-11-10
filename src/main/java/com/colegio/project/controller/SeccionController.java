package com.colegio.project.controller;

import com.colegio.project.model.Grado;
import com.colegio.project.model.Seccion;
import com.colegio.project.service.AsistenciaService;
import com.colegio.project.service.GradoService;
import com.colegio.project.service.HorarioService;
import com.colegio.project.service.MatriculaService;
import com.colegio.project.service.SeccionService;
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
@RequestMapping("/api/secciones")
public class SeccionController {

    @Autowired
    private SeccionService seccionService;

    @Autowired
    private GradoService gradoService;

    @Autowired
    private HorarioService horarioService;


    @Autowired
    private MatriculaService matriculaService;

    @Autowired
    private AsistenciaService asistenciaService;

    // Datos que llegan desde la pantalla
    public static class SeccionRequest {
        @NotBlank(message = "El nombre de la sección es obligatorio")
        private String nombre;

        @NotNull(message = "Debe elegir un grado")
        private Long gradoId;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Long getGradoId() { return gradoId; }
        public void setGradoId(Long gradoId) { this.gradoId = gradoId; }
    }

    @GetMapping
    public ResponseEntity<List<Seccion>> listar() {
        return ResponseEntity.ok(seccionService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody SeccionRequest datos) {
        Optional<Grado> grado = gradoService.buscarPorId(datos.getGradoId());
        if (grado.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El grado elegido no existe"));
        }
        String nombre = datos.getNombre().trim();
        if (seccionService.nombreEnUso(nombre, datos.getGradoId(), null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe esa sección en el grado elegido"));
        }
        Seccion seccion = new Seccion();
        seccion.setNombre(nombre);
        seccion.setGrado(grado.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(seccionService.guardar(seccion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody SeccionRequest datos) {
        Optional<Seccion> existente = seccionService.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "La sección no existe"));
        }
        Optional<Grado> grado = gradoService.buscarPorId(datos.getGradoId());
        if (grado.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El grado elegido no existe"));
        }
        String nombre = datos.getNombre().trim();
        if (seccionService.nombreEnUso(nombre, datos.getGradoId(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe esa sección en el grado elegido"));
        }
        Seccion seccion = existente.get();
        seccion.setNombre(nombre);
        seccion.setGrado(grado.get());
        return ResponseEntity.ok(seccionService.guardar(seccion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (seccionService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "La sección no existe"));
        }
        if (matriculaService.seccionTieneMatriculas(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "No se puede eliminar: la sección tiene alumnos matriculados"));
        }
        if (horarioService.seccionTieneHorarios(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "No se puede eliminar: la sección tiene horarios registrados"));
        }
        if (asistenciaService.seccionTieneAsistencias(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "No se puede eliminar: la sección tiene asistencias registradas"));
        }
        seccionService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Sección eliminada"));
    }
}

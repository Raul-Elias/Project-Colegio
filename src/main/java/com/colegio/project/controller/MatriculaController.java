package com.colegio.project.controller;

import com.colegio.project.model.Alumno;
import com.colegio.project.model.Matricula;
import com.colegio.project.model.MatriculaHistorial;
import com.colegio.project.model.Seccion;
import com.colegio.project.service.AlumnoService;
import com.colegio.project.service.MatriculaService;
import com.colegio.project.service.SeccionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// HU-08: Matricular a un alumno por sección y año escolar
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    @Autowired
    private MatriculaService matriculaService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private SeccionService seccionService;

    // Datos que llegan desde la pantalla
    public static class MatriculaRequest {
        @NotNull(message = "Debe elegir un alumno")
        private Long alumnoId;

        @NotNull(message = "Debe elegir una sección")
        private Long seccionId;

        @NotNull(message = "El año escolar es obligatorio")
        @Min(value = 2000, message = "El año escolar no es válido")
        @Max(value = 2100, message = "El año escolar no es válido")
        private Integer anioEscolar;

        public Long getAlumnoId() { return alumnoId; }
        public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }
        public Long getSeccionId() { return seccionId; }
        public void setSeccionId(Long seccionId) { this.seccionId = seccionId; }
        public Integer getAnioEscolar() { return anioEscolar; }
        public void setAnioEscolar(Integer anioEscolar) { this.anioEscolar = anioEscolar; }
    }

    @GetMapping
    public ResponseEntity<List<Matricula>> listar(@RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(matriculaService.listar(anio));
    }

    @PostMapping
    public ResponseEntity<?> matricular(@Valid @RequestBody MatriculaRequest datos) {
        Optional<Alumno> alumno = alumnoService.buscarPorId(datos.getAlumnoId());
        if (alumno.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El alumno elegido no existe"));
        }
        Optional<Seccion> seccion = seccionService.buscarPorId(datos.getSeccionId());
        if (seccion.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "La sección elegida no existe"));
        }
        if (matriculaService.yaMatriculado(datos.getAlumnoId(), datos.getAnioEscolar())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El alumno ya tiene una matrícula activa en el año escolar "
                            + datos.getAnioEscolar()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matriculaService.matricular(alumno.get(), seccion.get(), datos.getAnioEscolar()));
    }

    // ---------- HU-09: retiro, traslado, reactivación e historial ----------

    public static class AccionRequest {
        private String motivo;
        private Long seccionId;

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
        public Long getSeccionId() { return seccionId; }
        public void setSeccionId(Long seccionId) { this.seccionId = seccionId; }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }

    private String limpiar(String motivo) {
        return motivo == null || motivo.isBlank() ? null : motivo.trim();
    }

    @PutMapping("/{id}/retirar")
    public ResponseEntity<?> retirar(@PathVariable Long id, @RequestBody AccionRequest datos) {
        Optional<Matricula> m = matriculaService.buscarPorId(id);
        if (m.isEmpty()) return error(HttpStatus.NOT_FOUND, "La matrícula no existe");
        String motivo = limpiar(datos.getMotivo());
        if (motivo == null) return error(HttpStatus.BAD_REQUEST, "El motivo del retiro es obligatorio");
        if (!Matricula.ACTIVA.equals(m.get().getEstado())) {
            return error(HttpStatus.CONFLICT, "Solo se puede retirar una matrícula activa");
        }
        return ResponseEntity.ok(matriculaService.retirar(m.get(), motivo));
    }

    @PutMapping("/{id}/trasladar")
    public ResponseEntity<?> trasladar(@PathVariable Long id, @RequestBody AccionRequest datos) {
        Optional<Matricula> m = matriculaService.buscarPorId(id);
        if (m.isEmpty()) return error(HttpStatus.NOT_FOUND, "La matrícula no existe");
        if (!Matricula.ACTIVA.equals(m.get().getEstado())) {
            return error(HttpStatus.CONFLICT, "Solo se puede trasladar una matrícula activa");
        }
        if (datos.getSeccionId() == null) {
            return error(HttpStatus.BAD_REQUEST, "Debe elegir la sección de destino");
        }
        Optional<Seccion> destino = seccionService.buscarPorId(datos.getSeccionId());
        if (destino.isEmpty()) return error(HttpStatus.BAD_REQUEST, "La sección elegida no existe");
        if (destino.get().getId().equals(m.get().getSeccionId())) {
            return error(HttpStatus.CONFLICT, "El alumno ya está en esa sección");
        }
        return ResponseEntity.ok(
                matriculaService.trasladar(m.get(), destino.get(), limpiar(datos.getMotivo())));
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivar(@PathVariable Long id, @RequestBody(required = false) AccionRequest datos) {
        Optional<Matricula> m = matriculaService.buscarPorId(id);
        if (m.isEmpty()) return error(HttpStatus.NOT_FOUND, "La matrícula no existe");
        if (!"RETIRADA".equals(m.get().getEstado())) {
            return error(HttpStatus.CONFLICT, "Solo se puede reactivar una matrícula retirada");
        }
        if (matriculaService.yaMatriculado(m.get().getAlumnoId(), m.get().getAnioEscolar())) {
            return error(HttpStatus.CONFLICT, "El alumno ya tiene otra matrícula activa en el año escolar "
                    + m.get().getAnioEscolar());
        }
        return ResponseEntity.ok(
                matriculaService.reactivar(m.get(), datos == null ? null : limpiar(datos.getMotivo())));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> historial(@PathVariable Long id) {
        if (matriculaService.buscarPorId(id).isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "La matrícula no existe");
        }
        List<MatriculaHistorial> lista = matriculaService.historial(id);
        return ResponseEntity.ok(lista);
    }
}

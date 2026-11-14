package com.colegio.project.controller;

import com.colegio.project.model.Curso;
import com.colegio.project.model.Docente;
import com.colegio.project.model.Horario;
import com.colegio.project.model.Seccion;
import com.colegio.project.service.CursoService;
import com.colegio.project.service.DocenteService;
import com.colegio.project.service.HorarioService;
import com.colegio.project.service.SeccionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// HU-10: Armar horarios sin cruces
@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private SeccionService seccionService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private DocenteService docenteService;

    // Datos que llegan desde la pantalla
    public static class HorarioRequest {
        @NotNull(message = "El año escolar es obligatorio")
        @Min(value = 2000, message = "El año escolar no es válido")
        @Max(value = 2100, message = "El año escolar no es válido")
        private Integer anioEscolar;

        @NotNull(message = "Debe elegir una sección")
        private Long seccionId;

        @NotNull(message = "Debe elegir un curso")
        private Long cursoId;

        @NotNull(message = "Debe elegir un docente")
        private Long docenteId;

        @NotNull(message = "Debe elegir el día")
        @Pattern(regexp = "LUNES|MARTES|MIERCOLES|JUEVES|VIERNES",
                 message = "El día debe ser de lunes a viernes")
        private String diaSemana;

        @NotNull(message = "La hora de inicio es obligatoria")
        private LocalTime horaInicio;

        @NotNull(message = "La hora de fin es obligatoria")
        private LocalTime horaFin;

        public Integer getAnioEscolar() { return anioEscolar; }
        public void setAnioEscolar(Integer anioEscolar) { this.anioEscolar = anioEscolar; }
        public Long getSeccionId() { return seccionId; }
        public void setSeccionId(Long seccionId) { this.seccionId = seccionId; }
        public Long getCursoId() { return cursoId; }
        public void setCursoId(Long cursoId) { this.cursoId = cursoId; }
        public Long getDocenteId() { return docenteId; }
        public void setDocenteId(Long docenteId) { this.docenteId = docenteId; }
        public String getDiaSemana() { return diaSemana; }
        public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }

    @GetMapping
    public ResponseEntity<List<Horario>> listar(@RequestParam(required = false) Integer anio,
                                                @RequestParam(required = false) Long seccionId,
                                                @RequestParam(required = false) Long docenteId) {
        return ResponseEntity.ok(horarioService.listar(anio, seccionId, docenteId));
    }

    // Valida los datos y arma el bloque; devuelve el error si algo falla
    private Object armar(HorarioRequest d, Horario horario, Long idActual) {
        if (!d.getHoraInicio().isBefore(d.getHoraFin())) {
            return error(HttpStatus.BAD_REQUEST, "La hora de inicio debe ser menor que la hora de fin");
        }
        Optional<Seccion> seccion = seccionService.buscarPorId(d.getSeccionId());
        if (seccion.isEmpty()) return error(HttpStatus.BAD_REQUEST, "La sección elegida no existe");
        Optional<Curso> curso = cursoService.buscarPorId(d.getCursoId());
        if (curso.isEmpty()) return error(HttpStatus.BAD_REQUEST, "El curso elegido no existe");
        Optional<Docente> docente = docenteService.buscarPorId(d.getDocenteId());
        if (docente.isEmpty()) return error(HttpStatus.BAD_REQUEST, "El docente elegido no existe");
        if (!curso.get().getGradoId().equals(seccion.get().getGradoId())) {
            return error(HttpStatus.BAD_REQUEST, "El curso no pertenece al grado de la sección elegida");
        }

        horario.setAnioEscolar(d.getAnioEscolar());
        horario.setSeccion(seccion.get());
        horario.setCurso(curso.get());
        horario.setDocente(docente.get());
        horario.setDiaSemana(d.getDiaSemana());
        horario.setHoraInicio(d.getHoraInicio());
        horario.setHoraFin(d.getHoraFin());

        String cruce = horarioService.buscarCruce(horario, idActual);
        if (cruce != null) return error(HttpStatus.CONFLICT, cruce);
        return horario;
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody HorarioRequest datos) {
        Object resultado = armar(datos, new Horario(), null);
        if (resultado instanceof ResponseEntity<?> error) return error;
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.guardar((Horario) resultado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody HorarioRequest datos) {
        Optional<Horario> existente = horarioService.buscarPorId(id);
        if (existente.isEmpty()) return error(HttpStatus.NOT_FOUND, "El bloque de horario no existe");
        Object resultado = armar(datos, existente.get(), id);
        if (resultado instanceof ResponseEntity<?> error) return error;
        return ResponseEntity.ok(horarioService.guardar((Horario) resultado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (horarioService.buscarPorId(id).isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "El bloque de horario no existe");
        }
        horarioService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Bloque de horario eliminado"));
    }
}

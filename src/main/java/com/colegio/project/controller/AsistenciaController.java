package com.colegio.project.controller;

import com.colegio.project.model.Matricula;
import com.colegio.project.model.Seccion;
import com.colegio.project.service.AsistenciaService;
import com.colegio.project.service.AsistenciaService.Registro;
import com.colegio.project.service.SeccionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// HU-16: Tomar la asistencia diaria de mi sección
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private SeccionService seccionService;

    // Datos que llegan desde la pantalla
    public static class RegistroRequest {
        @NotNull(message = "Falta el alumno en uno de los registros")
        private Long matriculaId;

        @NotNull(message = "Falta el estado de asistencia de un alumno")
        @Pattern(regexp = "PRESENTE|TARDANZA|FALTA|JUSTIFICADA",
                 message = "El estado debe ser PRESENTE, TARDANZA, FALTA o JUSTIFICADA")
        private String estado;

        @Size(max = 200, message = "La observación no puede pasar de 200 caracteres")
        private String observacion;

        public Long getMatriculaId() { return matriculaId; }
        public void setMatriculaId(Long matriculaId) { this.matriculaId = matriculaId; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getObservacion() { return observacion; }
        public void setObservacion(String observacion) { this.observacion = observacion; }
    }

    public static class AsistenciaRequest {
        @NotNull(message = "Debe elegir una sección")
        private Long seccionId;

        @NotNull(message = "La fecha es obligatoria")
        private LocalDate fecha;

        @NotEmpty(message = "No hay alumnos para registrar asistencia")
        @Valid
        private List<RegistroRequest> registros;

        public Long getSeccionId() { return seccionId; }
        public void setSeccionId(Long seccionId) { this.seccionId = seccionId; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public List<RegistroRequest> getRegistros() { return registros; }
        public void setRegistros(List<RegistroRequest> registros) { this.registros = registros; }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }

    // Devuelve el motivo si la fecha no sirve para tomar asistencia, o null si es válida
    private String validarFecha(LocalDate fecha) {
        if (fecha.isAfter(LocalDate.now())) {
            return "No se puede tomar asistencia de una fecha futura";
        }
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return "Solo se toma asistencia de lunes a viernes";
        }
        return null;
    }

    // Lista de alumnos de la sección para esa fecha (con su estado si ya se tomó)
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam Long seccionId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (seccionService.buscarPorId(seccionId).isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "La sección elegida no existe");
        }
        String motivo = validarFecha(fecha);
        if (motivo != null) return error(HttpStatus.BAD_REQUEST, motivo);
        return ResponseEntity.ok(asistenciaService.listar(seccionId, fecha));
    }

    // Guarda (o corrige) la asistencia del día de toda la sección
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody AsistenciaRequest datos) {
        Optional<Seccion> seccion = seccionService.buscarPorId(datos.getSeccionId());
        if (seccion.isEmpty()) return error(HttpStatus.BAD_REQUEST, "La sección elegida no existe");

        String motivo = validarFecha(datos.getFecha());
        if (motivo != null) return error(HttpStatus.BAD_REQUEST, motivo);

        Map<Long, Matricula> permitidas = asistenciaService.matriculasActivas(datos.getSeccionId(), datos.getFecha())
                .stream().collect(Collectors.toMap(Matricula::getId, m -> m, (a, b) -> a, LinkedHashMap::new));

        Set<Long> vistos = new HashSet<>();
        for (RegistroRequest r : datos.getRegistros()) {
            if (!permitidas.containsKey(r.getMatriculaId())) {
                return error(HttpStatus.BAD_REQUEST, "Hay un alumno que no tiene matrícula activa en esta sección en "
                        + datos.getFecha().getYear());
            }
            if (!vistos.add(r.getMatriculaId())) {
                return error(HttpStatus.BAD_REQUEST, "Un alumno aparece repetido en la lista");
            }
        }

        List<Registro> registros = datos.getRegistros().stream()
                .map(r -> new Registro(r.getMatriculaId(), r.getEstado(),
                        r.getObservacion() == null || r.getObservacion().isBlank() ? null : r.getObservacion().trim()))
                .toList();

        int total = asistenciaService.guardar(seccion.get(), datos.getFecha(), registros, permitidas);
        return ResponseEntity.ok(Map.of("mensaje", "Asistencia guardada (" + total + " alumnos)"));
    }
}

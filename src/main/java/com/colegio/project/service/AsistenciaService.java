package com.colegio.project.service;

import com.colegio.project.model.Asistencia;
import com.colegio.project.model.Matricula;
import com.colegio.project.model.Seccion;
import com.colegio.project.repository.AsistenciaRepository;
import com.colegio.project.repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    // Una fila de la lista de asistencia: el alumno y el estado que tiene ese día (null si aún no se tomó)
    public record Fila(Long matriculaId, Long alumnoId, String alumnoDni, String alumnoNombre,
                       String estado, String observacion, boolean registrada) {}

    // Lo que se guarda por alumno
    public record Registro(Long matriculaId, String estado, String observacion) {}

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    // Alumnos con matrícula ACTIVA en la sección durante el año de la fecha, en orden alfabético
    public List<Matricula> matriculasActivas(Long seccionId, LocalDate fecha) {
        return matriculaRepository.findBySeccion_IdAndAnioEscolarAndEstado(
                seccionId, fecha.getYear(), Matricula.ACTIVA, Sort.by("alumno.apellido", "alumno.nombres"));
    }

    // Lista de la sección para esa fecha, con el estado ya guardado si la asistencia se tomó antes
    public List<Fila> listar(Long seccionId, LocalDate fecha) {
        List<Matricula> matriculas = matriculasActivas(seccionId, fecha);
        List<Long> ids = matriculas.stream().map(Matricula::getId).toList();
        Map<Long, Asistencia> guardadas = ids.isEmpty()
                ? Map.of()
                : asistenciaRepository.findByFechaAndMatricula_IdIn(fecha, ids).stream()
                        .collect(Collectors.toMap(Asistencia::getMatriculaId, Function.identity()));

        return matriculas.stream().map(m -> {
            Asistencia a = guardadas.get(m.getId());
            return new Fila(m.getId(), m.getAlumnoId(), m.getAlumnoDni(), m.getAlumnoNombre(),
                    a != null ? a.getEstado() : null,
                    a != null ? a.getObservacion() : null,
                    a != null);
        }).toList();
    }

    // Guarda la asistencia del día. Si ya estaba tomada, la corrige (no se duplica).
    // Ya se validó que cada matrícula esté activa y sea de la sección.
    @Transactional
    public int guardar(Seccion seccion, LocalDate fecha, List<Registro> registros, Map<Long, Matricula> matriculas) {
        for (Registro r : registros) {
            Asistencia a = asistenciaRepository.findByMatricula_IdAndFecha(r.matriculaId(), fecha)
                    .orElseGet(Asistencia::new);
            a.setMatricula(matriculas.get(r.matriculaId()));
            a.setSeccion(seccion);
            a.setFecha(fecha);
            a.setEstado(r.estado());
            a.setObservacion(r.observacion());
            asistenciaRepository.save(a);
        }
        return registros.size();
    }

    public boolean seccionTieneAsistencias(Long seccionId) {
        return asistenciaRepository.existsBySeccion_Id(seccionId);
    }
}

package com.colegio.project.service;

import com.colegio.project.model.Horario;
import com.colegio.project.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    // Valor que no coincide con ningún id: se usa al crear, cuando no hay bloque que excluir
    private static final Long NINGUNO = -1L;

    @Autowired
    private HorarioRepository horarioRepository;

    // Lista (con filtros opcionales) ordenada por año, día de la semana y hora
    public List<Horario> listar(Integer anio, Long seccionId, Long docenteId) {
        return horarioRepository.findAll().stream()
                .filter(h -> anio == null || h.getAnioEscolar().equals(anio))
                .filter(h -> seccionId == null || h.getSeccionId().equals(seccionId))
                .filter(h -> docenteId == null || h.getDocenteId().equals(docenteId))
                .sorted(Comparator
                        .comparing(Horario::getAnioEscolar).reversed()
                        .thenComparing(h -> Horario.DIAS.indexOf(h.getDiaSemana()))
                        .thenComparing(Horario::getHoraInicio))
                .toList();
    }

    public Optional<Horario> buscarPorId(Long id) {
        return horarioRepository.findById(id);
    }

    public void eliminar(Long id) {
        horarioRepository.deleteById(id);
    }

    public Horario guardar(Horario horario) {
        return horarioRepository.save(horario);
    }

    private String franja(Horario h) {
        return h.getDiaSemana().toLowerCase() + " " + h.getHoraInicio() + "-" + h.getHoraFin();
    }

    // Devuelve el motivo del cruce, o null si el bloque cabe sin cruces.
    // idActual = bloque que se está editando (para no compararlo consigo mismo), o null si es nuevo.
    public String buscarCruce(Horario h, Long idActual) {
        Long excluir = idActual != null ? idActual : NINGUNO;

        List<Horario> docente = horarioRepository.crucesDocente(h.getAnioEscolar(), h.getDiaSemana(),
                h.getDocente().getId(), h.getHoraInicio(), h.getHoraFin(), excluir);
        if (!docente.isEmpty()) {
            Horario otro = docente.get(0);
            return "Cruce de docente: " + otro.getDocenteNombre() + " ya dicta " + otro.getCursoNombre()
                    + " en " + otro.getSeccionNombre() + " el " + franja(otro);
        }

        List<Horario> seccion = horarioRepository.crucesSeccion(h.getAnioEscolar(), h.getDiaSemana(),
                h.getSeccion().getId(), h.getHoraInicio(), h.getHoraFin(), excluir);
        if (!seccion.isEmpty()) {
            Horario otro = seccion.get(0);
            return "Cruce de sección: " + otro.getSeccionNombre() + " ya tiene " + otro.getCursoNombre()
                    + " el " + franja(otro);
        }
        return null;
    }

    public boolean seccionTieneHorarios(Long id) { return horarioRepository.existsBySeccion_Id(id); }
    public boolean cursoTieneHorarios(Long id) { return horarioRepository.existsByCurso_Id(id); }
    public boolean docenteTieneHorarios(Long id) { return horarioRepository.existsByDocente_Id(id); }
}

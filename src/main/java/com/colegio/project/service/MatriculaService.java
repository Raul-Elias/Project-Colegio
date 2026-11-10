package com.colegio.project.service;

import com.colegio.project.model.Alumno;
import com.colegio.project.model.Matricula;
import com.colegio.project.model.MatriculaHistorial;
import com.colegio.project.model.Seccion;
import com.colegio.project.repository.MatriculaHistorialRepository;
import com.colegio.project.repository.MatriculaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MatriculaService {

    public static final String RETIRADA = "RETIRADA";

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private MatriculaHistorialRepository historialRepository;

    public List<Matricula> listar(Integer anioEscolar) {
        if (anioEscolar != null) {
            return matriculaRepository.findByAnioEscolar(anioEscolar);
        }
        return matriculaRepository.findAll(Sort.by(Sort.Direction.DESC, "anioEscolar", "id"));
    }

    public Optional<Matricula> buscarPorId(Long id) {
        return matriculaRepository.findById(id);
    }

    // Un alumno solo puede tener una matrícula activa por año escolar
    public boolean yaMatriculado(Long alumnoId, Integer anioEscolar) {
        return matriculaRepository.existsByAlumno_IdAndAnioEscolarAndEstado(
                alumnoId, anioEscolar, Matricula.ACTIVA);
    }

    private String etiqueta(Seccion s) {
        return s.getGradoNombre() + " - Sección " + s.getNombre();
    }

    @Transactional
    public Matricula matricular(Alumno alumno, Seccion seccion, Integer anioEscolar) {
        Matricula m = new Matricula();
        m.setAlumno(alumno);
        m.setSeccion(seccion);
        m.setAnioEscolar(anioEscolar);
        m = matriculaRepository.save(m);
        historialRepository.save(new MatriculaHistorial(
                m, MatriculaHistorial.MATRICULA, null, etiqueta(seccion), null));
        return m;
    }

    @Transactional
    public Matricula retirar(Matricula m, String motivo) {
        m.setEstado(RETIRADA);
        m = matriculaRepository.save(m);
        historialRepository.save(new MatriculaHistorial(
                m, MatriculaHistorial.RETIRO, etiqueta(m.getSeccion()), null, motivo));
        return m;
    }

    @Transactional
    public Matricula trasladar(Matricula m, Seccion destino, String motivo) {
        String origen = etiqueta(m.getSeccion());
        m.setSeccion(destino);
        m = matriculaRepository.save(m);
        historialRepository.save(new MatriculaHistorial(
                m, MatriculaHistorial.TRASLADO, origen, etiqueta(destino), motivo));
        return m;
    }

    @Transactional
    public Matricula reactivar(Matricula m, String motivo) {
        m.setEstado(Matricula.ACTIVA);
        m = matriculaRepository.save(m);
        historialRepository.save(new MatriculaHistorial(
                m, MatriculaHistorial.REACTIVACION, null, etiqueta(m.getSeccion()), motivo));
        return m;
    }

    public List<MatriculaHistorial> historial(Long matriculaId) {
        return historialRepository.findByMatricula_IdOrderByFechaAscIdAsc(matriculaId);
    }

    public boolean alumnoTieneMatriculas(Long alumnoId) {
        return matriculaRepository.existsByAlumno_Id(alumnoId);
    }

    public boolean seccionTieneMatriculas(Long seccionId) {
        return matriculaRepository.existsBySeccion_Id(seccionId);
    }
}

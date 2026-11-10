package com.colegio.project.service;

import com.colegio.project.model.Curso;
import com.colegio.project.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    public List<Curso> listarTodos() {
        return cursoRepository.findAll(Sort.by("grado.nombre", "nombre"));
    }

    public Curso guardar(Curso curso) {
        return cursoRepository.save(curso);
    }

    public Optional<Curso> buscarPorId(Long id) {
        return cursoRepository.findById(id);
    }

    public void eliminar(Long id) {
        cursoRepository.deleteById(id);
    }

    public boolean nombreEnUso(String nombre, Long gradoId, Long idActual) {
        return cursoRepository.findByNombreAndGrado_Id(nombre, gradoId)
                .map(c -> !c.getId().equals(idActual))
                .orElse(false);
    }
}

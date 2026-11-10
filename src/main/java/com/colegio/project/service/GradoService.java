package com.colegio.project.service;

import com.colegio.project.model.Grado;
import com.colegio.project.repository.CursoRepository;
import com.colegio.project.repository.GradoRepository;
import com.colegio.project.repository.SeccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradoService {

    @Autowired
    private GradoRepository gradoRepository;

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    public List<Grado> listarTodos() {
        return gradoRepository.findAll(Sort.by("id"));
    }

    public Grado guardar(Grado grado) {
        return gradoRepository.save(grado);
    }

    public Optional<Grado> buscarPorId(Long id) {
        return gradoRepository.findById(id);
    }

    public void eliminar(Long id) {
        gradoRepository.deleteById(id);
    }

    // El nombre solo está en uso si ya existe en el mismo nivel
    public boolean nombreEnUso(String nombre, String nivel, Long idActual) {
        return gradoRepository.findByNombreAndNivel(nombre.trim(), nivel)
                .map(g -> !g.getId().equals(idActual))
                .orElse(false);
    }

    // Un grado con secciones o cursos no se puede eliminar
    public boolean tieneDependencias(Long id) {
        return seccionRepository.existsByGrado_Id(id) || cursoRepository.existsByGrado_Id(id);
    }
}

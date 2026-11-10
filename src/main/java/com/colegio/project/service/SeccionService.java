package com.colegio.project.service;

import com.colegio.project.model.Seccion;
import com.colegio.project.repository.SeccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeccionService {

    @Autowired
    private SeccionRepository seccionRepository;

    public List<Seccion> listarTodos() {
        return seccionRepository.findAll(Sort.by("grado.nombre", "nombre"));
    }

    public Seccion guardar(Seccion seccion) {
        return seccionRepository.save(seccion);
    }

    public Optional<Seccion> buscarPorId(Long id) {
        return seccionRepository.findById(id);
    }

    public void eliminar(Long id) {
        seccionRepository.deleteById(id);
    }

    public boolean nombreEnUso(String nombre, Long gradoId, Long idActual) {
        return seccionRepository.findByNombreAndGrado_Id(nombre, gradoId)
                .map(s -> !s.getId().equals(idActual))
                .orElse(false);
    }
}

package com.colegio.project.service;

import com.colegio.project.model.Alumno;
import com.colegio.project.repository.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    public List<Alumno> listarTodos() {
        return alumnoRepository.findAll();
    }

    public Alumno guardar(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    public Optional<Alumno> buscarPorId(Long id) {
        return alumnoRepository.findById(id);
    }
}
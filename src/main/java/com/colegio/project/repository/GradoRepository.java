package com.colegio.project.repository;

import com.colegio.project.model.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradoRepository extends JpaRepository<Grado, Long> {
    Optional<Grado> findByNombreAndNivel(String nombre, String nivel);
}

package com.colegio.project.repository;

import com.colegio.project.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, Long> {
    Optional<Seccion> findByNombreAndGrado_Id(String nombre, Long gradoId);
    boolean existsByGrado_Id(Long gradoId);
}

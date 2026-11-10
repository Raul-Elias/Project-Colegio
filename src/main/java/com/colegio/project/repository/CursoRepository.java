package com.colegio.project.repository;

import com.colegio.project.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Optional<Curso> findByNombreAndGrado_Id(String nombre, Long gradoId);

    boolean existsByGrado_Id(Long gradoId);
}


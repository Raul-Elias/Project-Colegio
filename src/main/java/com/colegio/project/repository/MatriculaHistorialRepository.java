package com.colegio.project.repository;

import com.colegio.project.model.MatriculaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaHistorialRepository extends JpaRepository<MatriculaHistorial, Long> {
    List<MatriculaHistorial> findByMatricula_IdOrderByFechaAscIdAsc(Long matriculaId);
}

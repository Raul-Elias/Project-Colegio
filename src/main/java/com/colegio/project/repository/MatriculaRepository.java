package com.colegio.project.repository;

import com.colegio.project.model.Matricula;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    boolean existsByAlumno_IdAndAnioEscolarAndEstado(Long alumnoId, Integer anioEscolar, String estado);
    boolean existsByAlumno_Id(Long alumnoId);
    boolean existsBySeccion_Id(Long seccionId);
    List<Matricula> findByAnioEscolar(Integer anioEscolar);
    List<Matricula> findBySeccion_IdAndAnioEscolarAndEstado(Long seccionId, Integer anioEscolar, String estado, Sort sort);
}

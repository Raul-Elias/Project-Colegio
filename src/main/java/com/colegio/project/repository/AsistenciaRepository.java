package com.colegio.project.repository;

import com.colegio.project.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByFechaAndMatricula_IdIn(LocalDate fecha, Collection<Long> matriculaIds);
    Optional<Asistencia> findByMatricula_IdAndFecha(Long matriculaId, LocalDate fecha);
    boolean existsBySeccion_Id(Long seccionId);
}

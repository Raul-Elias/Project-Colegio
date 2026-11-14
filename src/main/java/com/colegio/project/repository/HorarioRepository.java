package com.colegio.project.repository;

import com.colegio.project.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    // Dos bloques se cruzan si uno empieza antes de que termine el otro y termina después de que empiece.
    // Si un bloque termina justo cuando otro empieza (ej. 08:00-09:00 y 09:00-10:00) NO hay cruce.
    @Query("select h from Horario h where h.anioEscolar = :anio and h.diaSemana = :dia "
         + "and h.docente.id = :docenteId and h.horaInicio < :fin and h.horaFin > :inicio and h.id <> :excluir")
    List<Horario> crucesDocente(@Param("anio") Integer anio, @Param("dia") String dia,
                                @Param("docenteId") Long docenteId, @Param("inicio") LocalTime inicio,
                                @Param("fin") LocalTime fin, @Param("excluir") Long excluir);

    @Query("select h from Horario h where h.anioEscolar = :anio and h.diaSemana = :dia "
         + "and h.seccion.id = :seccionId and h.horaInicio < :fin and h.horaFin > :inicio and h.id <> :excluir")
    List<Horario> crucesSeccion(@Param("anio") Integer anio, @Param("dia") String dia,
                                @Param("seccionId") Long seccionId, @Param("inicio") LocalTime inicio,
                                @Param("fin") LocalTime fin, @Param("excluir") Long excluir);

    boolean existsBySeccion_Id(Long seccionId);
    boolean existsByCurso_Id(Long cursoId);
    boolean existsByDocente_Id(Long docenteId);
}

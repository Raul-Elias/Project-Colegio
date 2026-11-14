package com.colegio.project.controller;

import com.colegio.project.model.Encuesta;
import com.colegio.project.repository.EncuestaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/** Recibe las encuestas de padres/alumnos (formulario de Apps Script o web). */
@RestController
@RequestMapping("/api/encuestas")
public class EncuestaController {
    @Autowired private EncuestaRepository repo;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Encuesta e) {
        e.setId(null);
        e.setFecha(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(e));
    }
}

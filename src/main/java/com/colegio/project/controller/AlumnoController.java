package com.colegio.project.controller;

import com.colegio.project.model.Alumno;
import com.colegio.project.service.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    @Autowired
    private AlumnoService alumnoService;

    @GetMapping
    public ResponseEntity<List<Alumno>> listar() {
        return ResponseEntity.ok(alumnoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Alumno> crear(@RequestBody Alumno alumno) {
        Alumno nuevo = alumnoService.guardar(alumno);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}
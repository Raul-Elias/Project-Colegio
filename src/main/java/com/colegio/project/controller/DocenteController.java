package com.colegio.project.controller;

import com.colegio.project.model.Docente;
import com.colegio.project.service.DocenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    @Autowired
    private DocenteService docenteService;

    @GetMapping
    public List<Docente> listar() {
        return docenteService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<Docente> registrar(@RequestBody Docente docente) {
        Docente guardado = docenteService.guardar(docente);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}
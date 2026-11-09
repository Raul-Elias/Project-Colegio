package com.colegio.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

// Convierte los errores de validación (@NotBlank, @Pattern, ...) en un mensaje claro para la pantalla
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(Map.of("mensaje", mensaje));
    }
}
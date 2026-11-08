package com.colegio.project.controller;

import com.colegio.project.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String correo, String password) {}

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return authService.autenticar(req.correo(), req.password())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "rol", u.getRol(),
                        "primerIngreso", u.getPrimerIngreso(),
                        "terminosAceptados", u.getTerminosAceptados())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "Correo o contraseña incorrectos")));
    }
}

package com.colegio.project.controller;

import com.colegio.project.model.Usuario;
import com.colegio.project.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String correo, String password) {}

    public record CambioPasswordRequest(String correo, String passwordActual, String passwordNueva) {}

    public record AceptarTerminosRequest(String correo) {}

    public record CodigoRequest(String correo, String password, String codigo) {}

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return authService.autenticar(req.correo(), req.password())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "rol", u.getRol(),
                        "primerIngreso", u.getPrimerIngreso(),
                        "terminosAceptados", u.getTerminosAceptados(),
                        "doblePasoActivo", authService.doblePasoActivo(u))))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "Correo o contraseña incorrectos")));
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody CambioPasswordRequest req) {
        String error = authService.cambiarPassword(req.correo(), req.passwordActual(), req.passwordNueva());
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada"));
    }

    @PostMapping("/aceptar-terminos")
    public ResponseEntity<?> aceptarTerminos(@RequestBody AceptarTerminosRequest req) {
        if (!authService.aceptarTerminos(req.correo())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Usuario no encontrado"));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Términos aceptados"));
    }

    // ----- HU-04: verificación en dos pasos para la cuenta de administración -----

    // Paso 1: genera la clave que se agrega a la aplicación de autenticación
    @PostMapping("/2fa/configurar")
    public ResponseEntity<?> configurarDoblePaso(@RequestBody LoginRequest req) {
        Optional<Usuario> opt = authService.autenticar(req.correo(), req.password());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Contraseña incorrecta"));
        }
        Usuario u = opt.get();
        if (!"ADMIN".equals(u.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Solo la cuenta de administración usa verificación en dos pasos"));
        }
        if (authService.doblePasoActivo(u)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La verificación en dos pasos ya está activada"));
        }
        String secreto = authService.generarSecreto(u);
        String etiqueta = URLEncoder.encode("Colegio:" + u.getCorreo(), StandardCharsets.UTF_8);
        String uri = "otpauth://totp/" + etiqueta + "?secret=" + secreto + "&issuer=Colegio";
        return ResponseEntity.ok(Map.of("secreto", secreto, "uri", uri));
    }

    // Paso 2: activa el doble paso al comprobar el primer código
    @PostMapping("/2fa/activar")
    public ResponseEntity<?> activarDoblePaso(@RequestBody CodigoRequest req) {
        Optional<Usuario> opt = authService.autenticar(req.correo(), req.password());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Contraseña incorrecta"));
        }
        if (!"ADMIN".equals(opt.get().getRol()) || !authService.activarDoblePaso(opt.get(), req.codigo())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El código es incorrecto o venció"));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Verificación en dos pasos activada"));
    }

    // En cada ingreso del administrador: valida contraseña y código
    @PostMapping("/2fa/verificar")
    public ResponseEntity<?> verificarDoblePaso(@RequestBody CodigoRequest req) {
        return authService.autenticarConCodigo(req.correo(), req.password(), req.codigo())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "rol", u.getRol(),
                        "primerIngreso", u.getPrimerIngreso(),
                        "terminosAceptados", u.getTerminosAceptados(),
                        "doblePasoActivo", authService.doblePasoActivo(u))))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("mensaje", "Código incorrecto o vencido")));
    }
}
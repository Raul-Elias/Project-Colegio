package com.colegio.project.controller;

import com.colegio.project.service.AgenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/agentes")
@CrossOrigin("*")
public class AgenteController {
    @Value("${dashboard.token:director2026}") private String token;
    @Autowired private AgenteService agentes;

    record MotivoReq(String dni, String texto) {}
    record TutorReq(String dni, String mensaje) {}

    private void validar(String t) {
        if (!token.equals(t)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    @GetMapping("/retencion")
    public Map<String, Object> retencion(@RequestParam(required = false) String t) { validar(t); return agentes.retencion(); }

    @PostMapping("/retencion/motivo")
    public ResponseEntity<?> motivo(@RequestBody MotivoReq r) {
        return agentes.registrarMotivo(r.dni(), r.texto()).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Alumno no encontrado")));
    }

    @GetMapping("/voz")
    public Map<String, Object> voz(@RequestParam(required = false) String t) { validar(t); return agentes.voz(); }

    @PostMapping("/tutor")
    public Map<String, String> tutor(@RequestBody TutorReq r) { return Map.of("respuesta", agentes.tutor(r.dni(), r.mensaje())); }

    @GetMapping("/tutor/perfil")
    public ResponseEntity<?> perfil(@RequestParam String dni) {
        return agentes.alumno(dni).<ResponseEntity<?>>map(a -> ResponseEntity.ok(Map.of("nombre", a.getNombres(), "grado", a.getGrado(),
                "promedio", a.getPromedio(), "asistencia", a.getAsistencia()))).orElse(ResponseEntity.notFound().build());
    }
}

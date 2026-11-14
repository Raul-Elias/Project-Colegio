package com.colegio.project.controller;

import com.colegio.project.service.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** KPIs del director: consulta puntual y flujo en vivo (SSE). Protegido con token. */
@RestController
@RequestMapping("/api/kpis")
@CrossOrigin("*")
public class KpiController {
    @Value("${dashboard.token:director2026}") private String token;
    @Autowired private KpiService kpis;
    private final List<SseEmitter> emisores = new CopyOnWriteArrayList<>();
    private String ultimo = "";

    private void validar(String t) {
        if (!token.equals(t)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
    }

    @GetMapping
    public Map<String, Object> get(@RequestParam(required = false) String t) {
        validar(t);
        Map<String, Object> m = kpis.calcular();
        m.put("actualizado", LocalDateTime.now().toString());
        return m;
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam(required = false) String t) {
        validar(t);
        SseEmitter em = new SseEmitter(0L);
        emisores.add(em);
        em.onCompletion(() -> emisores.remove(em));
        em.onTimeout(() -> emisores.remove(em));
        em.onError(x -> emisores.remove(em));
        try {
            Map<String, Object> m = kpis.calcular();
            m.put("actualizado", LocalDateTime.now().toString());
            em.send(SseEmitter.event().name("kpis").data(m, MediaType.APPLICATION_JSON));
        } catch (Exception ex) { emisores.remove(em); }
        return em;
    }

    @Scheduled(fixedDelay = 4000)
    public void difundir() {
        if (emisores.isEmpty()) return;
        Map<String, Object> m = kpis.calcular();
        String firma = m.toString();
        boolean cambio = !firma.equals(ultimo);
        ultimo = firma;
        m.put("actualizado", LocalDateTime.now().toString());
        for (SseEmitter em : emisores) {
            try {
                if (cambio) em.send(SseEmitter.event().name("kpis").data(m, MediaType.APPLICATION_JSON));
                else em.send(SseEmitter.event().comment("hb"));
            } catch (Exception ex) { emisores.remove(em); }
        }
    }
}

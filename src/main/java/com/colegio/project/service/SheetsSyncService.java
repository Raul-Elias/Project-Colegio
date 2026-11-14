package com.colegio.project.service;

import com.colegio.project.model.Alumno;
import com.colegio.project.repository.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Envía los alumnos matriculados a Google Sheets (Apps Script) cada vez que cambian. */
@Service
public class SheetsSyncService {
    public static final List<Object> CABECERA = List.of("DNI", "Nombres", "Apellido", "Grado", "Promedio",
            "Asistencia %", "Continuidad", "Motivo", "Fecha matrícula", "Riesgo");

    @Value("${sheets.webapp.url:}") private String url;
    @Value("${sheets.token:}") private String token;
    @Autowired private AlumnoRepository repo;

    public static List<Object> fila(Alumno a) {
        return Arrays.asList(a.getDni(), a.getNombres(), a.getApellido(), a.getGrado(), a.getPromedio(),
                a.getAsistencia(), a.getContinuidad(), a.getMotivoNoContinua(),
                a.getFechaMatricula() == null ? "" : a.getFechaMatricula().toString(), KpiService.riesgo(a));
    }

    public void sync() {
        if (url == null || url.isBlank()) return;
        CompletableFuture.runAsync(() -> {
            try {
                List<List<Object>> rows = new ArrayList<>();
                rows.add(CABECERA);
                repo.findAll().forEach(a -> rows.add(fila(a)));
                RestClient.create().post().uri(url).contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("token", token, "rows", rows)).retrieve().toBodilessEntity();
            } catch (Exception e) {
                System.err.println("Sync Sheets falló: " + e.getMessage());
            }
        });
    }
}

package com.colegio.project.service;

import com.colegio.project.model.Alumno;
import com.colegio.project.model.Encuesta;
import com.colegio.project.repository.AlumnoRepository;
import com.colegio.project.repository.EncuestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KpiService {
    @Autowired private AlumnoRepository alumnos;
    @Autowired private EncuestaRepository encuestas;
    @Value("${colegio.capacidad:700}") private int capacidad;

    /** Riesgo de no continuar (0-100) con reglas simples y explicables. */
    public static int riesgo(Alumno a) {
        double p = a.getPromedio() == null ? 14 : a.getPromedio();
        double as = a.getAsistencia() == null ? 95 : a.getAsistencia();
        if ("NO_CONTINUA".equals(a.getContinuidad())) return 100;
        int r = 0;
        if (p < 11) r += 40; else if (p < 13) r += 20;
        if (as < 85) r += 35; else if (as < 92) r += 15;
        if ("INDECISO".equals(a.getContinuidad())) r += 15;
        return Math.min(r, 100);
    }

    static double r1(double x) { return Math.round(x * 10) / 10.0; }

    public Map<String, Object> calcular() {
        List<Alumno> l = alumnos.findAll();
        List<Encuesta> e = encuestas.findAll();
        int n = l.size();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", n);
        m.put("capacidad", capacidad);
        m.put("vacantes", Math.max(capacidad - n, 0));
        m.put("asistencia", r1(l.stream().mapToDouble(a -> a.getAsistencia() == null ? 95 : a.getAsistencia()).average().orElse(0)));
        m.put("promedio", r1(l.stream().mapToDouble(a -> a.getPromedio() == null ? 0 : a.getPromedio()).average().orElse(0)));

        long si = l.stream().filter(a -> "CONTINUA".equals(a.getContinuidad())).count();
        long no = l.stream().filter(a -> "NO_CONTINUA".equals(a.getContinuidad())).count();
        m.put("continuan", si);
        m.put("noContinuan", no);
        m.put("indecisos", n - si - no);
        m.put("tasaContinuidad", n == 0 ? 0 : r1(si * 100.0 / n));
        m.put("motivos", l.stream().filter(a -> "NO_CONTINUA".equals(a.getContinuidad()))
                .collect(Collectors.groupingBy(a -> a.getMotivoNoContinua() == null ? "OTRO" : a.getMotivoNoContinua(), TreeMap::new, Collectors.counting())));

        List<Map<String, Object>> grados = new ArrayList<>();
        l.stream().collect(Collectors.groupingBy(a -> a.getGrado() == null ? "-" : a.getGrado(), TreeMap::new, Collectors.toList()))
                .forEach((g, xs) -> {
                    Map<String, Object> o = new LinkedHashMap<>();
                    o.put("grado", g);
                    o.put("n", xs.size());
                    o.put("prom", r1(xs.stream().mapToDouble(a -> a.getPromedio() == null ? 0 : a.getPromedio()).average().orElse(0)));
                    grados.add(o);
                });
        m.put("porGrado", grados);

        m.put("csat", r1(e.stream().mapToInt(Encuesta::getPuntaje).average().orElse(0)));
        m.put("encuestas", e.size());
        List<Encuesta> conNps = e.stream().filter(x -> x.getRecomienda() != null).toList();
        long prom = conNps.stream().filter(x -> x.getRecomienda() >= 9).count();
        long det = conNps.stream().filter(x -> x.getRecomienda() <= 6).count();
        m.put("nps", conNps.isEmpty() ? 0 : Math.round((prom - det) * 100.0 / conNps.size()));
        m.put("temas", temas(e));

        List<Alumno> riesgo = l.stream().filter(a -> !"NO_CONTINUA".equals(a.getContinuidad()) && riesgo(a) >= 50)
                .sorted(Comparator.comparingInt((Alumno a) -> riesgo(a)).reversed()).toList();
        m.put("riesgoAlto", riesgo.size());
        m.put("enRiesgo", riesgo.stream().limit(5).map(a -> {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("alumno", a.getNombres() + " " + a.getApellido());
            o.put("grado", a.getGrado());
            o.put("riesgo", riesgo(a));
            return o;
        }).toList());
        return m;
    }

    public static List<Map<String, Object>> temas(List<Encuesta> e) {
        List<Map<String, Object>> out = new ArrayList<>();
        e.stream().collect(Collectors.groupingBy(x -> x.getTema() == null ? "General" : x.getTema(), TreeMap::new, Collectors.toList()))
                .forEach((t, xs) -> {
                    Map<String, Object> o = new LinkedHashMap<>();
                    o.put("tema", t);
                    o.put("prom", r1(xs.stream().mapToInt(Encuesta::getPuntaje).average().orElse(0)));
                    o.put("n", xs.size());
                    out.add(o);
                });
        return out;
    }
}

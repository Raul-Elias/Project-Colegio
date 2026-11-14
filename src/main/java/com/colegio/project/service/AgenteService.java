package com.colegio.project.service;

import com.colegio.project.model.Alumno;
import com.colegio.project.model.Encuesta;
import com.colegio.project.repository.AlumnoRepository;
import com.colegio.project.repository.EncuestaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Tres agentes: Retención, Voz de Familia y Tutor del Estudiante.
 * Usan la API de Claude si hay ANTHROPIC_API_KEY; si no, reglas locales de respaldo.
 */
@Service
public class AgenteService {
    @Value("${anthropic.api.key:}") private String apiKey;
    @Value("${anthropic.model:claude-sonnet-5-5}") private String model;
    @Autowired private AlumnoRepository alumnos;
    @Autowired private EncuestaRepository encuestas;

    private static final List<String> MOTIVOS = List.of("ECONOMICO", "MUDANZA", "ACADEMICO", "CLIMA", "HORARIO", "OTRO");

    String claude(String system, String user) {
        if (apiKey == null || apiKey.isBlank()) return null;
        try {
            Map<String, Object> body = Map.of("model", model, "max_tokens", 500, "system", system,
                    "messages", List.of(Map.of("role", "user", "content", user)));
            Map<?, ?> r = RestClient.create().post().uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey).header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
            return (String) ((Map<?, ?>) ((List<?>) r.get("content")).get(0)).get("text");
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- 1. AGENTE DE RETENCIÓN ----------
    static List<String> senales(Alumno a) {
        List<String> s = new ArrayList<>();
        if (a.getPromedio() != null && a.getPromedio() < 13) s.add("Promedio bajo (" + a.getPromedio() + ")");
        if (a.getAsistencia() != null && a.getAsistencia() < 92) s.add("Asistencia " + a.getAsistencia() + "%");
        if ("INDECISO".equals(a.getContinuidad())) s.add("Familia indecisa");
        return s;
    }

    public Map<String, Object> retencion() {
        List<Map<String, Object>> top = alumnos.findAll().stream()
                .filter(a -> !"NO_CONTINUA".equals(a.getContinuidad()))
                .sorted(Comparator.comparingInt((Alumno a) -> KpiService.riesgo(a)).reversed()).limit(10)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("dni", a.getDni());
                    m.put("alumno", a.getNombres() + " " + a.getApellido());
                    m.put("grado", a.getGrado());
                    m.put("riesgo", KpiService.riesgo(a));
                    m.put("senales", senales(a));
                    return m;
                }).toList();
        String ia = claude("Eres el agente de retención de un colegio secundario peruano. Con la lista de alumnos en riesgo de "
                + "no continuar el próximo año, da al director 3 acciones concretas y breves (máx. 90 palabras). Español.", top.toString());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("enRiesgo", top);
        r.put("recomendacion", ia != null ? ia : "Contactar esta semana a las familias con riesgo ≥ 60, "
                + "ofrecer reforzamiento a quienes tienen promedio < 13 y conversar con quienes faltan seguido.");
        return r;
    }

    public String clasificarMotivo(String texto) {
        String ia = claude("Clasifica el motivo por el que una familia no continuará en el colegio. Responde SOLO una palabra de: "
                + String.join(", ", MOTIVOS), texto);
        if (ia != null) {
            String u = ia.trim().toUpperCase();
            for (String m : MOTIVOS) if (u.contains(m)) return m;
        }
        String t = texto.toLowerCase();
        if (t.matches(".*(pens|pago|econ|dinero|costo|caro|trabajo).*")) return "ECONOMICO";
        if (t.matches(".*(mud|traslad|viaj|lejos|distancia|provincia).*")) return "MUDANZA";
        if (t.matches(".*(profe|docen|enseñ|acad|nota|nivel|aprend).*")) return "ACADEMICO";
        if (t.matches(".*(segur|bullying|acoso|trato|ambiente|conviv).*")) return "CLIMA";
        if (t.matches(".*(horario|turno|tarde|hora).*")) return "HORARIO";
        return "OTRO";
    }

    public Optional<Map<String, Object>> registrarMotivo(String dni, String texto) {
        return alumnos.findAll().stream().filter(a -> a.getDni().equals(dni)).findFirst().map(a -> {
            a.setContinuidad("NO_CONTINUA");
            a.setMotivoNoContinua(clasificarMotivo(texto));
            alumnos.save(a);
            return Map.<String, Object>of("dni", dni, "motivo", a.getMotivoNoContinua());
        });
    }

    // ---------- 2. AGENTE VOZ DE FAMILIA ----------
    public Map<String, Object> voz() {
        List<Encuesta> e = encuestas.findAll();
        List<Map<String, Object>> temas = KpiService.temas(e);
        Map<String, Object> peor = temas.stream().min(Comparator.comparingDouble(t -> (double) t.get("prom"))).orElse(null);
        Map<String, Object> mejor = temas.stream().max(Comparator.comparingDouble(t -> (double) t.get("prom"))).orElse(null);
        List<String> recientes = e.stream().sorted(Comparator.comparing(Encuesta::getFecha).reversed()).limit(15)
                .map(x -> x.getTema() + " (" + x.getPuntaje() + "/5): " + x.getComentario()).toList();
        String ia = claude("Eres el agente Voz de Familia de un colegio. Resume en 5 líneas máximo para el director: lo que más valoran, "
                + "lo que más preocupa y 1 acción recomendada. Español, directo.", "Temas: " + temas + "\nComentarios: " + recientes);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("temas", temas);
        r.put("resumen", ia != null ? ia : (mejor == null ? "Aún no hay encuestas."
                : "Lo mejor valorado: " + mejor.get("tema") + " (" + mejor.get("prom") + "/5). Lo que más preocupa: "
                + peor.get("tema") + " (" + peor.get("prom") + "/5). Acción sugerida: reunirse con los responsables de " + peor.get("tema") + "."));
        return r;
    }

    // ---------- 3. TUTOR DEL ESTUDIANTE (mascota) ----------
    public Optional<Alumno> alumno(String dni) {
        return alumnos.findAll().stream().filter(a -> a.getDni().equals(dni)).findFirst();
    }

    public String tutor(String dni, String mensaje) {
        Optional<Alumno> oa = alumno(dni);
        if (oa.isEmpty()) return "No encuentro tu ficha todavía. Pídele a secretaría que verifique tu DNI.";
        Alumno a = oa.get();
        String ctx = "Alumno: " + a.getNombres() + ", " + a.getGrado() + ", promedio " + a.getPromedio() + "/20, asistencia " + a.getAsistencia() + "%.";
        String ia = claude("Eres Tobi, un perro tutor amigable de un colegio secundario. Ayudas SOLO a este estudiante con sus notas, "
                + "hábitos de estudio y organización. Máx. 4 frases, cálido, en español, sin inventar datos ni hablar de otros alumnos. " + ctx, mensaje);
        if (ia != null) return ia;
        String m = mensaje.toLowerCase();
        double p = a.getPromedio() == null ? 0 : a.getPromedio();
        if (m.matches(".*(nota|promedio|voy).*"))
            return "Tu promedio es " + p + "/20 y tu asistencia " + a.getAsistencia() + "%. " + (p >= 14 ? "¡Vas muy bien, sigue así! 🐾" : "Vamos a mejorarlo paso a paso.");
        if (m.matches(".*(estudi|plan|subir|mejor).*"))
            return "Plan simple: 25 min de estudio + 5 de descanso, repasa primero el curso más difícil y haz 3 ejercicios cada día. ¡Tú puedes!";
        return "Puedo ayudarte con tus notas, un plan de estudio o cómo subir tu promedio. ¿Qué necesitas?";
    }
}

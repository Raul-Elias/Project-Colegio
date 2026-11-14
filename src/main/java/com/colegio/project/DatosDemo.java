package com.colegio.project;

import com.colegio.project.model.Alumno;
import com.colegio.project.model.Encuesta;
import com.colegio.project.repository.AlumnoRepository;
import com.colegio.project.repository.EncuestaRepository;
import com.colegio.project.repository.UsuarioRepository;
import com.colegio.project.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

/** Datos de ejemplo para ver el dashboard lleno (colegio.demo=true y base vacía). */
@Component
public class DatosDemo implements CommandLineRunner {
    @Value("${colegio.demo:false}") private boolean demo;
    @Autowired private AlumnoRepository alumnos;
    @Autowired private EncuestaRepository encuestas;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private AuthService auth;

    @Override
    public void run(String... args) {
        if (!demo || alumnos.count() > 0) return;
        Random r = new Random(7);
        String[] nom = {"Mateo", "Camila", "Lucas", "Valeria", "Diego", "Adriana", "Sofía", "Joaquín", "Alonso", "Isabella", "Santiago", "Luciana"};
        String[] ape = {"Quispe", "Ramos", "Vega", "Zavaleta", "Navarro", "Cárdenas", "Rojas", "Flores", "Huamán", "Paredes", "Torres", "Mendoza"};
        String[] motivos = {"ECONOMICO", "ECONOMICO", "MUDANZA", "ACADEMICO", "CLIMA", "HORARIO", "OTRO"};
        for (int i = 0; i < 180; i++) {
            Alumno a = new Alumno();
            a.setDni(String.valueOf(70000001 + i));
            a.setNombres(nom[r.nextInt(nom.length)]);
            a.setApellido(ape[r.nextInt(ape.length)] + " " + ape[r.nextInt(ape.length)]);
            a.setGrado((1 + i % 5) + "° Secundaria");
            a.setPromedio(Math.round(Math.max(6, Math.min(20, 14 + r.nextGaussian() * 2.5)) * 10) / 10.0);
            a.setAsistencia(Math.round(Math.min(100, 96 - Math.abs(r.nextGaussian()) * 6) * 10) / 10.0);
            double x = r.nextDouble();
            if (x < 0.72) a.setContinuidad("CONTINUA");
            else if (x < 0.88) a.setContinuidad("INDECISO");
            else { a.setContinuidad("NO_CONTINUA"); a.setMotivoNoContinua(motivos[r.nextInt(motivos.length)]); }
            a.setFechaMatricula(java.time.LocalDate.now().minusDays(r.nextInt(120)));
            alumnos.save(a);
        }
        String[] temas = {"Docentes", "Infraestructura", "Comunicación", "Pensiones", "Seguridad", "Exigencia académica"};
        String[] bien = {"Muy buena enseñanza", "Excelente trato", "Nos mantienen informados", "Se siente seguro", "Buen nivel preuniversitario"};
        String[] mal = {"Los patios son pequeños", "Tardan en responder mensajes", "La pensión subió mucho", "Falta más comunicación", "Los baños necesitan mejoras"};
        for (int i = 0; i < 160; i++) {
            Encuesta e = new Encuesta();
            String tema = temas[r.nextInt(temas.length)];
            int p = (tema.equals("Infraestructura") || tema.equals("Pensiones")) ? 2 + r.nextInt(3) : 3 + r.nextInt(3);
            e.setTema(tema);
            e.setPuntaje(p);
            e.setRol(r.nextBoolean() ? "PADRE" : "ALUMNO");
            e.setRecomienda(Math.min(10, p * 2 - r.nextInt(2)));
            e.setComentario(p >= 4 ? bien[r.nextInt(bien.length)] : mal[r.nextInt(mal.length)]);
            e.setFecha(java.time.LocalDateTime.now().minusDays(r.nextInt(30)));
            encuestas.save(e);
        }
        if (usuarios.findByCorreo("70000001@colegio.com").isEmpty()) auth.crear("70000001@colegio.com", "Alumno123", "ESTUDIANTE");
        System.out.println(">>> Datos demo cargados. Alumno demo: 70000001@colegio.com / Alumno123");
    }
}

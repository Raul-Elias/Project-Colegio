package com.colegio.project;

import com.colegio.project.repository.UsuarioRepository;
import com.colegio.project.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatosIniciales implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByCorreo("admin@colegio.com").isEmpty()) {
            authService.crear("admin@colegio.com", "Admin123", "ADMIN");
        }
    }
}

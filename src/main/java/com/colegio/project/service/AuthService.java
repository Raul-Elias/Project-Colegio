package com.colegio.project.service;

import com.colegio.project.model.Usuario;
import com.colegio.project.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Optional<Usuario> autenticar(String correo, String password) {
        return usuarioRepository.findByCorreo(correo)
                .filter(u -> encoder.matches(password, u.getPassword()));
    }

    public Usuario crear(String correo, String password, String rol) {
        Usuario u = new Usuario();
        u.setCorreo(correo);
        u.setPassword(encoder.encode(password));
        u.setRol(rol);
        return usuarioRepository.save(u);
    }
}

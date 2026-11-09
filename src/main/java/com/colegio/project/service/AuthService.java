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

    // Devuelve null si todo salió bien, o el mensaje de error
    public String cambiarPassword(String correo, String actual, String nueva) {
        Optional<Usuario> opt = autenticar(correo, actual);
        if (opt.isEmpty()) {
            return "La contraseña actual es incorrecta";
        }
        if (nueva == null || nueva.length() < 8) {
            return "La nueva contraseña debe tener al menos 8 caracteres";
        }
        if (nueva.equals(actual)) {
            return "La nueva contraseña debe ser distinta de la actual";
        }
        Usuario u = opt.get();
        u.setPassword(encoder.encode(nueva));
        u.setPrimerIngreso(false);
        usuarioRepository.save(u);
        return null;
    }

    // Devuelve true si el usuario existe y se marcó la aceptación
    public boolean aceptarTerminos(String correo) {
        Optional<Usuario> opt = usuarioRepository.findByCorreo(correo);
        if (opt.isEmpty()) {
            return false;
        }
        Usuario u = opt.get();
        u.setTerminosAceptados(true);
        usuarioRepository.save(u);
        return true;
    }
    
    // ----- Verificación en dos pasos (solo cuenta de administración) -----

    public boolean doblePasoActivo(Usuario u) {
        return "ADMIN".equals(u.getRol()) && Boolean.TRUE.equals(u.getDoblePasoActivo());
    }

    // Valida contraseña y, si el usuario tiene doble paso activo, también el código
    public Optional<Usuario> autenticarConCodigo(String correo, String password, String codigo) {
        return autenticar(correo, password)
                .filter(u -> !doblePasoActivo(u) || Totp.verificar(u.getSecreto2fa(), codigo));
    }

    // Crea una clave nueva (todavía sin activar) y la devuelve
    public String generarSecreto(Usuario u) {
        String secreto = Totp.generarSecreto();
        u.setSecreto2fa(secreto);
        u.setDoblePasoActivo(false);
        usuarioRepository.save(u);
        return secreto;
    }

    // Activa el doble paso si el código corresponde a la clave generada
    public boolean activarDoblePaso(Usuario u, String codigo) {
        if (u.getSecreto2fa() == null || !Totp.verificar(u.getSecreto2fa(), codigo)) {
            return false;
        }
        u.setDoblePasoActivo(true);
        usuarioRepository.save(u);
        return true;
    }
}
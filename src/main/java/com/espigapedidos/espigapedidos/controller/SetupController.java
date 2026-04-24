package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Usuario;
import com.espigapedidos.espigapedidos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SetupController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/setup-admin")
    public String crearAdmin() {
        if (usuarioRepository.findByUsername("admin").isPresent()) {
            return "El usuario admin ya existe";
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setRol("ADMIN");
        admin.setActivo(true);

        usuarioRepository.save(admin);

        return "Usuario admin creado correctamente";
    }
    @GetMapping("/setup-tienda")
    public String crearTienda() {

        if (usuarioRepository.findByUsername("tienda").isPresent()) {
            return "Usuario tienda ya existe";
        }

        Usuario u = new Usuario();
        u.setNombre("Usuario Tienda");
        u.setUsername("tienda");
        u.setPassword(passwordEncoder.encode("1234"));
        u.setRol("TIENDA");
        u.setActivo(true);

        usuarioRepository.save(u);

        return "Usuario tienda creado";
    }
}

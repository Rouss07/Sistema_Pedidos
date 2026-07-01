package com.espigapedidos.espigapedidos.service;

import com.espigapedidos.espigapedidos.entity.SolicitudReset;
import com.espigapedidos.espigapedidos.entity.Usuario;
import com.espigapedidos.espigapedidos.repository.SolicitudResetRepository;
import com.espigapedidos.espigapedidos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudResetService {

    private final SolicitudResetRepository solicitudRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    public SolicitudResetService(SolicitudResetRepository solicitudRepo,
                                 UsuarioRepository usuarioRepo,
                                 PasswordEncoder passwordEncoder) {
        this.solicitudRepo = solicitudRepo;
        this.usuarioRepo = usuarioRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public void crearSolicitud(String username, String mensaje) {
        SolicitudReset s = new SolicitudReset();
        s.setUsername(username);
        s.setMensaje(mensaje);
        s.setFecha(LocalDateTime.now());
        s.setAtendida(false);
        solicitudRepo.save(s);
    }

    public List<SolicitudReset> listarPendientes() {
        return solicitudRepo.findByAtendidaFalse();
    }

    public void resetearPassword(Long solicitudId, String nuevaPassword) {
        SolicitudReset s = solicitudRepo.findById(solicitudId).orElse(null);
        if (s == null) return;

        Usuario u = usuarioRepo.findByUsername(s.getUsername()).orElse(null);
        if (u != null) {
            u.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepo.save(u);
        }
        s.setAtendida(true);
        solicitudRepo.save(s);
    }
}

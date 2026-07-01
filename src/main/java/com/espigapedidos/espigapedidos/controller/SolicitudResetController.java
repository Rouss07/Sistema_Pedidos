package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.service.SolicitudResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reset")
public class SolicitudResetController {

    private final SolicitudResetService service;

    public SolicitudResetController(SolicitudResetService service) {
        this.service = service;
    }

    // Formulario público para que la tienda envíe solicitud
    @GetMapping("/solicitar")
    public String mostrarFormulario() {
        return "reset/solicitar";
    }

    @PostMapping("/solicitar")
    public String enviarSolicitud(@RequestParam String username,
                                  @RequestParam String mensaje) {
        service.crearSolicitud(username, mensaje);
        return "redirect:/reset/enviado";
    }

    @GetMapping("/enviado")
    public String enviado() {
        return "reset/enviado";
    }

    // Panel admin - ver solicitudes pendientes
    @GetMapping("/admin/solicitudes")
    public String verSolicitudes(Model model) {
        model.addAttribute("solicitudes", service.listarPendientes());
        return "reset/admin_solicitudes";
    }

    // Admin resetea contraseña
    @PostMapping("/admin/resetear")
    public String resetear(@RequestParam Long solicitudId,
                           @RequestParam String nuevaPassword) {
        service.resetearPassword(solicitudId, nuevaPassword);
        return "redirect:/reset/admin/solicitudes";
    }
}

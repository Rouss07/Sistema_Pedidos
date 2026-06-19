package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.PedidoEspecial;
import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.service.PedidoEspecialService;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/pedidos-especiales")
public class PedidoEspecialController {

    private final PedidoEspecialService pedidoEspecialService;
    private final TiendaService tiendaService;

    public PedidoEspecialController(PedidoEspecialService pedidoEspecialService, TiendaService tiendaService) {
        this.pedidoEspecialService = pedidoEspecialService;
        this.tiendaService = tiendaService;
    }

    @GetMapping
    public String listarPedidosEspeciales(Model model) {
        model.addAttribute("pedidosEspeciales", pedidoEspecialService.listarPedidosEspeciales());
        return "pedidosespeciales/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("pedidoEspecial", new PedidoEspecial());
        model.addAttribute("tiendas", tiendaService.listarTiendas());
        return "pedidosespeciales/formulario";
    }

    @PostMapping("/guardar")
    public String guardarPedidoEspecial(@ModelAttribute PedidoEspecial pedidoEspecial,
                                        @RequestParam("tiendaId") Long tiendaId,
                                        @RequestParam("archivoImagen") MultipartFile archivoImagen) throws IOException {

        Tienda tienda = tiendaService.obtenerTiendaPorId(tiendaId);
        pedidoEspecial.setTienda(tienda);

        if (!archivoImagen.isEmpty()) {
            String carpetaUploads = System.getProperty("user.dir") + "/uploads/";
            File directorio = new File(carpetaUploads);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            String nombreArchivo = UUID.randomUUID() + "_" + archivoImagen.getOriginalFilename();
            File destino = new File(carpetaUploads + nombreArchivo);
            archivoImagen.transferTo(destino);

            pedidoEspecial.setImagen(nombreArchivo);
        }

        pedidoEspecialService.guardarPedidoEspecial(pedidoEspecial);
        return "redirect:/pedidos-especiales";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPedidoEspecial(@PathVariable Long id) {
        pedidoEspecialService.eliminarPedidoEspecial(id);
        return "redirect:/pedidos-especiales";
    }
}

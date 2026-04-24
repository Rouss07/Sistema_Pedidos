package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.service.PedidoService;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final TiendaService tiendaService;

    public PedidoController(PedidoService pedidoService, TiendaService tiendaService) {
        this.pedidoService = pedidoService;
        this.tiendaService = tiendaService;
    }

    @GetMapping
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarPedidos());
        return "pedidos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDate.now());
        model.addAttribute("pedido", pedido);
        model.addAttribute("tiendas", tiendaService.listarTiendas());
        return "pedidos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarPedido(@ModelAttribute Pedido pedido, @RequestParam("tienda") Long tiendaId) {
        Tienda tienda = tiendaService.obtenerTiendaPorId(tiendaId);
        pedido.setTienda(tienda);
        pedidoService.guardarPedido(pedido);
        return "redirect:/pedidos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("tiendas", tiendaService.listarTiendas());
        return "pedidos/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return "redirect:/pedidos";
    }
}

package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.service.PedidoEspecialService;
import com.espigapedidos.espigapedidos.service.PedidoService;
import com.espigapedidos.espigapedidos.service.ProductoService;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductoService productoService;
    private final TiendaService tiendaService;
    private final PedidoService pedidoService;
    private final PedidoEspecialService pedidoEspecialService;

    public HomeController(ProductoService productoService,
                          TiendaService tiendaService,
                          PedidoService pedidoService,
                          PedidoEspecialService pedidoEspecialService) {
        this.productoService = productoService;
        this.tiendaService = tiendaService;
        this.pedidoService = pedidoService;
        this.pedidoEspecialService = pedidoEspecialService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("totalProductos", productoService.contarProductos());
        model.addAttribute("totalTiendas", tiendaService.contarTiendas());
        model.addAttribute("totalPedidos", pedidoService.contarPedidos());
        model.addAttribute("totalPedidosEspeciales", pedidoEspecialService.contarPedidosEspeciales());
        return "index";
    }
}
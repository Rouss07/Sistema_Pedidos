package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.DetallePedido;
import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.entity.Producto;
import com.espigapedidos.espigapedidos.entity.Tienda;
import com.espigapedidos.espigapedidos.service.DetallePedidoService;
import com.espigapedidos.espigapedidos.service.PedidoService;
import com.espigapedidos.espigapedidos.service.ProductoService;
import com.espigapedidos.espigapedidos.service.TiendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final TiendaService tiendaService;
    private final ProductoService productoService;
    private final DetallePedidoService detallePedidoService;

    public PedidoController(PedidoService pedidoService, TiendaService tiendaService,
                            ProductoService productoService, DetallePedidoService detallePedidoService) {
        this.pedidoService = pedidoService;
        this.tiendaService = tiendaService;
        this.productoService = productoService;
        this.detallePedidoService = detallePedidoService;
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
        model.addAttribute("productos", productoService.listarProductos());
        return "pedidos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarPedido(@ModelAttribute Pedido pedido,
                                @RequestParam("tienda") Long tiendaId,
                                @RequestParam(value = "productoId", required = false) List<Long> productoIds,
                                @RequestParam(value = "cantidad", required = false) List<Integer> cantidades) {

        Tienda tienda = tiendaService.obtenerTiendaPorId(tiendaId);
        pedido.setTienda(tienda);
        Pedido saved = pedidoService.guardarPedido(pedido);

        if (productoIds != null && cantidades != null) {
            for (int i = 0; i < productoIds.size(); i++) {
                Long prodId = productoIds.get(i);
                Integer cant = cantidades.get(i);
                if (prodId != null && cant != null && cant > 0) {
                    Producto producto = productoService.obtenerProductoPorId(prodId);
                    DetallePedido detalle = new DetallePedido();
                    detalle.setPedido(saved);
                    detalle.setProducto(producto);
                    detalle.setCantidad(cant);
                    detallePedidoService.guardarDetalle(detalle);
                }
            }
        }

        return "redirect:/pedidos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("tiendas", tiendaService.listarTiendas());
        model.addAttribute("productos", productoService.listarProductos());
        return "pedidos/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return "redirect:/pedidos";
    }
}

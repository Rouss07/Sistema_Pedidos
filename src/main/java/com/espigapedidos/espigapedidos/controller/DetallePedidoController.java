package com.espigapedidos.espigapedidos.controller;

import com.espigapedidos.espigapedidos.entity.DetallePedido;
import com.espigapedidos.espigapedidos.entity.Pedido;
import com.espigapedidos.espigapedidos.entity.Producto;
import com.espigapedidos.espigapedidos.service.DetallePedidoService;
import com.espigapedidos.espigapedidos.service.PedidoService;
import com.espigapedidos.espigapedidos.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/detalle-pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;
    private final PedidoService pedidoService;
    private final ProductoService productoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService,
                                   PedidoService pedidoService,
                                   ProductoService productoService) {
        this.detallePedidoService = detallePedidoService;
        this.pedidoService = pedidoService;
        this.productoService = productoService;
    }

    @GetMapping("/{pedidoId}")
    public String verDetallePedido(@PathVariable Long pedidoId, Model model) {
        model.addAttribute("detalles", detallePedidoService.listarPorPedido(pedidoId));
        model.addAttribute("pedido", pedidoService.obtenerPedidoPorId(pedidoId));
        return "detallepedido/lista";
    }

    @GetMapping("/nuevo/{pedidoId}")
    public String mostrarFormularioNuevo(@PathVariable Long pedidoId, Model model) {
        model.addAttribute("pedido", pedidoService.obtenerPedidoPorId(pedidoId));
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("detallePedido", new DetallePedido());
        return "detallepedido/formulario";
    }

    @PostMapping("/guardar")
    public String guardarDetalle(@ModelAttribute DetallePedido detallePedido,
                                 @RequestParam("pedidoId") Long pedidoId,
                                 @RequestParam("productoId") Long productoId) {

        Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId);
        Producto producto = productoService.obtenerProductoPorId(productoId);

        detallePedido.setPedido(pedido);
        detallePedido.setProducto(producto);

        detallePedidoService.guardarDetalle(detallePedido);

        return "redirect:/detalle-pedido/" + pedidoId;
    }

    @GetMapping("/eliminar/{id}/{pedidoId}")
    public String eliminarDetalle(@PathVariable Long id, @PathVariable Long pedidoId) {
        detallePedidoService.eliminarDetalle(id);
        return "redirect:/detalle-pedido/" + pedidoId;
    }
}

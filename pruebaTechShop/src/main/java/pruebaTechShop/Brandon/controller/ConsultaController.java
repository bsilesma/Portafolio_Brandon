package pruebaTechShop.Brandon.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pruebaTechShop.Brandon.service.ProductoService;

@Controller
@RequestMapping("/pruebas")
public class ConsultaController {

    @Autowired
    private ProductoService productoService;

    // Muestra la página con los formularios de consulta y (por defecto) todos los productos.
    @GetMapping("/listado")
    public String listado(Model model) {
        var productos = productoService.getProductos(false);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        return "/consultas/listado";
    }

    // 1. Consulta DERIVADA (findByPrecioBetweenOrderByPrecioAsc)
    @PostMapping("/consultaDerivada")
    public String consultaDerivada(Model model,
            @RequestParam("precioInf") BigDecimal precioInf,
            @RequestParam("precioSup") BigDecimal precioSup) {
        var productos = productoService.consultaDerivada(precioInf, precioSup);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("precioInf", precioInf);
        model.addAttribute("precioSup", precioSup);
        return "/consultas/listado";
    }

    // 2. Consulta JPQL
    @PostMapping("/consultaJPQL")
    public String consultaJPQL(Model model,
            @RequestParam("precioInf") BigDecimal precioInf,
            @RequestParam("precioSup") BigDecimal precioSup) {
        var productos = productoService.consultaJPQL(precioInf, precioSup);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("precioInf", precioInf);
        model.addAttribute("precioSup", precioSup);
        return "/consultas/listado";
    }

    // 3. Consulta SQL nativa
    @PostMapping("/consultaSQL")
    public String consultaSQL(Model model,
            @RequestParam("precioInf") BigDecimal precioInf,
            @RequestParam("precioSup") BigDecimal precioSup) {
        var productos = productoService.consultaSQL(precioInf, precioSup);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("precioInf", precioInf);
        model.addAttribute("precioSup", precioSup);
        return "/consultas/listado";
    }
}

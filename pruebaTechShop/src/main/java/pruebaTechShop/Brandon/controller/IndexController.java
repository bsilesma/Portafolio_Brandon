package pruebaTechShop.Brandon.controller;

import pruebaTechShop.Brandon.service.CategoriaService;
import pruebaTechShop.Brandon.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pruebaTechShop.Brandon.domain.Categoria;

@Controller
public class IndexController {

    // Inyección de dependencias recomendada por Spring (usando constructor y final)
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public IndexController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public String cargarPaginaInicio(Model model) {
        var lista = productoService.getProductos(true);
        model.addAttribute("productos", lista);
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "index"; // Importante: Sin la barra diagonal ("/") inicial para Thymeleaf
    }

    @GetMapping("/consultas/{idCategoria}")
    public String listado(@PathVariable("idCategoria") Integer idCategoria, Model model) {
        model.addAttribute("idCategoriaActual", idCategoria);

        // 1. Creamos un objeto "molde" falso solo con el ID, tal como lo exige tu Servicio
        Categoria categoriaBuscar = new Categoria();
        categoriaBuscar.setIdCategoria(idCategoria);

        // 2. Ejecutamos la búsqueda enviando el objeto
        Categoria categoria = categoriaService.getCategoria(categoriaBuscar);

        // 3. Como tu servicio devuelve un objeto normal (y no un Optional), 
        // validamos directamente si es nulo
        if (categoria == null) {
            model.addAttribute("productos", java.util.Collections.emptyList());
        } else {
            var productos = categoria.getProductos(); // Magia de @OneToMany
            model.addAttribute("productos", productos);
        }

        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);

        return "index";
    }
}

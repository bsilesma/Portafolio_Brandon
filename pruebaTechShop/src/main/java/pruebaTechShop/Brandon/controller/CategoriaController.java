package pruebaTechShop.Brandon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pruebaTechShop.Brandon.domain.Categoria;
import pruebaTechShop.Brandon.service.CategoriaService;

@Controller
@RequestMapping("/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/listado")
    public String listado(Model model) {
        var categorias = categoriaService.getCategorias(false);
        model.addAttribute("categorias", categorias);
        model.addAttribute("totalCategorias", categorias.size());
        return "/categoria/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Categoria categoria) {
        return "/categoria/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(Categoria categoria, @RequestParam("imagenFile") MultipartFile imagenFile) {
        categoriaService.save(categoria, imagenFile);
        return "redirect:/categoria/listado";
    }

    // --- NUEVO: Ruta para editar (Slide 17) ---
    @GetMapping("/modificar/{idCategoria}")
    public String modificar(Categoria categoria, Model model) {
        categoria = categoriaService.getCategoria(categoria);
        model.addAttribute("categoria", categoria);
        return "/categoria/modifica";
    }

    @PostMapping("/eliminar")
    public String eliminar(Categoria categoria, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.delete(categoria);
            // Si logra eliminar, mandamos el mensaje de éxito (Toast verde)
            redirectAttributes.addFlashAttribute("todoOk", "La categoría fue eliminada con éxito.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Si la base de datos bloquea la eliminación, mandamos error (Toast rojo)
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la categoría porque tiene productos asociados.");
        } catch (Exception e) {
            // Para cualquier otro error inesperado
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al intentar eliminar.");
        }   
        return "redirect:/categoria/listado";
    }
}

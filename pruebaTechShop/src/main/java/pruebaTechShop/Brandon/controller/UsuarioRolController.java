package pruebaTechShop.Brandon.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pruebaTechShop.Brandon.domain.Usuario;
import pruebaTechShop.Brandon.service.UsuarioService;

/* Lec13: permite otorgar y revocar roles a los usuarios del sistema. */
@Controller
@RequestMapping("/usuario_rol")
public class UsuarioRolController {

    private final UsuarioService usuarioService;

    public UsuarioRolController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 1. Vista inicial (todavía sin buscar ningún usuario)
    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("usuario", new Usuario());
        // Se inicializan listas vacías para evitar errores de Thymeleaf
        model.addAttribute("rolesAsignados", Collections.emptySet());
        model.addAttribute("rolesDisponibles", Collections.emptyList());
        return "usuario_rol/mantenimiento";
    }

    // 2. Busca el usuario y muestra sus roles asignados y los disponibles
    @GetMapping("/buscar")
    public String buscarUsuario(@RequestParam("username") String username, Model model) {
        Usuario usuario = usuarioService.getUsuarioPorUsername(username).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("buscado", username);

        if (usuario != null) {
            List<String> todosRolesNombres = usuarioService.getRolesNombres();

            // Los disponibles son los que el usuario todavía NO tiene
            List<String> rolesDisponibles = todosRolesNombres.stream()
                    .filter(rolNombre -> usuario.getRoles().stream()
                    .noneMatch(rolAsignado -> rolAsignado.getRol().equals(rolNombre)))
                    .toList();

            model.addAttribute("rolesAsignados", usuario.getRoles());
            model.addAttribute("rolesDisponibles", rolesDisponibles);
        } else {
            model.addAttribute("rolesAsignados", Collections.emptySet());
            model.addAttribute("rolesDisponibles", Collections.emptyList());
        }

        return "usuario_rol/mantenimiento"; // Vuelve a la misma página
    }

    // 3. Otorga un rol
    @GetMapping("/agregar")
    public String agregarRol(@RequestParam("username") String username,
            @RequestParam("nombreRol") String nombreRol) {

        usuarioService.asignarRolPorUsername(username, nombreRol);

        // Se vuelve a /buscar para recargar los datos del usuario actualizado
        return "redirect:/usuario_rol/buscar?username=" + username;
    }

    // 4. Revoca un rol
    @GetMapping("/eliminar")
    public String eliminarRol(@RequestParam("username") String username,
            @RequestParam("idRol") Integer idRol) {

        usuarioService.eliminarRol(username, idRol);

        return "redirect:/usuario_rol/buscar?username=" + username;
    }
}

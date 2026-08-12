package pruebaTechShop.Brandon.controller;

import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pruebaTechShop.Brandon.domain.Usuario;
import pruebaTechShop.Brandon.service.UsuarioService;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public UsuarioController(UsuarioService usuarioService,
            MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String inicio(Model model) {
        var usuarios = usuarioService.getUsuarios(false);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "/usuario/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Usuario usuario,
            BindingResult bindingResult,
            @RequestParam MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error04", null, Locale.getDefault()));
            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }
            return "redirect:/usuario/modificar/" + usuario.getIdUsuario();
        }
        try {
            usuarioService.save(usuario, imagenFile, true);
        } catch (DataIntegrityViolationException e) {
            // El username o el correo ya están ocupados por otro usuario. Se usa el
            // mensaje que nombra ambos campos, porque cualquiera de los dos pudo chocar.
            redirectAttributes.addFlashAttribute("error",
                    String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo",
                            null, Locale.getDefault()), usuario.getUsername(), usuario.getCorreo()));
            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }
            return "redirect:/usuario/modificar/" + usuario.getIdUsuario();
        }
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("mensaje.actualizado",
                        null, Locale.getDefault()));
        return "redirect:/usuario/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idUsuario,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.delete(idUsuario);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("mensaje.eliminado", null,
                            Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error01", null,
                            Locale.getDefault()));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error02", null,
                            Locale.getDefault()));
        } catch (NoSuchMessageException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error03", null,
                            Locale.getDefault()));
        }
        return "redirect:/usuario/listado";
    }

    @GetMapping("/modificar/{idUsuario}")
    public String modificar(@PathVariable("idUsuario") Integer idUsuario,
            Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.getUsuario(idUsuario);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "El usuario no fue encontrado.");
            return "redirect:/usuario/listado";
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setPassword("");
        model.addAttribute("usuario", usuario);
        return "/usuario/modifica";
    }
}

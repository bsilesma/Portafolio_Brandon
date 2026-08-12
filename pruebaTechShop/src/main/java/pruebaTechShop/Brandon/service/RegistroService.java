package pruebaTechShop.Brandon.service;

import jakarta.mail.MessagingException;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import pruebaTechShop.Brandon.domain.Usuario;

/* Lec11: proceso de registro de nuevos usuarios y recuperación de la cuenta. */
@Service
public class RegistroService {

    private final CorreoService correoService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;
    private final ConstanteService constanteService;

    public RegistroService(CorreoService correoService, UsuarioService usuarioService,
            MessageSource messageSource, ConstanteService constanteService) {
        this.correoService = correoService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
        this.constanteService = constanteService;
    }

    //Este método se usa en el enlace del correo enviado...
    public Model activar(Model model, String username, String clave) {
        Optional<Usuario> usuario = usuarioService.getUsuarioPorUsernameYPassword(username, clave);
        if (!usuario.isEmpty()) {  //Si estaba...
            model.addAttribute("usuario", usuario.get());
        } else { //hay que devolver error
            model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje", messageSource.getMessage("registro.activar.error", null, Locale.getDefault()));
        }
        return model;
    }

    //Este método es el que finalmente crea el usuario en el sistema.
    //En el formulario de activación el usuario puede editar su username y su correo,
    //así que puede chocar con los de otra cuenta: se devuelve true solo si se activó.
    public boolean activar(Usuario usuario, MultipartFile imagenFile, Model model) {
        try {
            usuario.setActivo(true);
            usuarioService.save(usuario, imagenFile, true);
            return true;
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje", String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getUsername(), usuario.getCorreo()));
            return false;
        }
    }

    public Model crearUsuario(Model model, Usuario usuario) throws MessagingException {
        String mensaje;
        // Si el username o el correo ya están registrados no se intenta crear nada:
        // se devuelve el mensaje correspondiente.
        if (usuarioService.existeUsuarioPorUsernameOCorreo(usuario.getUsername(), usuario.getCorreo())) {
            mensaje = String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getUsername(), usuario.getCorreo());
            model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje", mensaje);
            return model;
        }
        try {
            String clave = demeClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);
            usuarioService.save(usuario, null, false);
            enviaCorreoActivar(usuario, clave);
            mensaje = String.format(messageSource.getMessage("registro.mensaje.activacion.ok", null, Locale.getDefault()), usuario.getCorreo());
        } catch (MessagingException | NoSuchMessageException | DataIntegrityViolationException e) {
            // Red de seguridad: cubre también la carrera entre dos registros simultáneos
            // y cualquier fallo al enviar el correo.
            mensaje = String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getUsername(), usuario.getCorreo());
        }
        model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }

    public Model recordarUsuario(Model model, Usuario usuario)
            throws MessagingException {
        String mensaje;
        // Se busca primero por username y, si no aparece, por correo. No se consultan
        // los dos campos a la vez porque si el username es de una cuenta y el correo de
        // otra la consulta devolvería dos filas y fallaría.
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioPorUsername(usuario.getUsername());
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioService.getUsuarioPorUsernameOCorreo(null, usuario.getCorreo());
        }
        if (!usuarioOpt.isEmpty()) {
            usuario = usuarioOpt.get();
            String clave = demeClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);
            usuarioService.save(usuario, null, false);
            enviaCorreoRecordar(usuario, clave);
            mensaje = String.format(messageSource.getMessage("registro.mensaje.recordar.ok", null, Locale.getDefault()), usuario.getCorreo());
        } else {
            mensaje = String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getUsername(), usuario.getCorreo());
        }
        model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }

    private String demeClave() {
        String tira = "ABCDEFGHIJKLMNOPQRSTUXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String clave = "";
        for (int i = 0; i < 40; i++) {
            clave += tira.charAt((int) (Math.random() * tira.length()));
        }
        return clave;
    }

    /* Lec13: la dirección del servidor ya no se lee de application.properties sino de la
       tabla constante, así se puede cambiar desde la pantalla de Constantes sin tocar el
       código. Se consulta al momento de enviar el correo para que el cambio aplique de
       inmediato, sin reiniciar la aplicación. */
    private String getServidor() {
        return constanteService.getValor("servidor.http", "http://localhost:8080");
    }

    private void enviaCorreoActivar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage("registro.correo.activar", null, Locale.getDefault());
        mensaje = String.format(mensaje, usuario.getNombre(), usuario.getApellidos(), getServidor(), usuario.getUsername(), clave);
        String asunto = messageSource.getMessage("registro.mensaje.activacion", null, Locale.getDefault());
        correoService.enviarCorreoHtml(usuario.getCorreo(), asunto, mensaje);
    }

    private void enviaCorreoRecordar(Usuario usuario, String clave) throws MessagingException {
        String mensaje = messageSource.getMessage("registro.correo.recordar", null, Locale.getDefault());
        mensaje = String.format(mensaje, usuario.getNombre(), usuario.getApellidos(), getServidor(), usuario.getUsername(), clave);
        String asunto = messageSource.getMessage("registro.mensaje.recordar", null, Locale.getDefault());
        correoService.enviarCorreoHtml(usuario.getCorreo(), asunto, mensaje);
    }
}

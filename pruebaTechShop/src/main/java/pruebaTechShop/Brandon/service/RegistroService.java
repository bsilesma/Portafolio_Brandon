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

// Lec11: proceso de registro de nuevos usuarios y recuperación de la cuenta.
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

    public Model activar(Model model, String username, String clave) {
        Optional<Usuario> usuario = usuarioService.getUsuarioPorUsernameYPassword(username, clave);
        if (!usuario.isEmpty()) {
            model.addAttribute("usuario", usuario.get());
        } else {
            model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje", messageSource.getMessage("registro.activar.error", null, Locale.getDefault()));
        }
        return model;
    }

    // En la activación el usuario puede editar su username y correo, y chocar con otra
    // cuenta: devuelve true solo si se logró activar.
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
            // Cubre dos registros simultáneos y los fallos al enviar el correo.
            mensaje = String.format(messageSource.getMessage("registro.mensaje.usuario.o.correo", null, Locale.getDefault()), usuario.getUsername(), usuario.getCorreo());
        }
        model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
        model.addAttribute("mensaje", mensaje);
        return model;
    }

    public Model recordarUsuario(Model model, Usuario usuario)
            throws MessagingException {
        String mensaje;
        // Primero por username y luego por correo: consultarlos juntos podría devolver
        // dos filas si pertenecen a cuentas distintas, y la consulta fallaría.
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

    // Sale de la tabla constante, no de application.properties. Se lee al enviar el
    // correo para que un cambio desde la pantalla de Constantes aplique sin reiniciar.
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

package pruebaTechShop.Brandon.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pruebaTechShop.Brandon.domain.Rol;
import pruebaTechShop.Brandon.domain.Usuario;
import pruebaTechShop.Brandon.repository.RolRepository;
import pruebaTechShop.Brandon.repository.UsuarioRepository;

@Service
public class UsuarioService {

    // Rol por defecto de todo usuario nuevo. Debe existir en la columna rol de la tabla rol.
    private static final String ROL_POR_DEFECTO = "USER";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            FirebaseStorageService firebaseStorageService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameYPassword(String username,
            String password) {
        return usuarioRepository.findByUsernameAndPassword(username, password);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameOCorreo(String username,
            String correo) {
        return usuarioRepository.findByUsernameOrCorreo(username, correo);
    }

    @Transactional(readOnly = true)
    public boolean existeUsuarioPorUsernameOCorreo(String username,
            String correo) {
        return usuarioRepository.existsByUsernameOrCorreo(username, correo);
    }

    @Transactional
    public void save(Usuario usuario, MultipartFile imagenFile, boolean encriptaClave) {
        // username y correo son UNIQUE en la base. Sin esta validación el INSERT falla
        // contra el índice de MySQL y el usuario ve un error 500 en vez de un aviso.
        final Integer idUser = usuario.getIdUsuario();
        if (idUser == null) {
            // Al crear: ninguno de los dos campos puede estar ocupado.
            if (usuarioRepository.existsByUsernameOrCorreo(usuario.getUsername(), usuario.getCorreo())) {
                throw new DataIntegrityViolationException("El username o el correo ya están en uso.");
            }
        } else {
            // Al modificar: pueden repetirse solo consigo mismo. Se consulta campo por campo
            // porque una consulta combinada podría devolver dos filas y fallar.
            Optional<Usuario> porCorreo = usuarioRepository.findByUsernameOrCorreo(null, usuario.getCorreo());
            if (porCorreo.isPresent() && !porCorreo.get().getIdUsuario().equals(idUser)) {
                throw new DataIntegrityViolationException("El correo ya está en uso por otro usuario.");
            }
            Optional<Usuario> porUsername = usuarioRepository.findByUsername(usuario.getUsername());
            if (porUsername.isPresent() && !porUsername.get().getIdUsuario().equals(idUser)) {
                throw new DataIntegrityViolationException("El username ya está en uso por otro usuario.");
            }
        }

        var asignarRol = false;
        if (usuario.getIdUsuario() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios.");
            }
            // En la activación la clave llega ya generada, por eso no siempre se encripta.
            usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            asignarRol = true;
        } else {
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario a modificar no encontrado."));

            // Los roles no viajan en el formulario y Usuario es el lado dueño del @ManyToMany:
            // guardar sin ellos borraría las filas de usuario_rol, así que se reatan.
            usuario.setRoles(new HashSet<>(usuarioExistente.getRoles()));

            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            }
        }
        usuario = usuarioRepository.save(usuario);
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String rutaImagen = firebaseStorageService.uploadImage(
                        imagenFile, "usuario", usuario.getIdUsuario());
                usuario.setRutaImagen(rutaImagen);
                usuarioRepository.save(usuario);
            } catch (IOException e) {
            }
        }
        if (asignarRol) {
            asignarRolPorUsername(usuario.getUsername(), ROL_POR_DEFECTO);
        }
    }

    @Transactional
    public void delete(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException(
                    "El usuario con ID " + idUsuario + " no existe.");
        }
        try {
            usuarioRepository.deleteById(idUsuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No se puede eliminar el usuario. Tiene datos asociados.", e);
        }
    }

    @Transactional
    public Usuario asignarRolPorUsername(String username, String rolStr) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }
        Usuario usuario = usuarioOpt.get();
        Optional<Rol> rolOpt = rolRepository.findByRol(rolStr);
        if (rolOpt.isEmpty()) {
            throw new RuntimeException("Rol no encontrado.");
        }
        Rol rol = rolOpt.get();
        usuario.getRoles().add(rol);
        return usuarioRepository.save(usuario);
    }

    // Lec13: otorgar y revocar roles a un usuario.

    @Transactional(readOnly = true)
    public List<String> getRolesNombres() {
        return rolRepository.findAll().stream()
                .map(Rol::getRol)
                .toList();
    }

    @Transactional
    public Usuario eliminarRol(String username, Integer idRol) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }
        Usuario usuario = usuarioOpt.get();

        usuario.getRoles().removeIf(rol -> rol.getIdRol().equals(idRol));

        return usuarioRepository.save(usuario);
    }
}

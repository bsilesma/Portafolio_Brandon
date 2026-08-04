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

    // Rol que se asigna por defecto a todo usuario recién creado.
    // Debe coincidir EXACTAMENTE con un valor de la columna rol de la tabla rol.
    // La Lec09 define el rol del cliente final como ROLE_USER, por eso es "USER".
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
        // Se valida que el username y el correo no estén ocupados. Ambas columnas son
        // UNIQUE en la base de datos; si no se validan aquí, el INSERT falla contra el
        // índice de MySQL y el error sale como un 500 en lugar de un mensaje entendible.
        final Integer idUser = usuario.getIdUsuario();
        if (idUser == null) {
            // CREACIÓN: no debe existir ningún usuario con ese username ni con ese correo.
            if (usuarioRepository.existsByUsernameOrCorreo(usuario.getUsername(), usuario.getCorreo())) {
                throw new DataIntegrityViolationException("El username o el correo ya están en uso.");
            }
        } else {
            // MODIFICACIÓN: pueden repetirse, pero solo consigo mismo.
            // Se consulta cada campo por separado porque los dos son UNIQUE y así cada
            // consulta devuelve como máximo una fila.
            Optional<Usuario> porCorreo = usuarioRepository.findByUsernameOrCorreo(null, usuario.getCorreo());
            if (porCorreo.isPresent() && !porCorreo.get().getIdUsuario().equals(idUser)) {
                throw new DataIntegrityViolationException("El correo ya está en uso por otro usuario.");
            }
            Optional<Usuario> porUsername = usuarioRepository.findByUsername(usuario.getUsername());
            if (porUsername.isPresent() && !porUsername.get().getIdUsuario().equals(idUser)) {
                throw new DataIntegrityViolationException("El username ya está en uso por otro usuario.");
            }
        }

        //Se valida si la clave se va actualizar o si es un usuario nuevo se debe actualizar...
        var asignarRol = false;
        if (usuario.getIdUsuario() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios.");
            }
            //La primera vez como es activación no se encripta...
            usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            asignarRol = true;
        } else {
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario a modificar no encontrado."));

            // Los roles no viajan en el formulario. Como Usuario es el lado dueño de la
            // relación @ManyToMany, guardar con la colección vacía borraría las filas de
            // usuario_rol; por eso se conservan los roles que el usuario ya tenía.
            usuario.setRoles(new HashSet<>(usuarioExistente.getRoles()));

            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                // El campo de password en el formulario viene vacío (no se desea actualizar).
                // Se conserva la contraseña que ya estaba almacenada.
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                // El campo de password NO está vacío (se desea actualizar).
                // Se encripta y se guarda la nueva contraseña.
                usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            }
        }
        usuario = usuarioRepository.save(usuario);
        if (imagenFile != null && !imagenFile.isEmpty()) { //Si no está vacío... pasaron una imagen...
            try {
                String rutaImagen = firebaseStorageService.uploadImage(
                        imagenFile, "usuario", usuario.getIdUsuario());
                usuario.setRutaImagen(rutaImagen);
                usuarioRepository.save(usuario);
            } catch (IOException e) {
            }
        }
        if (asignarRol) {
            //Si se está creando el usuario, se le asigna el rol por defecto
            asignarRolPorUsername(usuario.getUsername(), ROL_POR_DEFECTO);
        }
    }

    @Transactional
    public void delete(Integer idUsuario) {
        // Verifica si el usuario existe antes de intentar eliminarlo
        if (!usuarioRepository.existsById(idUsuario)) {
            // Lanza una excepción para indicar que el usuario no fue encontrado
            throw new IllegalArgumentException(
                    "El usuario con ID " + idUsuario + " no existe.");
        }
        try {
            usuarioRepository.deleteById(idUsuario);
        } catch (DataIntegrityViolationException e) {
            // Excepción para encapsular el problema de integridad de datos
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
}

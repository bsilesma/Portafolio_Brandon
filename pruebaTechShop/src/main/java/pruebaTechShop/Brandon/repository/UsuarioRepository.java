package pruebaTechShop.Brandon.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pruebaTechShop.Brandon.domain.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Consulta derivada: busca al usuario por el username del formulario de login,
    // siempre que esté activo.
    public Optional<Usuario> findByUsernameAndActivoTrue(String username);
}

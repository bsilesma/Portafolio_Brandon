package pruebaTechShop.Brandon.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pruebaTechShop.Brandon.domain.Constante;

@Repository
public interface ConstanteRepository extends JpaRepository<Constante, Integer> {

    // Lec13: consulta derivada para ubicar una constante por su nombre.
    public Optional<Constante> findByAtributo(String atributo);
}

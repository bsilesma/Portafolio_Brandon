package pruebaTechShop.Brandon.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pruebaTechShop.Brandon.domain.Ruta;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer> {

    // Consulta derivada: las rutas públicas (requiereRol = false) salen primero,
    // de modo que se registren antes que las restringidas en la cadena de filtros.
    public List<Ruta> findAllByOrderByRequiereRolAsc();
}

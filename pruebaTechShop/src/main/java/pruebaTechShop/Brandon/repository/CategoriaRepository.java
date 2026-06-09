package pruebaTechShop.Brandon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pruebaTechShop.Brandon.domain.Categoria;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByActivo(Boolean activo);
}
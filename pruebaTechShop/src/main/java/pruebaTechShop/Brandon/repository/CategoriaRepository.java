package pruebaTechShop.Brandon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pruebaTechShop.Brandon.domain.Categoria;

// Cambiamos el Long por Integer para que coincida con la llave primaria de la entidad
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
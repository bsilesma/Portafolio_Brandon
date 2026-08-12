package pruebaTechShop.Brandon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pruebaTechShop.Brandon.domain.Categoria;

// El Integer coincide con el tipo de la llave primaria de Categoria.
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
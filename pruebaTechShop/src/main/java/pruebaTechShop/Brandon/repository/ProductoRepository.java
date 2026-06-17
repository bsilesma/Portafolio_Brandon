package pruebaTechShop.Brandon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pruebaTechShop.Brandon.domain.Producto;

// ¡Aquí está la magia! Cambiamos Long por Integer
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

}

package pruebaTechShop.Brandon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pruebaTechShop.Brandon.domain.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

}

package pruebaTechShop.Brandon.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pruebaTechShop.Brandon.domain.Factura;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    // Trae la factura con usuario, ventas y productos en una sola consulta.
    @Query("SELECT f FROM Factura f "
            + "LEFT JOIN FETCH f.usuario u "
            + "LEFT JOIN FETCH f.ventas v "
            + "LEFT JOIN FETCH v.producto p "
            + "WHERE f.idFactura = :idFactura")
    Optional<Factura> findByIdFacturaConDetalle(@Param("idFactura") Integer idFactura);
}

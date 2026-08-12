package pruebaTechShop.Brandon.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pruebaTechShop.Brandon.domain.Producto;

// El Integer coincide con el tipo de la llave primaria de Producto.
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Consulta derivada: Spring Data arma el SQL a partir del nombre del método.
    List<Producto> findByPrecioBetweenOrderByPrecioAsc(BigDecimal precioInf, BigDecimal precioSup);

    // JPQL: se escribe sobre la entidad Producto y sus atributos, no sobre la tabla.
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioInf AND :precioSup ORDER BY p.precio ASC")
    List<Producto> consultaJPQL(@Param("precioInf") BigDecimal precioInf,
                                @Param("precioSup") BigDecimal precioSup);

    // SQL nativa: se ejecuta tal cual contra la tabla y las columnas de MySQL.
    @Query(value = "SELECT * FROM producto WHERE precio BETWEEN :precioInf AND :precioSup ORDER BY precio ASC",
            nativeQuery = true)
    List<Producto> consultaSQL(@Param("precioInf") BigDecimal precioInf,
                               @Param("precioSup") BigDecimal precioSup);
}

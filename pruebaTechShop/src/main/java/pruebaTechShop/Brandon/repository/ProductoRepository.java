package pruebaTechShop.Brandon.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pruebaTechShop.Brandon.domain.Producto;

// ¡Aquí está la magia! Cambiamos Long por Integer
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // 1. Consulta DERIVADA: Spring Data JPA construye la consulta a partir del nombre del método.
    //    Busca productos con precio dentro de un rango y los ordena por precio ascendente.
    List<Producto> findByPrecioBetweenOrderByPrecioAsc(BigDecimal precioInf, BigDecimal precioSup);

    // 2. Consulta JPQL: se escribe explícitamente sobre las ENTIDADES (Producto) y sus atributos.
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioInf AND :precioSup ORDER BY p.precio ASC")
    List<Producto> consultaJPQL(@Param("precioInf") BigDecimal precioInf,
                                @Param("precioSup") BigDecimal precioSup);

    // 3. Consulta SQL NATIVA: se ejecuta directamente sobre las TABLAS y columnas de la base de datos.
    @Query(value = "SELECT * FROM producto WHERE precio BETWEEN :precioInf AND :precioSup ORDER BY precio ASC",
            nativeQuery = true)
    List<Producto> consultaSQL(@Param("precioInf") BigDecimal precioInf,
                               @Param("precioSup") BigDecimal precioSup);
}

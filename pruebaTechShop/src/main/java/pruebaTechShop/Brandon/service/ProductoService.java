package pruebaTechShop.Brandon.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pruebaTechShop.Brandon.domain.Producto;

public interface ProductoService {

    public List<Producto> getProductos(boolean activos);

    public Producto getProducto(Producto producto);

    public void save(Producto producto, MultipartFile imagenFile);

    public void delete(Producto producto);

    // --- Lec08: las 3 formas de consultar productos por rango de precio ---
    public List<Producto> consultaDerivada(BigDecimal precioInf, BigDecimal precioSup);

    public List<Producto> consultaJPQL(BigDecimal precioInf, BigDecimal precioSup);

    public List<Producto> consultaSQL(BigDecimal precioInf, BigDecimal precioSup);
}

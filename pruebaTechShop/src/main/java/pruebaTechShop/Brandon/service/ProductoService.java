package pruebaTechShop.Brandon.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pruebaTechShop.Brandon.domain.Producto;

public interface ProductoService {

    public List<Producto> getProductos(boolean activos);

    public Producto getProducto(Producto producto);

    public void save(Producto producto, MultipartFile imagenFile);

    public void delete(Producto producto);
}

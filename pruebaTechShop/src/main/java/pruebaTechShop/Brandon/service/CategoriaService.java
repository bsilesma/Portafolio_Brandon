package pruebaTechShop.Brandon.service;

import java.util.List;
import java.util.Optional; // Asegúrate de importar esto
import org.springframework.web.multipart.MultipartFile;
import pruebaTechShop.Brandon.domain.Categoria;

public interface CategoriaService {

    public List<Categoria> getCategorias(boolean activos);

    public Categoria getCategoria(Categoria categoria);

    public void save(Categoria categoria, MultipartFile imagenFile);

    public void delete(Categoria categoria);
}

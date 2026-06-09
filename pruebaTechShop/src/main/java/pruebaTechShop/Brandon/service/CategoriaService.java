package pruebaTechShop.Brandon.service;

import pruebaTechShop.Brandon.domain.Categoria;
import java.util.List;

public interface CategoriaService {
    List<Categoria> getCategorias(boolean soloActivas);
}
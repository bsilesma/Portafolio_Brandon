package pruebaTechShop.Brandon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pruebaTechShop.Brandon.domain.Categoria;
import pruebaTechShop.Brandon.repository.CategoriaRepository;
import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> getCategorias(boolean soloActivas) {
        if (soloActivas) {
            return categoriaRepository.findByActivo(true);
        }
        return categoriaRepository.findAll();
    }
}
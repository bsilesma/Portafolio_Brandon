package pruebaTechShop.Brandon.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pruebaTechShop.Brandon.domain.Constante;
import pruebaTechShop.Brandon.repository.ConstanteRepository;

@Service
public class ConstanteService {

    private final ConstanteRepository constanteRepository;

    public ConstanteService(ConstanteRepository constanteRepository) {
        this.constanteRepository = constanteRepository;
    }

    @Transactional(readOnly = true)
    public List<Constante> getConstantes() {
        return constanteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Constante getConstante(Integer idConstante) {
        return constanteRepository.findById(idConstante).orElseThrow(
                () -> new NoSuchElementException("Constante con ID " + idConstante + " no encontrada."));
    }

    @Transactional
    public void save(Constante constante) {
        constanteRepository.save(constante);
    }

    @Transactional
    public void delete(Integer idConstante) {
        if (!constanteRepository.existsById(idConstante)) {
            throw new IllegalArgumentException("La Constante con ID " + idConstante + " no existe.");
        }
        try {
            constanteRepository.deleteById(idConstante);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar la constante. Tiene datos asociados.", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Constante> findByAtributo(String atributo) {
        return constanteRepository.findByAtributo(atributo);
    }

    // Lee el valor de una constante, con un respaldo si no existe. Se consulta al usarla
    // para que un cambio desde la pantalla de Constantes aplique sin reiniciar.
    @Transactional(readOnly = true)
    public String getValor(String atributo, String porDefecto) {
        return constanteRepository.findByAtributo(atributo)
                .map(Constante::getValor)
                .orElse(porDefecto);
    }
}

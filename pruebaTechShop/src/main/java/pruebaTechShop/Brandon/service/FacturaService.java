package pruebaTechShop.Brandon.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pruebaTechShop.Brandon.domain.Factura;
import pruebaTechShop.Brandon.repository.FacturaRepository;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    @Transactional(readOnly = true)
    public Factura getFacturaConVentas(Integer idFactura) {
        return facturaRepository.findByIdFacturaConDetalle(idFactura)
                .orElseThrow(() -> new NoSuchElementException("Factura con ID " + idFactura + " no encontrada."));
    }
}

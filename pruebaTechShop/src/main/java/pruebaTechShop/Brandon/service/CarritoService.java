package pruebaTechShop.Brandon.service;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pruebaTechShop.Brandon.domain.EstadoFactura;
import pruebaTechShop.Brandon.domain.Factura;
import pruebaTechShop.Brandon.domain.Item;
import pruebaTechShop.Brandon.domain.Producto;
import pruebaTechShop.Brandon.domain.Usuario;
import pruebaTechShop.Brandon.domain.Venta;
import pruebaTechShop.Brandon.repository.FacturaRepository;
import pruebaTechShop.Brandon.repository.ProductoRepository;
import pruebaTechShop.Brandon.repository.VentaRepository;

// Lec12: lógica del carrito de compras. El carrito vive en la sesión HTTP hasta
// que se factura; ahí se convierte en Factura + Ventas y se descuenta el stock.
@Service
public class CarritoService {

    private static final String ATTRIBUTE_CARRITO = "carrito";

    private final ProductoRepository productoRepository;
    private final FacturaRepository facturaRepository;
    private final VentaRepository ventaRepository;

    public CarritoService(ProductoRepository productoRepository,
            FacturaRepository facturaRepository,
            VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.facturaRepository = facturaRepository;
        this.ventaRepository = ventaRepository;
    }

    public List<Item> obtenerCarrito(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Item> carrito = (List<Item>) session.getAttribute(ATTRIBUTE_CARRITO);
        if (carrito == null) {
            carrito = new ArrayList<>();
        }
        return carrito;
    }

    public void guardarCarrito(HttpSession session, List<Item> carrito) {
        session.setAttribute(ATTRIBUTE_CARRITO, carrito);
    }

    public void agregarProducto(List<Item> carrito, Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

        Optional<Item> itemExistente = carrito.stream()
                .filter(i -> i.getProducto().getIdProducto().equals(idProducto))
                .findFirst();

        int cantidad = 1;

        if (itemExistente.isPresent()) {
            Item item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;

            if (nuevaCantidad > producto.getExistencias()) {
                throw new RuntimeException("Stock insuficiente para agregar " + cantidad + " unidades.");
            }
            item.setCantidad(nuevaCantidad);
        } else {
            if (cantidad > producto.getExistencias()) {
                throw new RuntimeException("Stock insuficiente para agregar " + cantidad + " unidades.");
            }

            Item nuevoItem = new Item();
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioHistorico(producto.getPrecio()); // Capturar precio actual
            carrito.add(nuevoItem);
        }
    }

    public Item buscarItem(List<Item> carrito, Integer idProducto) {
        if (carrito == null) {
            return null;
        }
        return carrito.stream()
                .filter(item -> item.getProducto().getIdProducto().equals(idProducto))
                .findFirst()
                .orElse(null);
    }

    public void eliminarItem(List<Item> carrito, Integer idProducto) {
        carrito.removeIf(item -> item.getProducto().getIdProducto().equals(idProducto));
    }

    public void actualizarCantidad(List<Item> carrito, Integer idProducto, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            eliminarItem(carrito, idProducto);
            return;
        }

        Optional<Item> itemExistente = carrito.stream()
                .filter(i -> i.getProducto().getIdProducto().equals(idProducto))
                .findFirst();

        if (itemExistente.isPresent()) {
            Item item = itemExistente.get();
            Producto producto = item.getProducto();

            if (nuevaCantidad > producto.getExistencias()) {
                throw new RuntimeException("No hay suficiente stock disponible.");
            }
            item.setCantidad(nuevaCantidad);
        }
    }

    public int contarUnidades(List<Item> carrito) {
        if (carrito == null || carrito.isEmpty()) {
            return 0;
        }
        return carrito.stream()
                .mapToInt(Item::getCantidad)
                .sum();
    }

    public BigDecimal calcularTotal(List<Item> carrito) {
        return carrito.stream()
                .map(Item::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limpiarCarrito(HttpSession session) {
        List<Item> carrito = obtenerCarrito(session);
        if (carrito != null) {
            carrito.clear();
        }
        guardarCarrito(session, carrito);
    }

    @Transactional
    public Factura procesarCompra(List<Item> carrito, Usuario usuario) {
        if (carrito == null || carrito.isEmpty()) {
            throw new RuntimeException("El carrito está vacío para procesar la compra.");
        }

        Factura factura = new Factura();
        factura.setUsuario(usuario);
        factura.setFecha(LocalDateTime.now());
        factura.setTotal(calcularTotal(carrito));
        factura.setEstado(EstadoFactura.Pagada);
        factura.setFechaCreacion(LocalDateTime.now());
        factura.setFechaModificacion(LocalDateTime.now());
        factura = facturaRepository.save(factura); // Persistir para obtener el idFactura

        for (Item item : carrito) {
            // Se revalida contra la base: el stock pudo cambiar mientras el carrito estaba en sesión.
            Producto producto = productoRepository.findById(item.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("El producto ya no existe."));
            if (item.getCantidad() > producto.getExistencias()) {
                throw new RuntimeException("Fallo en la compra: El producto "
                        + producto.getDescripcion() + " ya no tiene suficiente stock.");
            }

            Venta venta = new Venta();
            venta.setFactura(factura);
            venta.setProducto(producto);
            venta.setPrecioHistorico(item.getPrecioHistorico());
            venta.setCantidad(item.getCantidad());
            venta.setFechaCreacion(LocalDateTime.now());
            venta.setFechaModificacion(LocalDateTime.now());
            ventaRepository.save(venta);

            producto.setExistencias(producto.getExistencias() - item.getCantidad());
            productoRepository.save(producto);
        }

        return factura;
    }
}

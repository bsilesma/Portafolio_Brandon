package pruebaTechShop.Brandon.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/* Lec12: línea del carrito de compras. NO es una entidad: vive únicamente en la
   sesión del usuario hasta que se procesa la compra y se convierte en Venta. */
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    // Referencia a la entidad Producto (ya cargada de la BD)
    private Producto producto;

    // Cantidad deseada por el usuario
    private int cantidad;

    private BigDecimal precioHistorico;

    public Item() {
    }

    // Método para calcular el subtotal
    public BigDecimal getSubTotal() {
        return producto.getPrecio().multiply(new BigDecimal(cantidad));
    }

    // --- GETTERS Y SETTERS MANUALES ---
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioHistorico() {
        return precioHistorico;
    }

    public void setPrecioHistorico(BigDecimal precioHistorico) {
        this.precioHistorico = precioHistorico;
    }
}

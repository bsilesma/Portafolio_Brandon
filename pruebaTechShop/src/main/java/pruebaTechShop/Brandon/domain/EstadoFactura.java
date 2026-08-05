package pruebaTechShop.Brandon.domain;

/* Lec12: estados posibles de una factura. Coincide con el ENUM de la columna
   factura.estado en la base de datos. */
public enum EstadoFactura {
    Activa("Activa"),
    Pagada("Pagada"),
    Anulada("Anulada");

    private final String valorBD;

    EstadoFactura(String valorBD) {
        this.valorBD = valorBD;
    }

    public String getValorBD() {
        return valorBD;
    }
}

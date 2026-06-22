package pruebaTechShop.Brandon.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List; // ¡No olvides este import!

@Entity
@Table(name = "categoria")
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(unique = true, nullable = false, length = 50)
    private String descripcion;

    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    @Column(name = "activo")
    private boolean activo;

    // --- NUEVA RELACIÓN (Semana 6) ---
    // Relación de uno a muchos con la clase Producto
    // Sin "cascade" ni "orphanRemoval" para evitar la propagación de operaciones.
    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos;

    public Categoria() {
    }

    // --- GETTERS Y SETTERS MANUALES ---
    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}

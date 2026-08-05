-- ============================================================================
-- Lec10: tablas para autenticación/autorización por base de datos.
-- Se usa CREATE TABLE IF NOT EXISTS porque este script corre en cada arranque
-- (spring.sql.init.mode=always) y NO debe afectar las tablas ya existentes
-- (categoria, producto).
-- ============================================================================

CREATE TABLE IF NOT EXISTS rol (
    id_rol             INT AUTO_INCREMENT PRIMARY KEY,
    rol                VARCHAR(25) NOT NULL UNIQUE,
    fecha_creacion     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario         INT AUTO_INCREMENT PRIMARY KEY,
    username           VARCHAR(30) NOT NULL UNIQUE,
    password           VARCHAR(512) NOT NULL,
    nombre             VARCHAR(20),
    apellidos          VARCHAR(30),
    correo             VARCHAR(75) UNIQUE,
    telefono           VARCHAR(25),
    ruta_imagen        VARCHAR(1024),
    activo             TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla intermedia de la relación @ManyToMany entre usuario y rol
CREATE TABLE IF NOT EXISTS usuario_rol (
    id_usuario         INT NOT NULL,
    id_rol             INT NOT NULL,
    fecha_creacion     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuarioRol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_usuarioRol_rol     FOREIGN KEY (id_rol)     REFERENCES rol (id_rol)
);

-- Reglas de acceso: sustituyen a los arreglos String[] que estaban en el código (Lec09).
-- id_rol es NULL cuando requiere_rol = 0 (ruta pública).
CREATE TABLE IF NOT EXISTS ruta (
    id_ruta            INT AUTO_INCREMENT PRIMARY KEY,
    ruta               VARCHAR(255) NOT NULL UNIQUE,
    id_rol             INT NULL,
    requiere_rol       TINYINT(1) NOT NULL DEFAULT 0,
    fecha_creacion     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ruta_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
);

-- Lec12: encabezado de la factura que se genera al procesar el carrito de compras.
CREATE TABLE IF NOT EXISTS factura (
    id_factura         INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario         INT NOT NULL,
    fecha              TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    total              DECIMAL(12,2) NULL,
    estado             ENUM('Activa','Pagada','Anulada') NOT NULL,
    fecha_creacion     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_factura_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

-- Lec12: línea de detalle de la factura. precio_historico congela el precio que
-- tenía el producto al momento de la compra.
CREATE TABLE IF NOT EXISTS venta (
    id_venta           INT AUTO_INCREMENT PRIMARY KEY,
    id_factura         INT NOT NULL,
    id_producto        INT NOT NULL,
    precio_historico   DECIMAL(12,2) NULL,
    cantidad           INT UNSIGNED NULL,
    fecha_creacion     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_venta_factura  FOREIGN KEY (id_factura)  REFERENCES factura (id_factura),
    CONSTRAINT fk_venta_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
);

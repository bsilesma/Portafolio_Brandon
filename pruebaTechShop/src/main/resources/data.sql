-- ============================================================================
-- Lec10: datos iniciales de seguridad.
-- Se usa INSERT IGNORE porque este script corre en cada arranque
-- (spring.sql.init.mode=always): si el registro ya existe, no hace nada.
-- ============================================================================

-- --- Roles -------------------------------------------------------------------
INSERT IGNORE INTO rol (id_rol, rol) VALUES
    (1, 'ADMIN'),
    (2, 'VENDEDOR'),
    (3, 'USER');

-- --- Usuarios ----------------------------------------------------------------
-- Las contraseñas están encriptadas con BCrypt (las mismas de la Lec09):
--   juan/123    rebeca/456    pedro/789
INSERT IGNORE INTO usuario (id_usuario, username, password, nombre, apellidos, correo, telefono, ruta_imagen, activo) VALUES
    (1, 'juan',   '$2a$10$t8cbuCQsCNxRomNddl/9AeMjq97qclVVkYnNtLatH9xhxq.3yjw8e', 'Juan',   'Administrador', 'juan@techshop.com',   '2222-0001', '/img/avatar.svg', 1),
    (2, 'rebeca', '$2a$10$rr/oa4p/gGBYAku9NF069.TWOPQl1IC6Q8dHqvHAIcTCWOBcx6BTG', 'Rebeca', 'Vendedora',     'rebeca@techshop.com', '2222-0002', '/img/avatar.svg', 1),
    (3, 'pedro',  '$2a$10$WAVLi1eN2E6lgUOAL3Nt.uHLHruGev4S6uyLA/kIa1X.gh1k5XOFm', 'Pedro',  'Cliente',       'pedro@techshop.com',  '2222-0003', '/img/avatar.svg', 1);

-- --- Roles de cada usuario (relación @ManyToMany) ----------------------------
-- juan es ADMIN y además VENDEDOR y USUARIO, porque la tabla ruta solo admite UN
-- rol por ruta: así el administrador conserva el "acceso total" que define la Lec09
-- sobre las rutas asignadas a VENDEDOR y a USUARIO.
INSERT IGNORE INTO usuario_rol (id_usuario, id_rol) VALUES
    (1, 1), (1, 2), (1, 3),
    (2, 2),
    (3, 3);

-- --- Rutas públicas (requiere_rol = 0) ---------------------------------------
INSERT IGNORE INTO ruta (ruta, id_rol, requiere_rol) VALUES
    ('/',                 NULL, 0),
    ('/index',            NULL, 0),
    ('/css/**',           NULL, 0),
    ('/js/**',            NULL, 0),
    ('/img/**',           NULL, 0),
    ('/webjars/**',       NULL, 0),
    ('/fav/**',           NULL, 0),
    ('/carrito/**',       NULL, 0),
    ('/consultas/**',     NULL, 0),
    ('/registro/**',      NULL, 0),
    ('/login',            NULL, 0),
    ('/acceso_denegado',  NULL, 0);

-- --- Rutas exclusivas de ADMIN (crear, modificar, eliminar) ------------------
INSERT IGNORE INTO ruta (ruta, id_rol, requiere_rol) VALUES
    ('/producto/nuevo',         1, 1),
    ('/producto/guardar',       1, 1),
    ('/producto/modificar/**',  1, 1),
    ('/producto/eliminar/**',   1, 1),
    ('/categoria/nuevo',        1, 1),
    ('/categoria/guardar',      1, 1),
    ('/categoria/modificar/**', 1, 1),
    ('/categoria/eliminar/**',  1, 1),
    ('/usuario/nuevo',          1, 1),
    ('/usuario/guardar',        1, 1),
    ('/usuario/modificar/**',   1, 1),
    ('/usuario/eliminar/**',    1, 1);

-- --- Listados: solo lectura, rol VENDEDOR (juan lo tiene también) ------------
INSERT IGNORE INTO ruta (ruta, id_rol, requiere_rol) VALUES
    ('/producto/listado',  2, 1),
    ('/categoria/listado', 2, 1),
    ('/usuario/listado',   2, 1);

-- --- Rutas del cliente final -------------------------------------------------
INSERT IGNORE INTO ruta (ruta, id_rol, requiere_rol) VALUES
    ('/facturar/carrito', 3, 1);

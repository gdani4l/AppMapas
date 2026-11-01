CREATE TABLE IF NOT EXISTS sesiones_usuario (
  id_sesion bigint unsigned NOT NULL AUTO_INCREMENT,
  id_usuario int unsigned NOT NULL,
  token_hash char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  fecha_expiracion datetime NOT NULL,
  revocada tinyint(1) NOT NULL DEFAULT 0,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_uso datetime DEFAULT NULL,
  PRIMARY KEY (id_sesion),
  UNIQUE KEY uq_sesiones_usuario_token_hash (token_hash),
  KEY idx_sesiones_usuario_usuario (id_usuario, revocada, fecha_expiracion),
  CONSTRAINT fk_sesiones_usuario_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Migración aditiva: conserva perfiles_usuario como respaldo histórico; la app usa usuarios.
SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'fecha_nacimiento') = 0, 'ALTER TABLE usuarios ADD COLUMN fecha_nacimiento date DEFAULT NULL', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

CREATE TABLE IF NOT EXISTS categorias_lugar (
  codigo varchar(20) NOT NULL,
  nombre varchar(80) NOT NULL,
  orden int NOT NULL DEFAULT 0,
  activa tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT IGNORE INTO categorias_lugar (codigo, nombre, orden, activa) VALUES
  ('HOTEL', 'Hoteles', 10, 1),
  ('RESTAURANTE', 'Restaurantes', 20, 1),
  ('PARQUE', 'Parques', 30, 1),
  ('FARMACIA', 'Farmacias', 40, 1),
  ('HOSPITAL', 'Hospitales', 50, 1),
  ('COMISARIA', 'Comisarías', 60, 1);

CREATE TABLE IF NOT EXISTS lugares (
  id_lugar bigint unsigned NOT NULL AUTO_INCREMENT,
  tipo varchar(20) NOT NULL,
  nombre varchar(120) NOT NULL,
  descripcion varchar(1000) DEFAULT NULL,
  direccion varchar(255) DEFAULT NULL,
  latitud decimal(10,7) NOT NULL,
  longitud decimal(10,7) NOT NULL,
  id_usuario_creador int unsigned NOT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_lugar),
  KEY idx_lugares_vista (latitud, longitud, tipo),
  KEY idx_lugares_nombre (nombre),
  CONSTRAINT fk_lugares_categoria FOREIGN KEY (tipo) REFERENCES categorias_lugar (codigo),
  CONSTRAINT fk_lugares_usuario FOREIGN KEY (id_usuario_creador)
    REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Para instalaciones que ya tenían la tabla lugares: agrega la relación sin tocar sus filas.
SET @categoria_ddl = IF((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'lugares' AND constraint_name = 'fk_lugares_categoria') = 0, 'ALTER TABLE lugares ADD CONSTRAINT fk_lugares_categoria FOREIGN KEY (tipo) REFERENCES categorias_lugar (codigo)', 'SELECT 1');
PREPARE categoria_stmt FROM @categoria_ddl;
EXECUTE categoria_stmt;
DEALLOCATE PREPARE categoria_stmt;

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'distrito') = 0, 'ALTER TABLE usuarios ADD COLUMN distrito varchar(100) DEFAULT NULL', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'telefono') = 0, 'ALTER TABLE usuarios ADD COLUMN telefono varchar(20) DEFAULT NULL', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'direccion') = 0, 'ALTER TABLE usuarios ADD COLUMN direccion varchar(255) DEFAULT NULL', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'imagen_perfil_url') = 0, 'ALTER TABLE usuarios ADD COLUMN imagen_perfil_url varchar(500) DEFAULT NULL', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'imagen_public_id') = 0, 'ALTER TABLE usuarios ADD COLUMN imagen_public_id varchar(255) DEFAULT NULL', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'perfiles_usuario') > 0, 'UPDATE usuarios u JOIN perfiles_usuario p ON p.id_usuario = u.id_usuario SET u.fecha_nacimiento = COALESCE(u.fecha_nacimiento, p.fecha_nacimiento), u.distrito = COALESCE(u.distrito, p.distrito), u.telefono = COALESCE(u.telefono, p.telefono), u.direccion = COALESCE(u.direccion, p.direccion), u.imagen_perfil_url = COALESCE(u.imagen_perfil_url, p.imagen_perfil_url), u.imagen_public_id = COALESCE(u.imagen_public_id, p.imagen_public_id)', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;
SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'perfiles_usuario') > 0, 'RENAME TABLE perfiles_usuario TO perfiles_usuario_respaldo', 'SELECT 1');
PREPARE perfil_stmt FROM @perfil_ddl;
EXECUTE perfil_stmt;
DEALLOCATE PREPARE perfil_stmt;

CREATE TABLE IF NOT EXISTS resenas_lugar (
  id_resena bigint unsigned NOT NULL AUTO_INCREMENT,
  id_lugar bigint unsigned NOT NULL,
  id_usuario int unsigned NOT NULL,
  estrellas int NOT NULL,
  comentario varchar(2000) DEFAULT NULL,
  imagen_url varchar(500) DEFAULT NULL,
  imagen_public_id varchar(255) DEFAULT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_resena),
  UNIQUE KEY uq_resena_lugar_usuario (id_lugar, id_usuario),
  KEY idx_resenas_lugar_fecha (id_lugar, fecha_creacion),
  CONSTRAINT fk_resena_lugar FOREIGN KEY (id_lugar) REFERENCES lugares (id_lugar) ON DELETE CASCADE,
  CONSTRAINT fk_resena_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  CONSTRAINT chk_resena_estrellas CHECK (estrellas BETWEEN 0 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

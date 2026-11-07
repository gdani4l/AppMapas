-- Las cuentas existentes verifican su correo en su siguiente inicio de sesión.
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET @auth_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'correo_verificado') = 0, 'ALTER TABLE usuarios ADD COLUMN correo_verificado tinyint(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE auth_stmt FROM @auth_ddl;
EXECUTE auth_stmt;
DEALLOCATE PREPARE auth_stmt;
SET @auth_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'activa') = 0, 'ALTER TABLE usuarios ADD COLUMN activa tinyint(1) NOT NULL DEFAULT 1', 'SELECT 1');
PREPARE auth_stmt FROM @auth_ddl;
EXECUTE auth_stmt;
DEALLOCATE PREPARE auth_stmt;

CREATE TABLE IF NOT EXISTS desafios_acceso (
 id varchar(36) NOT NULL PRIMARY KEY,
 id_usuario int unsigned NOT NULL,
 proposito varchar(20) NOT NULL,
 codigo_hash varchar(100) NOT NULL,
 correo varchar(254) NOT NULL,
 password_hash varchar(255) NOT NULL,
 creado datetime NOT NULL,
 enviado datetime NOT NULL,
 expira datetime NOT NULL,
 intentos int NOT NULL DEFAULT 0,
 envios int NOT NULL DEFAULT 1,
 usado tinyint(1) NOT NULL DEFAULT 0,
 KEY idx_desafios_usuario (id_usuario, creado),
 CONSTRAINT fk_desafio_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS auditoria_cuentas (
 id bigint unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
 id_admin int unsigned NOT NULL,
 id_usuario int unsigned NOT NULL,
 accion varchar(100) NOT NULL,
 fecha timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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

SET @perfil_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuarios' AND column_name = 'rol') = 0, 'ALTER TABLE usuarios ADD COLUMN rol varchar(20) NOT NULL DEFAULT ''USUARIO''', 'SELECT 1');
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

CREATE TABLE IF NOT EXISTS reportes_seguridad (
  id_reporte bigint unsigned NOT NULL AUTO_INCREMENT,
  id_usuario int unsigned DEFAULT NULL,
  tipo_evento varchar(40) NOT NULL,
  nivel_peligro varchar(20) NOT NULL,
  descripcion varchar(2000) NOT NULL,
  latitud decimal(10,7) NOT NULL,
  longitud decimal(10,7) NOT NULL,
  radio_metros int unsigned NOT NULL,
  anonimo tinyint(1) NOT NULL DEFAULT 0,
  estado_validacion varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  confirmaciones int unsigned NOT NULL DEFAULT 0,
  refutaciones int unsigned NOT NULL DEFAULT 0,
  distrito varchar(100) DEFAULT NULL,
  imagen_url varchar(500) DEFAULT NULL,
  imagen_public_id varchar(255) DEFAULT NULL,
  fecha_expiracion datetime NOT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_reporte),
  KEY idx_reportes_seguridad_area (latitud, longitud, radio_metros),
  KEY idx_reportes_seguridad_fecha (fecha_creacion),
  KEY idx_reportes_seguridad_estado (estado_validacion),
  CONSTRAINT fk_reportes_seguridad_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE SET NULL,
  CONSTRAINT chk_reportes_radio CHECK (radio_metros BETWEEN 10 AND 50),
  CONSTRAINT chk_reportes_nivel CHECK (nivel_peligro IN ('MEDIO', 'ALTO')),
  CONSTRAINT chk_reportes_estado CHECK (estado_validacion IN ('PENDIENTE', 'CONFIRMADO', 'REFUTADO', 'EN_REVISION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Ajusta instalaciones anteriores al nuevo alcance permitido de 10 a 50 metros.
SET @radio_check_outdated = (
  SELECT COUNT(*) FROM information_schema.check_constraints
  WHERE constraint_schema = DATABASE() AND constraint_name = 'chk_reportes_radio'
    AND LOWER(REPLACE(check_clause, '`', '')) NOT LIKE '%between 10 and 50%'
);
SET @reporte_ddl = IF(@radio_check_outdated > 0,
  'ALTER TABLE reportes_seguridad DROP CHECK chk_reportes_radio', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl;
EXECUTE reporte_stmt;
DEALLOCATE PREPARE reporte_stmt;

SET @reporte_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reportes_seguridad' AND column_name = 'distrito') = 0, 'ALTER TABLE reportes_seguridad ADD COLUMN distrito varchar(100) DEFAULT NULL', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl; EXECUTE reporte_stmt; DEALLOCATE PREPARE reporte_stmt;
SET @reporte_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reportes_seguridad' AND column_name = 'imagen_url') = 0, 'ALTER TABLE reportes_seguridad ADD COLUMN imagen_url varchar(500) DEFAULT NULL', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl; EXECUTE reporte_stmt; DEALLOCATE PREPARE reporte_stmt;
SET @reporte_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reportes_seguridad' AND column_name = 'imagen_public_id') = 0, 'ALTER TABLE reportes_seguridad ADD COLUMN imagen_public_id varchar(255) DEFAULT NULL', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl; EXECUTE reporte_stmt; DEALLOCATE PREPARE reporte_stmt;
UPDATE reportes_seguridad SET radio_metros = 50 WHERE radio_metros > 50;
UPDATE reportes_seguridad SET radio_metros = 10 WHERE radio_metros < 10;
SET @reporte_ddl = IF((SELECT COUNT(*) FROM information_schema.table_constraints
  WHERE table_schema = DATABASE() AND table_name = 'reportes_seguridad'
    AND constraint_name = 'chk_reportes_radio') = 0,
  'ALTER TABLE reportes_seguridad ADD CONSTRAINT chk_reportes_radio CHECK (radio_metros BETWEEN 10 AND 50)', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl;
EXECUTE reporte_stmt;
DEALLOCATE PREPARE reporte_stmt;

SET @reporte_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reportes_seguridad' AND column_name = 'fecha_expiracion') = 0, 'ALTER TABLE reportes_seguridad ADD COLUMN fecha_expiracion datetime DEFAULT NULL AFTER refutaciones', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl;
EXECUTE reporte_stmt;
DEALLOCATE PREPARE reporte_stmt;
UPDATE reportes_seguridad SET fecha_expiracion = DATE_ADD(fecha_creacion, INTERVAL 24 HOUR) WHERE fecha_expiracion IS NULL;
SET @reporte_ddl = IF((SELECT is_nullable FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'reportes_seguridad' AND column_name = 'fecha_expiracion') = 'YES', 'ALTER TABLE reportes_seguridad MODIFY fecha_expiracion datetime NOT NULL', 'SELECT 1');
PREPARE reporte_stmt FROM @reporte_ddl;
EXECUTE reporte_stmt;
DEALLOCATE PREPARE reporte_stmt;

CREATE TABLE IF NOT EXISTS comentarios_reporte (
  id_comentario bigint unsigned NOT NULL AUTO_INCREMENT,
  id_reporte bigint unsigned NOT NULL,
  id_usuario int unsigned NOT NULL,
  comentario varchar(1000) NOT NULL,
  imagen_url varchar(500) DEFAULT NULL,
  imagen_public_id varchar(255) DEFAULT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_comentario),
  KEY idx_comentarios_reporte_fecha (id_reporte, fecha_creacion),
  CONSTRAINT fk_comentarios_reporte FOREIGN KEY (id_reporte) REFERENCES reportes_seguridad (id_reporte) ON DELETE CASCADE,
  CONSTRAINT fk_comentarios_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET @comentario_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'comentarios_reporte' AND column_name = 'imagen_url') = 0, 'ALTER TABLE comentarios_reporte ADD COLUMN imagen_url varchar(500) DEFAULT NULL', 'SELECT 1');
PREPARE comentario_stmt FROM @comentario_ddl; EXECUTE comentario_stmt; DEALLOCATE PREPARE comentario_stmt;
SET @comentario_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'comentarios_reporte' AND column_name = 'imagen_public_id') = 0, 'ALTER TABLE comentarios_reporte ADD COLUMN imagen_public_id varchar(255) DEFAULT NULL', 'SELECT 1');
PREPARE comentario_stmt FROM @comentario_ddl; EXECUTE comentario_stmt; DEALLOCATE PREPARE comentario_stmt;

CREATE TABLE IF NOT EXISTS votos_reporte (
  id_reporte bigint unsigned NOT NULL,
  id_usuario int unsigned NOT NULL,
  veracidad varchar(10) NOT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_reporte, id_usuario),
  CONSTRAINT fk_votos_reporte FOREIGN KEY (id_reporte) REFERENCES reportes_seguridad (id_reporte) ON DELETE CASCADE,
  CONSTRAINT fk_votos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  CONSTRAINT chk_voto_veracidad CHECK (veracidad IN ('VERDADERO', 'FALSO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS reportes_contenido (
  id_reporte_contenido bigint unsigned NOT NULL AUTO_INCREMENT,
  id_usuario int unsigned NOT NULL,
  tipo_contenido varchar(20) NOT NULL,
  id_contenido bigint unsigned NOT NULL,
  motivo varchar(500) NOT NULL,
  estado varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_reporte_contenido),
  KEY idx_reportes_contenido_estado (estado, fecha_creacion),
  CONSTRAINT fk_reportes_contenido_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  CONSTRAINT chk_reportes_contenido_tipo CHECK (tipo_contenido IN ('REPORTE', 'RESENA', 'LUGAR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS notificaciones (
  id_notificacion bigint unsigned NOT NULL AUTO_INCREMENT,
  id_usuario int unsigned NOT NULL,
  tipo varchar(30) NOT NULL,
  titulo varchar(120) NOT NULL,
  mensaje varchar(500) NOT NULL,
  id_referencia bigint unsigned DEFAULT NULL,
  leida tinyint(1) NOT NULL DEFAULT 0,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_notificacion),
  UNIQUE KEY uq_notificacion_evento (id_usuario, tipo, id_referencia),
  KEY idx_notificaciones_usuario (id_usuario, leida, fecha_creacion),
  CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS puntos_recorrido (
  id_punto bigint unsigned NOT NULL AUTO_INCREMENT,
  id_usuario int unsigned NOT NULL,
  latitud decimal(10,7) NOT NULL,
  longitud decimal(10,7) NOT NULL,
  simulado tinyint(1) NOT NULL DEFAULT 0,
  fecha_registro timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_punto),
  KEY idx_recorrido_fecha (fecha_registro),
  KEY idx_recorrido_usuario_fecha (id_usuario, fecha_registro),
  CONSTRAINT fk_recorrido_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS eventos_alerta_usuario (
  id_evento bigint unsigned NOT NULL AUTO_INCREMENT,
  id_usuario int unsigned NOT NULL,
  id_reporte bigint unsigned NOT NULL,
  nivel varchar(10) NOT NULL,
  fecha_creacion timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_evento),
  UNIQUE KEY uq_alerta_usuario_reporte_nivel (id_usuario,id_reporte,nivel),
  KEY idx_alertas_fecha (fecha_creacion),
  KEY idx_alertas_reporte (id_reporte),
  CONSTRAINT fk_alerta_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_alerta_reporte FOREIGN KEY (id_reporte) REFERENCES reportes_seguridad (id_reporte) ON DELETE CASCADE,
  CONSTRAINT chk_alerta_nivel CHECK (nivel IN ('500','50','inside'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

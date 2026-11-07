-- Datos repetibles para completar los indicadores del panel de análisis.
-- Los reportes agregados son históricos y no se muestran como alertas activas en el mapa.
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
START TRANSACTION;

-- Repara importaciones anteriores realizadas desde una consola con otra codificación.
UPDATE reportes_seguridad SET distrito='San Martín de Porres' WHERE distrito LIKE 'San Mart%n de Porres';
UPDATE reportes_seguridad SET tipo_evento='Accidente de tránsito' WHERE tipo_evento LIKE 'Accidente de tr%nsito';
UPDATE reportes_seguridad SET tipo_evento='Iluminación deficiente' WHERE tipo_evento LIKE 'Iluminaci%n deficiente';
UPDATE reportes_seguridad SET tipo_evento='Obstrucción de vía' WHERE tipo_evento LIKE 'Obstrucci%n de v%a';
UPDATE comentarios_reporte SET comentario='La situación fue visible desde la avenida y se recomendó precaución.' WHERE comentario LIKE 'La situaci%n fue visible%';
UPDATE comentarios_reporte SET comentario='Vecinos de la zona confirmaron que el incidente ocurrió.' WHERE comentario LIKE 'Vecinos de la zona confirmaron%';
UPDATE comentarios_reporte SET comentario='El tránsito y el paso peatonal estuvieron afectados durante varios minutos.' WHERE comentario LIKE 'El tr%nsito y el paso peatonal%';
UPDATE comentarios_reporte SET comentario='La zona volvió a la normalidad después de la atención correspondiente.' WHERE comentario LIKE 'La zona volvi% a la normalidad%';
UPDATE comentarios_reporte SET comentario='Se compartió el aviso para prevenir a las personas cercanas.' WHERE comentario LIKE 'Se comparti% el aviso%';

DROP TEMPORARY TABLE IF EXISTS analytics_users;
CREATE TEMPORARY TABLE analytics_users AS
SELECT id_usuario, correo,
       ROW_NUMBER() OVER (ORDER BY id_usuario) numero
FROM usuarios
WHERE correo LIKE '%@mapasmail.test' AND activa = 1;
SET @analytics_user_count = (SELECT COUNT(*) FROM analytics_users);

-- Completa el distrito en reportes anteriores de las cuentas de demostración.
UPDATE reportes_seguridad r
JOIN analytics_users u ON u.id_usuario = r.id_usuario
SET r.distrito = ELT(MOD(u.numero - 1, 4) + 1,
    'Independencia', 'Los Olivos', 'San Martín de Porres', 'Comas')
WHERE r.distrito IS NULL OR TRIM(r.distrito) = '';

-- Veinticuatro sucesos históricos, concentrados en seis puntos para mostrar reincidencia.
INSERT INTO reportes_seguridad
 (id_usuario,tipo_evento,nivel_peligro,descripcion,latitud,longitud,radio_metros,
  anonimo,estado_validacion,confirmaciones,refutaciones,fecha_expiracion,
  fecha_creacion,fecha_actualizacion,distrito)
WITH RECURSIVE secuencia(numero) AS (
  SELECT 1 UNION ALL SELECT numero + 1 FROM secuencia WHERE numero < 24
), datos AS (
  SELECT s.numero, u.id_usuario,
         ELT(MOD(s.numero-1,6)+1,'Asalto','Accidente de tránsito','Robo',
             'Vandalismo','Iluminación deficiente','Obstrucción de vía') tipo,
         ELT(MOD(s.numero-1,4)+1,'Independencia','Los Olivos','San Martín de Porres','Comas') distrito,
         ELT(MOD(s.numero-1,6)+1,
             -11.9586000,-11.9612000,-11.9565000,-11.9633000,-11.9599000,-11.9549000) latitud,
         ELT(MOD(s.numero-1,6)+1,
             -77.0670000,-77.0693000,-77.0648000,-77.0715000,-77.0635000,-77.0706000) longitud,
         ELT(MOD(s.numero-1,6)+1,7,8,13,18,19,21) hora,
         ELT(MOD(s.numero-1,3)+1,'CONFIRMADO','PENDIENTE','REFUTADO') estado
  FROM secuencia s
  JOIN analytics_users u ON u.numero = MOD(s.numero-1,@analytics_user_count)+1
), fechas AS (
  SELECT d.*,
         TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL MOD(d.numero*3,30) DAY),
                   MAKETIME(d.hora,MOD(d.numero*11,60),0)) fecha_evento
  FROM datos d
)
SELECT id_usuario,tipo,IF(MOD(numero,3)=0,'MEDIO','ALTO'),
       CONCAT('Incidente registrado por la comunidad en el sector ',LPAD(numero,2,'0'),'.'),
       latitud,longitud,ELT(MOD(numero-1,3)+1,10,25,50),0,estado,
       IF(estado='CONFIRMADO',5+MOD(numero,5),1),
       IF(estado='REFUTADO',4+MOD(numero,4),0),
       DATE_ADD(fecha_evento,INTERVAL 24 HOUR),fecha_evento,fecha_evento,distrito
FROM fechas f
WHERE NOT EXISTS (
  SELECT 1 FROM reportes_seguridad r
  WHERE r.descripcion=CONCAT('Incidente registrado por la comunidad en el sector ',LPAD(f.numero,2,'0'),'.')
);

-- Distribución ampliada: Comas concentra más reportes y los asaltos son su principal incidencia.
DROP TEMPORARY TABLE IF EXISTS analytics_distribution;
CREATE TEMPORARY TABLE analytics_distribution (
  id int unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
  distrito varchar(100) NOT NULL,
  tipo varchar(40) NOT NULL,
  cantidad int unsigned NOT NULL,
  latitud decimal(10,7) NOT NULL,
  longitud decimal(10,7) NOT NULL
);
INSERT INTO analytics_distribution (distrito,tipo,cantidad,latitud,longitud) VALUES
('Comas','Asalto',18,-11.9396000,-77.0562000),
('Comas','Robo',8,-11.9396000,-77.0562000),
('Comas','Accidente de tránsito',4,-11.9396000,-77.0562000),
('Comas','Vandalismo',3,-11.9396000,-77.0562000),
('Comas','Iluminación deficiente',2,-11.9396000,-77.0562000),
('Comas','Obstrucción de vía',1,-11.9396000,-77.0562000),
('San Martín de Porres','Asalto',9,-11.9867000,-77.0901000),
('San Martín de Porres','Robo',5,-11.9867000,-77.0901000),
('San Martín de Porres','Accidente de tránsito',4,-11.9867000,-77.0901000),
('San Martín de Porres','Vandalismo',3,-11.9867000,-77.0901000),
('San Martín de Porres','Iluminación deficiente',2,-11.9867000,-77.0901000),
('San Martín de Porres','Obstrucción de vía',1,-11.9867000,-77.0901000),
('Los Olivos','Asalto',6,-11.9597000,-77.0683000),
('Los Olivos','Robo',3,-11.9597000,-77.0683000),
('Los Olivos','Accidente de tránsito',4,-11.9597000,-77.0683000),
('Los Olivos','Vandalismo',2,-11.9597000,-77.0683000),
('Los Olivos','Iluminación deficiente',2,-11.9597000,-77.0683000),
('Los Olivos','Obstrucción de vía',1,-11.9597000,-77.0683000),
('Independencia','Asalto',5,-11.9903000,-77.0548000),
('Independencia','Robo',3,-11.9903000,-77.0548000),
('Independencia','Accidente de tránsito',2,-11.9903000,-77.0548000),
('Independencia','Vandalismo',2,-11.9903000,-77.0548000),
('Independencia','Iluminación deficiente',1,-11.9903000,-77.0548000),
('Independencia','Obstrucción de vía',1,-11.9903000,-77.0548000);

INSERT INTO reportes_seguridad
 (id_usuario,tipo_evento,nivel_peligro,descripcion,latitud,longitud,radio_metros,
  anonimo,estado_validacion,confirmaciones,refutaciones,fecha_expiracion,
  fecha_creacion,fecha_actualizacion,distrito)
WITH RECURSIVE secuencia(numero) AS (
  SELECT 1 UNION ALL SELECT numero+1 FROM secuencia WHERE numero<20
), preparados AS (
  SELECT d.id,d.distrito,d.tipo,d.latitud,d.longitud,s.numero,
         TIMESTAMP(
           DATE_SUB(CURDATE(),INTERVAL MOD(d.id*5+s.numero*3,60) DAY),
           MAKETIME(CASE MOD(d.id*7+s.numero,10)
             WHEN 0 THEN 21 WHEN 1 THEN 21 WHEN 2 THEN 21 WHEN 3 THEN 21
             WHEN 4 THEN 22 WHEN 5 THEN 22 WHEN 6 THEN 20 WHEN 7 THEN 19
             WHEN 8 THEN 8 ELSE 14 END,
             MOD(d.id*13+s.numero*7,60),0)
         ) fecha_evento
  FROM analytics_distribution d JOIN secuencia s ON s.numero<=d.cantidad
)
SELECT u.id_usuario,p.tipo,IF(MOD(p.id+p.numero,4)=0,'MEDIO','ALTO'),
       ELT(MOD(p.id+p.numero,5)+1,
          'Se reportó un incidente cerca de una vía de tránsito frecuente.',
          'Vecinos comunicaron una situación de riesgo en este sector.',
          'Se recomendó transitar con precaución por esta zona.',
          'El incidente afectó temporalmente el paso peatonal y vehicular.',
          'La comunidad compartió una alerta preventiva sobre este punto.'),
       p.latitud+(MOD(p.numero-1,3)*0.00018),
       p.longitud+(MOD(p.numero-1,3)*0.00016),
       ELT(MOD(p.numero-1,3)+1,10,25,50),0,
       ELT(MOD(p.id+p.numero,5)+1,'CONFIRMADO','PENDIENTE','CONFIRMADO','REFUTADO','PENDIENTE'),
       IF(MOD(p.id+p.numero,5) IN (0,2),5+MOD(p.numero,6),1),
       IF(MOD(p.id+p.numero,5)=3,4+MOD(p.numero,4),0),
       DATE_ADD(p.fecha_evento,INTERVAL 24 HOUR),p.fecha_evento,p.fecha_evento,p.distrito
FROM preparados p
JOIN analytics_users u ON u.numero=MOD(p.id*11+p.numero-2,@analytics_user_count)+1
WHERE NOT EXISTS (
  SELECT 1 FROM reportes_seguridad r
  WHERE r.descripcion=ELT(MOD(p.id+p.numero,5)+1,
          'Se reportó un incidente cerca de una vía de tránsito frecuente.',
          'Vecinos comunicaron una situación de riesgo en este sector.',
          'Se recomendó transitar con precaución por esta zona.',
          'El incidente afectó temporalmente el paso peatonal y vehicular.',
          'La comunidad compartió una alerta preventiva sobre este punto.')
    AND r.distrito=p.distrito AND r.tipo_evento=p.tipo
    AND r.latitud=p.latitud+(MOD(p.numero-1,3)*0.00018)
    AND DATE(r.fecha_creacion)=DATE(p.fecha_evento)
);

DROP TEMPORARY TABLE IF EXISTS analytics_reports;
CREATE TEMPORARY TABLE analytics_reports AS
SELECT r.id_reporte,r.id_usuario,r.distrito,r.fecha_creacion,
       ROW_NUMBER() OVER (ORDER BY r.id_reporte) numero
FROM reportes_seguridad r
JOIN usuarios u ON u.id_usuario=r.id_usuario
WHERE u.correo LIKE '%@mapasmail.test';

-- Comentarios distribuidos por distrito y fecha.
INSERT INTO comentarios_reporte (id_reporte,id_usuario,comentario,fecha_creacion)
SELECT r.id_reporte,u.id_usuario,
       ELT(MOD(r.numero+u.numero,5)+1,
           'La situación fue visible desde la avenida y se recomendó precaución.',
           'Vecinos de la zona confirmaron que el incidente ocurrió.',
           'El tránsito y el paso peatonal estuvieron afectados durante varios minutos.',
           'La zona volvió a la normalidad después de la atención correspondiente.',
           'Se compartió el aviso para prevenir a las personas cercanas.'),
       DATE_ADD(r.fecha_creacion,INTERVAL MOD(u.numero*7,180) MINUTE)
FROM analytics_reports r
JOIN analytics_users u ON MOD(r.numero+u.numero,7)=0 AND u.id_usuario<>r.id_usuario
WHERE NOT EXISTS (
  SELECT 1 FROM comentarios_reporte c
  WHERE c.id_reporte=r.id_reporte AND c.id_usuario=u.id_usuario
);

-- Validaciones comunitarias para completar porcentajes y actividad.
INSERT INTO votos_reporte (id_reporte,id_usuario,veracidad,fecha_creacion)
SELECT r.id_reporte,u.id_usuario,
       IF(MOD(r.numero+u.numero,5)=0,'FALSO','VERDADERO'),
       DATE_ADD(r.fecha_creacion,INTERVAL MOD(u.numero*13,240) MINUTE)
FROM analytics_reports r
JOIN analytics_users u ON MOD(r.numero*2+u.numero,5)=0 AND u.id_usuario<>r.id_usuario
ON DUPLICATE KEY UPDATE veracidad=VALUES(veracidad),fecha_creacion=VALUES(fecha_creacion);

-- Alertas recibidas a 500 m, 50 m y dentro de la zona.
INSERT IGNORE INTO eventos_alerta_usuario (id_usuario,id_reporte,nivel,fecha_creacion)
SELECT u.id_usuario,r.id_reporte,'500',DATE_ADD(r.fecha_creacion,INTERVAL 5 MINUTE)
FROM analytics_reports r JOIN analytics_users u ON MOD(r.numero+u.numero,4)=0;

INSERT IGNORE INTO eventos_alerta_usuario (id_usuario,id_reporte,nivel,fecha_creacion)
SELECT u.id_usuario,r.id_reporte,'50',DATE_ADD(r.fecha_creacion,INTERVAL 12 MINUTE)
FROM analytics_reports r JOIN analytics_users u ON MOD(r.numero+u.numero,7)=0;

INSERT IGNORE INTO eventos_alerta_usuario (id_usuario,id_reporte,nivel,fecha_creacion)
SELECT u.id_usuario,r.id_reporte,'inside',DATE_ADD(r.fecha_creacion,INTERVAL 18 MINUTE)
FROM analytics_reports r JOIN analytics_users u ON MOD(r.numero+u.numero,11)=0;

-- Uso histórico de lugares. Se reemplaza al ejecutar el script para no duplicar datos.
DELETE FROM eventos_uso_lugar WHERE origen='DEMO';
DROP TEMPORARY TABLE IF EXISTS analytics_place_usage;
CREATE TEMPORARY TABLE analytics_place_usage (
  nombre varchar(120) PRIMARY KEY,
  vistas int unsigned NOT NULL,
  busquedas int unsigned NOT NULL
);
INSERT INTO analytics_place_usage (nombre,vistas,busquedas) VALUES
('Sabor Norteño',96,47),
('Hotel Alameda Norte',82,34),
('Café Plaza Central',74,41),
('Parque Los Cedros',61,22),
('Farmacia Vida Norte',55,18),
('Cevichería Puerto Azul',48,29),
('Pollería Brasa Norte',43,25),
('Parque Santa Rosa',36,12),
('Clínica San Gabriel Norte',31,16),
('Hostal Mirador Verde',25,9);

INSERT INTO eventos_uso_lugar
  (tipo_evento,id_lugar,nombre_lugar,categoria,origen,fecha_creacion)
WITH RECURSIVE secuencia(numero) AS (
  SELECT 1 UNION ALL SELECT numero+1 FROM secuencia WHERE numero<100
)
SELECT 'VISTA',l.id_lugar,l.nombre,l.tipo,'DEMO',
       DATE_SUB(NOW(),INTERVAL MOD(s.numero*3+l.id_lugar,45) DAY)
FROM analytics_place_usage d JOIN lugares l ON l.nombre=d.nombre
JOIN secuencia s ON s.numero<=d.vistas;

INSERT INTO eventos_uso_lugar
  (tipo_evento,id_lugar,nombre_lugar,categoria,origen,fecha_creacion)
WITH RECURSIVE secuencia(numero) AS (
  SELECT 1 UNION ALL SELECT numero+1 FROM secuencia WHERE numero<50
)
SELECT 'SELECCION_BUSQUEDA',l.id_lugar,l.nombre,l.tipo,'DEMO',
       DATE_SUB(NOW(),INTERVAL MOD(s.numero*5+l.id_lugar,45) DAY)
FROM analytics_place_usage d JOIN lugares l ON l.nombre=d.nombre
JOIN secuencia s ON s.numero<=d.busquedas;

COMMIT;

SELECT 'reportes_historicos' indicador,COUNT(*) valor
FROM reportes_seguridad r JOIN usuarios u ON u.id_usuario=r.id_usuario
WHERE u.correo LIKE '%@mapasmail.test'
UNION ALL SELECT 'comentarios',COUNT(*) FROM comentarios_reporte
UNION ALL SELECT 'validaciones',COUNT(*) FROM votos_reporte
UNION ALL SELECT 'eventos_alerta',COUNT(*) FROM eventos_alerta_usuario
UNION ALL SELECT 'eventos_uso_lugares',COUNT(*) FROM eventos_uso_lugar
UNION ALL SELECT 'distritos_con_reportes',COUNT(DISTINCT distrito)
FROM reportes_seguridad WHERE distrito IS NOT NULL;

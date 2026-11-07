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

COMMIT;

SELECT 'reportes_historicos' indicador,COUNT(*) valor
FROM reportes_seguridad r JOIN usuarios u ON u.id_usuario=r.id_usuario
WHERE u.correo LIKE '%@mapasmail.test'
UNION ALL SELECT 'comentarios',COUNT(*) FROM comentarios_reporte
UNION ALL SELECT 'validaciones',COUNT(*) FROM votos_reporte
UNION ALL SELECT 'eventos_alerta',COUNT(*) FROM eventos_alerta_usuario
UNION ALL SELECT 'distritos_con_reportes',COUNT(DISTINCT distrito)
FROM reportes_seguridad WHERE distrito IS NOT NULL;

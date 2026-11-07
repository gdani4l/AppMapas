-- Datos de demostración repetibles para paneles, mapa y análisis.
-- Área principal: -11.9597289, -77.0683366 (aprox. 1 km de radio).
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
START TRANSACTION;

INSERT IGNORE INTO usuarios
  (primer_nombre, segundo_nombre, apellido_paterno, apellido_materno, correo,
   password_hash, fecha_registro, fecha_nacimiento, distrito, telefono, direccion,
   rol, correo_verificado, activa)
VALUES
('Camila',NULL,'Mendoza','Ruiz','camila.mendoza@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 29 DAY,'1998-04-12','Independencia','900100101','Av. Túpac Amaru 1040','USUARIO',1,1),
('Luis','Alberto','Paredes','Salazar','luis.paredes@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 28 DAY,'1994-09-23','Los Olivos','900100102','Jr. Los Pinos 312','USUARIO',1,1),
('Andrea','Lucía','Huamán','Torres','andrea.huaman@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 27 DAY,'2001-01-18','Comas','900100103','Av. Universitaria 1842','USUARIO',1,1),
('Miguel','Ángel','Rojas','Cabrera','miguel.rojas@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 25 DAY,'1990-06-07','San Martín de Porres','900100104','Jr. Las Gardenias 520','USUARIO',1,1),
('Valeria',NULL,'Quispe','Flores','valeria.quispe@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 24 DAY,'1997-11-30','Independencia','900100105','Calle Los Claveles 175','USUARIO',1,1),
('Diego','Martín','Castro','Vega','diego.castro@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 22 DAY,'1996-02-14','Los Olivos','900100106','Av. Carlos Izaguirre 681','USUARIO',1,1),
('Lucía','Fernanda','Navarro','Peña','lucia.navarro@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 21 DAY,'1999-08-19','Comas','900100107','Jr. Santa Rosa 224','USUARIO',1,1),
('Renato',NULL,'Aguilar','Soto','renato.aguilar@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 20 DAY,'1992-12-05','Independencia','900100108','Av. Los Alisos 901','USUARIO',1,1),
('Mariana','Isabel','Campos','León','mariana.campos@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 18 DAY,'2000-05-28','Los Olivos','900100109','Calle Las Dalias 433','USUARIO',1,1),
('Carlos','Eduardo','Medina','Espinoza','carlos.medina@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 17 DAY,'1988-03-09','San Martín de Porres','900100110','Jr. Los Robles 730','USUARIO',1,1),
('Daniela',NULL,'Vargas','Silva','daniela.vargas@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 16 DAY,'1995-10-22','Independencia','900100111','Av. El Pacífico 118','USUARIO',1,1),
('Jorge','Luis','Ramos','Castillo','jorge.ramos@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 15 DAY,'1991-07-11','Comas','900100112','Calle Los Sauces 394','USUARIO',1,1),
('Fernanda','Rocío','Ortiz','Guerra','fernanda.ortiz@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 13 DAY,'2002-03-17','Los Olivos','900100113','Jr. Las Magnolias 808','USUARIO',1,1),
('Sebastián',NULL,'Chávez','Luna','sebastian.chavez@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 12 DAY,'1998-12-02','Independencia','900100114','Av. Tomás Valle 1520','USUARIO',1,1),
('Paola','Andrea','Reyes','Mendoza','paola.reyes@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 11 DAY,'1993-06-26','San Martín de Porres','900100115','Jr. Los Nogales 649','USUARIO',1,1),
('Alonso','Javier','Gutiérrez','Prado','alonso.gutierrez@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 10 DAY,'1997-01-08','Los Olivos','900100116','Calle Las Acacias 277','USUARIO',1,1),
('Natalia','Sofía','Salas','Romero','natalia.salas@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 9 DAY,'2000-09-15','Comas','900100117','Av. Metropolitana 415','USUARIO',1,1),
('Bruno',NULL,'Villanueva','Cruz','bruno.villanueva@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 8 DAY,'1994-04-03','Independencia','900100118','Jr. Los Jazmines 960','USUARIO',1,1),
('Fiorella','María','Palomino','Díaz','fiorella.palomino@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 7 DAY,'1999-02-21','Los Olivos','900100119','Calle Los Tulipanes 140','USUARIO',1,1),
('Martín','Alejandro','Cárdenas','Ríos','martin.cardenas@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 6 DAY,'1992-08-12','San Martín de Porres','900100120','Av. Perú 3240','USUARIO',1,1),
('Alejandra',NULL,'Tello','Fuentes','alejandra.tello@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 5 DAY,'2001-05-10','Independencia','900100121','Jr. Los Eucaliptos 506','USUARIO',1,1),
('Eduardo','José','Vílchez','Ponce','eduardo.vilchez@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 4 DAY,'1989-11-25','Comas','900100122','Av. Trapiche 1090','USUARIO',1,1),
('Gabriela','Elena','Espino','Valdez','gabriela.espino@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 3 DAY,'1996-07-04','Los Olivos','900100123','Calle Los Laureles 719','USUARIO',0,1),
('Raúl','Enrique','Benavides','Arias','raul.benavides@mapasmail.test','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO',NOW()-INTERVAL 2 DAY,'1990-10-16','Independencia','900100124','Av. Industrial 288','USUARIO',0,0);

DROP TEMPORARY TABLE IF EXISTS seed_places;
CREATE TEMPORARY TABLE seed_places (
  correo varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci, tipo varchar(20), nombre varchar(120), descripcion varchar(1000),
  direccion varchar(255), latitud decimal(10,7), longitud decimal(10,7), dias int
);
INSERT INTO seed_places VALUES
('camila.mendoza@mapasmail.test','HOTEL','Hotel Alameda Norte','Alojamiento urbano con recepción permanente y habitaciones familiares.','Av. Túpac Amaru 1125',-11.9569000,-77.0678000,20),
('luis.paredes@mapasmail.test','HOTEL','Hostal Mirador Verde','Hospedaje cercano a comercios y transporte público.','Jr. Los Pinos 428',-11.9628000,-77.0701000,18),
('andrea.huaman@mapasmail.test','HOTEL','Hospedaje Las Palmeras','Habitaciones sencillas y zona de descanso.','Calle Las Palmeras 205',-11.9583000,-77.0734000,15),
('miguel.rojas@mapasmail.test','RESTAURANTE','Sabor Norteño','Menú criollo, platos a la carta y atención durante todo el día.','Av. Industrial 315',-11.9605000,-77.0660000,19),
('valeria.quispe@mapasmail.test','RESTAURANTE','Cevichería Puerto Azul','Pescados, mariscos y menú del día.','Jr. Santa Rosa 188',-11.9558000,-77.0700000,17),
('diego.castro@mapasmail.test','RESTAURANTE','Café Plaza Central','Café, desayunos y opciones para llevar.','Calle Las Dalias 512',-11.9618000,-77.0647000,14),
('lucia.navarro@mapasmail.test','RESTAURANTE','Pollería Brasa Norte','Pollos a la brasa y platos familiares.','Av. Los Alisos 745',-11.9641000,-77.0672000,12),
('renato.aguilar@mapasmail.test','PARQUE','Parque Los Cedros','Área verde con juegos, bancas y espacio para caminar.','Jr. Los Cedros cdra. 4',-11.9570000,-77.0650000,22),
('mariana.campos@mapasmail.test','PARQUE','Parque Santa Rosa','Zona recreativa con losa deportiva y áreas de descanso.','Calle Santa Rosa cdra. 6',-11.9630000,-77.0687000,16),
('carlos.medina@mapasmail.test','PARQUE','Plaza Las Gardenias','Plaza vecinal iluminada con jardines y juegos infantiles.','Jr. Las Gardenias 610',-11.9549000,-77.0718000,11),
('daniela.vargas@mapasmail.test','FARMACIA','Farmacia Vida Norte','Medicamentos, productos de cuidado personal y orientación básica.','Av. El Pacífico 240',-11.9590000,-77.0713000,13),
('jorge.ramos@mapasmail.test','FARMACIA','Botica Salud Familiar','Atención diaria y productos de primeros auxilios.','Calle Los Sauces 450',-11.9560000,-77.0645000,10),
('fernanda.ortiz@mapasmail.test','FARMACIA','Botica Bienestar','Medicamentos genéricos y productos para el hogar.','Av. Los Alisos 880',-11.9645000,-77.0710000,8),
('sebastian.chavez@mapasmail.test','HOSPITAL','Clínica San Gabriel Norte','Consulta ambulatoria, laboratorio y atención de urgencias.','Av. Tomás Valle 1580',-11.9625000,-77.0661000,21),
('paola.reyes@mapasmail.test','HOSPITAL','Centro Médico Los Olivos','Consultorios de medicina general y especialidades.','Jr. Los Nogales 702',-11.9549000,-77.0670000,9),
('alonso.gutierrez@mapasmail.test','HOSPITAL','Policlínico Santa Elena','Atención ambulatoria, farmacia y toma de muestras.','Calle Las Acacias 330',-11.9581000,-77.0627000,7),
('natalia.salas@mapasmail.test','COMISARIA','Puesto de Auxilio Comunitario Norte','Punto de orientación y apoyo para emergencias vecinales.','Av. Metropolitana 480',-11.9585000,-77.0625000,6),
('bruno.villanueva@mapasmail.test','COMISARIA','Centro de Seguridad Vecinal','Atención de incidencias y coordinación con serenazgo.','Jr. Los Jazmines 1010',-11.9650000,-77.0693000,5);

INSERT INTO lugares (tipo,nombre,descripcion,direccion,latitud,longitud,id_usuario_creador,fecha_creacion)
SELECT sp.tipo,sp.nombre,sp.descripcion,sp.direccion,sp.latitud,sp.longitud,u.id_usuario,NOW()-INTERVAL sp.dias DAY
FROM seed_places sp JOIN usuarios u ON u.correo=sp.correo
WHERE NOT EXISTS (SELECT 1 FROM lugares l WHERE l.nombre=sp.nombre AND l.id_usuario_creador=u.id_usuario);

DROP TEMPORARY TABLE IF EXISTS seed_reviews;
CREATE TEMPORARY TABLE seed_reviews (correo varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci, lugar varchar(120), estrellas int, comentario varchar(500), dias int);
INSERT INTO seed_reviews VALUES
('luis.paredes@mapasmail.test','Hotel Alameda Norte',5,'Habitación limpia y atención amable.',18),
('andrea.huaman@mapasmail.test','Hotel Alameda Norte',4,'Buena ubicación y ambiente tranquilo.',14),
('valeria.quispe@mapasmail.test','Hostal Mirador Verde',4,'El personal resolvió nuestras consultas rápidamente.',12),
('diego.castro@mapasmail.test','Hostal Mirador Verde',3,'Correcto para una estadía corta.',8),
('lucia.navarro@mapasmail.test','Hospedaje Las Palmeras',4,'Cómodo y ordenado.',7),
('renato.aguilar@mapasmail.test','Sabor Norteño',5,'El menú estuvo bien servido y llegó rápido.',16),
('mariana.campos@mapasmail.test','Sabor Norteño',4,'Buena sazón y precios razonables.',10),
('carlos.medina@mapasmail.test','Sabor Norteño',3,'La comida estuvo bien, aunque hubo espera.',5),
('daniela.vargas@mapasmail.test','Cevichería Puerto Azul',5,'Pescado fresco y porciones generosas.',15),
('jorge.ramos@mapasmail.test','Cevichería Puerto Azul',4,'Buena atención durante el almuerzo.',9),
('fernanda.ortiz@mapasmail.test','Café Plaza Central',5,'Buen café y un espacio agradable.',11),
('sebastian.chavez@mapasmail.test','Café Plaza Central',4,'Los desayunos son variados.',6),
('paola.reyes@mapasmail.test','Pollería Brasa Norte',4,'Buen sabor y atención rápida.',9),
('alonso.gutierrez@mapasmail.test','Pollería Brasa Norte',3,'El local estaba concurrido pero atendieron bien.',4),
('natalia.salas@mapasmail.test','Parque Los Cedros',5,'Buen espacio para caminar por la tarde.',20),
('bruno.villanueva@mapasmail.test','Parque Los Cedros',4,'Se mantiene limpio y tiene varias bancas.',13),
('fiorella.palomino@mapasmail.test','Parque Los Cedros',4,'Los juegos están en buen estado.',3),
('martin.cardenas@mapasmail.test','Parque Santa Rosa',4,'La losa deportiva tiene buena iluminación.',12),
('alejandra.tello@mapasmail.test','Parque Santa Rosa',3,'Faltan más tachos, pero el espacio es amplio.',5),
('eduardo.vilchez@mapasmail.test','Plaza Las Gardenias',5,'Ambiente familiar y tranquilo.',7),
('gabriela.espino@mapasmail.test','Plaza Las Gardenias',4,'Los jardines están bien cuidados.',2),
('camila.mendoza@mapasmail.test','Farmacia Vida Norte',5,'Encontré lo que necesitaba y me orientaron bien.',11),
('miguel.rojas@mapasmail.test','Farmacia Vida Norte',4,'Atención rápida.',6),
('valeria.quispe@mapasmail.test','Botica Salud Familiar',4,'Tiene buen surtido de productos.',8),
('diego.castro@mapasmail.test','Botica Salud Familiar',3,'Servicio correcto.',3),
('lucia.navarro@mapasmail.test','Botica Bienestar',5,'Buena atención y precios claros.',5),
('renato.aguilar@mapasmail.test','Clínica San Gabriel Norte',4,'La consulta empezó a la hora indicada.',14),
('mariana.campos@mapasmail.test','Clínica San Gabriel Norte',3,'La atención fue buena, pero el registro demoró.',7),
('carlos.medina@mapasmail.test','Centro Médico Los Olivos',5,'El personal fue atento y explicó el tratamiento.',10),
('daniela.vargas@mapasmail.test','Centro Médico Los Olivos',4,'Instalaciones ordenadas.',4),
('jorge.ramos@mapasmail.test','Policlínico Santa Elena',4,'Conseguí una cita con rapidez.',6),
('fernanda.ortiz@mapasmail.test','Puesto de Auxilio Comunitario Norte',5,'Brindaron orientación clara ante una consulta.',5),
('sebastian.chavez@mapasmail.test','Centro de Seguridad Vecinal',4,'Respondieron con rapidez al aviso.',3),
('paola.reyes@mapasmail.test','Hotel Alameda Norte',4,'Buena experiencia general.',2),
('alonso.gutierrez@mapasmail.test','Sabor Norteño',5,'Volvería por el menú criollo.',2),
('natalia.salas@mapasmail.test','Cevichería Puerto Azul',3,'El sabor estuvo bien, aunque el local estaba lleno.',1),
('bruno.villanueva@mapasmail.test','Café Plaza Central',4,'Servicio cordial y precios visibles.',1),
('fiorella.palomino@mapasmail.test','Parque Santa Rosa',5,'Buen lugar para hacer deporte.',1),
('martin.cardenas@mapasmail.test','Farmacia Vida Norte',4,'Atención eficiente.',1),
('alejandra.tello@mapasmail.test','Clínica San Gabriel Norte',5,'Buena orientación en recepción.',1),
('eduardo.vilchez@mapasmail.test','Hostal Mirador Verde',2,'La habitación podría tener mejor ventilación.',1),
('gabriela.espino@mapasmail.test','Pollería Brasa Norte',5,'La porción familiar fue suficiente.',1);

INSERT IGNORE INTO resenas_lugar (id_lugar,id_usuario,estrellas,comentario,fecha_creacion,fecha_actualizacion)
SELECT l.id_lugar,u.id_usuario,sr.estrellas,sr.comentario,NOW()-INTERVAL sr.dias DAY,NOW()-INTERVAL sr.dias DAY
FROM seed_reviews sr JOIN usuarios u ON u.correo=sr.correo JOIN lugares l ON l.nombre=sr.lugar;

DROP TEMPORARY TABLE IF EXISTS seed_reports;
CREATE TEMPORARY TABLE seed_reports
(correo varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci, tipo varchar(40), nivel varchar(20), descripcion varchar(500), lat decimal(10,7), lon decimal(10,7), radio int, estado varchar(20), confirma int, refuta int, horas int);
INSERT INTO seed_reports VALUES
('camila.mendoza@mapasmail.test','Accidente de tránsito','ALTO','Choque vehicular con tránsito lento en la avenida.',-11.9589000,-77.0671000,25,'CONFIRMADO',8,1,3),
('luis.paredes@mapasmail.test','Asalto','ALTO','Vecinos reportaron un asalto cerca del cruce.',-11.9611000,-77.0694000,50,'EN_REVISION',4,2,5),
('andrea.huaman@mapasmail.test','Vandalismo','MEDIO','Daños en mobiliario urbano durante la noche.',-11.9567000,-77.0649000,25,'CONFIRMADO',6,1,7),
('miguel.rojas@mapasmail.test','Iluminación deficiente','MEDIO','Tramo con luminarias apagadas y poca visibilidad.',-11.9635000,-77.0716000,50,'PENDIENTE',2,0,9),
('valeria.quispe@mapasmail.test','Accidente de tránsito','MEDIO','Vehículo detenido ocupando parte de la vía.',-11.9554000,-77.0682000,10,'REFUTADO',1,5,11),
('diego.castro@mapasmail.test','Robo','ALTO','Aviso de robo de pertenencias en los alrededores.',-11.9600000,-77.0634000,25,'EN_REVISION',3,2,13),
('lucia.navarro@mapasmail.test','Vandalismo','MEDIO','Pintas recientes en la zona recreativa.',-11.9642000,-77.0678000,10,'PENDIENTE',1,0,15),
('renato.aguilar@mapasmail.test','Asalto','ALTO','Se recomienda precaución al transitar por este tramo.',-11.9575000,-77.0727000,50,'CONFIRMADO',10,1,16),
('mariana.campos@mapasmail.test','Accidente de tránsito','ALTO','Incidente entre motocicleta y automóvil.',-11.9621000,-77.0651000,25,'CONFIRMADO',7,1,18),
('carlos.medina@mapasmail.test','Iluminación deficiente','MEDIO','Dos cuadras con iluminación intermitente.',-11.9548000,-77.0707000,50,'PENDIENTE',2,1,19),
('daniela.vargas@mapasmail.test','Robo','ALTO','Reporte comunitario de objetos sustraídos.',-11.9593000,-77.0733000,25,'EN_REVISION',5,3,20),
('jorge.ramos@mapasmail.test','Obstrucción de vía','MEDIO','Materiales ocupan parte de la calzada.',-11.9651000,-77.0646000,10,'REFUTADO',1,6,22);

INSERT INTO reportes_seguridad
 (id_usuario,tipo_evento,nivel_peligro,descripcion,latitud,longitud,radio_metros,anonimo,estado_validacion,confirmaciones,refutaciones,fecha_expiracion,fecha_creacion,fecha_actualizacion)
SELECT u.id_usuario,sr.tipo,sr.nivel,sr.descripcion,sr.lat,sr.lon,sr.radio,0,sr.estado,sr.confirma,sr.refuta,
       DATE_ADD(DATE_SUB(NOW(),INTERVAL sr.horas HOUR),INTERVAL 24 HOUR),
       DATE_SUB(NOW(),INTERVAL sr.horas HOUR),DATE_SUB(NOW(),INTERVAL sr.horas HOUR)
FROM seed_reports sr JOIN usuarios u ON u.correo=sr.correo
WHERE NOT EXISTS (SELECT 1 FROM reportes_seguridad r WHERE r.id_usuario=u.id_usuario AND r.descripcion=sr.descripcion);

-- Sesiones recientes para medir usuarios activos; los hashes no corresponden a tokens utilizables.
DELETE s FROM sesiones_usuario s JOIN usuarios u ON u.id_usuario=s.id_usuario WHERE u.correo LIKE '%@mapasmail.test';
INSERT INTO sesiones_usuario (id_usuario,token_hash,fecha_expiracion,revocada,fecha_creacion,ultimo_uso)
SELECT u.id_usuario,SHA2(CONCAT('panel-seed:',u.correo),256),NOW()+INTERVAL 30 DAY,0,
       u.fecha_registro,NOW()-INTERVAL MOD(u.id_usuario,9) DAY
FROM usuarios u WHERE u.correo LIKE '%@mapasmail.test' AND u.activa=1 AND u.correo_verificado=1;

-- Recorridos distribuidos en varios días y agrupaciones geográficas.
DELETE p FROM puntos_recorrido p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.correo LIKE '%@mapasmail.test';
INSERT INTO puntos_recorrido (id_usuario,latitud,longitud,simulado,fecha_registro)
WITH RECURSIVE seq(n) AS (SELECT 1 UNION ALL SELECT n+1 FROM seq WHERE n<480)
SELECT u.id_usuario,
       -11.9597289 + 0.0048*SIN(seq.n*0.73) + 0.0008*SIN(MOD(seq.n,7)),
       -77.0683366 + 0.0052*COS(seq.n*0.61) + 0.0007*COS(MOD(seq.n,5)),
       IF(MOD(seq.n,4)=0,1,0),
       DATE_SUB(DATE_SUB(NOW(),INTERVAL MOD(seq.n,30) DAY),INTERVAL MOD(seq.n*7,720) MINUTE)
FROM seq JOIN usuarios u ON u.correo=(ELT(MOD(seq.n-1,24)+1,
 'camila.mendoza@mapasmail.test','luis.paredes@mapasmail.test','andrea.huaman@mapasmail.test','miguel.rojas@mapasmail.test',
 'valeria.quispe@mapasmail.test','diego.castro@mapasmail.test','lucia.navarro@mapasmail.test','renato.aguilar@mapasmail.test',
 'mariana.campos@mapasmail.test','carlos.medina@mapasmail.test','daniela.vargas@mapasmail.test','jorge.ramos@mapasmail.test',
 'fernanda.ortiz@mapasmail.test','sebastian.chavez@mapasmail.test','paola.reyes@mapasmail.test','alonso.gutierrez@mapasmail.test',
 'natalia.salas@mapasmail.test','bruno.villanueva@mapasmail.test','fiorella.palomino@mapasmail.test','martin.cardenas@mapasmail.test',
 'alejandra.tello@mapasmail.test','eduardo.vilchez@mapasmail.test','gabriela.espino@mapasmail.test','raul.benavides@mapasmail.test') COLLATE utf8mb4_0900_as_ci);

COMMIT;

SELECT 'usuarios_agregados' indicador, COUNT(*) valor FROM usuarios WHERE correo LIKE '%@mapasmail.test'
UNION ALL SELECT 'lugares_area',COUNT(*) FROM lugares WHERE latitud BETWEEN -11.9688 AND -11.9507 AND longitud BETWEEN -77.0776 AND -77.0591
UNION ALL SELECT 'resenas_agregadas',COUNT(*) FROM resenas_lugar r JOIN usuarios u ON u.id_usuario=r.id_usuario WHERE u.correo LIKE '%@mapasmail.test'
UNION ALL SELECT 'reportes_agregados',COUNT(*) FROM reportes_seguridad r JOIN usuarios u ON u.id_usuario=r.id_usuario WHERE u.correo LIKE '%@mapasmail.test'
UNION ALL SELECT 'puntos_recorrido',COUNT(*) FROM puntos_recorrido p JOIN usuarios u ON u.id_usuario=p.id_usuario WHERE u.correo LIKE '%@mapasmail.test';

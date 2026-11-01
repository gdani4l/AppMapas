-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: app_mapas
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categorias_lugar`
--

DROP TABLE IF EXISTS `categorias_lugar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias_lugar` (
  `codigo` varchar(20) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `orden` int NOT NULL DEFAULT '0',
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias_lugar`
--

LOCK TABLES `categorias_lugar` WRITE;
/*!40000 ALTER TABLE `categorias_lugar` DISABLE KEYS */;
INSERT INTO `categorias_lugar` VALUES ('COMISARIA','Comisarías',60,1),('FARMACIA','Farmacias',40,1),('HOSPITAL','Hospitales',50,1),('HOTEL','Hoteles',10,1),('PARQUE','Parques',30,1),('RESTAURANTE','Restaurantes',20,1);
/*!40000 ALTER TABLE `categorias_lugar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `codigos_recuperacion`
--

DROP TABLE IF EXISTS `codigos_recuperacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `codigos_recuperacion` (
  `id_codigo_recuperacion` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `codigo_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `fecha_expiracion` datetime NOT NULL,
  `utilizado` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_codigo_recuperacion`),
  KEY `idx_codigo_recuperacion_usuario` (`id_usuario`,`utilizado`,`fecha_expiracion`),
  CONSTRAINT `fk_codigos_recuperacion_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `codigos_recuperacion`
--

LOCK TABLES `codigos_recuperacion` WRITE;
/*!40000 ALTER TABLE `codigos_recuperacion` DISABLE KEYS */;
INSERT INTO `codigos_recuperacion` VALUES (1,1,'e661baa9caaefbfcb3beb2726d685e370d78c0a9cc0f6add97d5b8544e442e06','2026-10-16 15:45:40',1,'2026-10-16 20:30:39'),(2,2,'be5f2a327016d1aa01ddd2a9bcc1d9f668ed027c3e536a4784df0428e885438c','2026-10-16 16:22:11',1,'2026-10-16 21:07:11');
/*!40000 ALTER TABLE `codigos_recuperacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lugares`
--

DROP TABLE IF EXISTS `lugares`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lugares` (
  `id_lugar` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tipo` varchar(20) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `descripcion` varchar(1000) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL,
  `id_usuario_creador` int unsigned NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_lugar`),
  KEY `idx_lugares_vista` (`latitud`,`longitud`,`tipo`),
  KEY `idx_lugares_nombre` (`nombre`),
  KEY `fk_lugares_usuario` (`id_usuario_creador`),
  KEY `fk_lugares_categoria` (`tipo`),
  CONSTRAINT `fk_lugares_categoria` FOREIGN KEY (`tipo`) REFERENCES `categorias_lugar` (`codigo`),
  CONSTRAINT `fk_lugares_usuario` FOREIGN KEY (`id_usuario_creador`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lugares`
--

LOCK TABLES `lugares` WRITE;
/*!40000 ALTER TABLE `lugares` DISABLE KEYS */;
INSERT INTO `lugares` VALUES (4,'HOTEL','El Cielo','Cuartos baratos','av. washingtong',-11.9587935,-77.0685040,1,'2026-10-18 03:42:53'),(7,'FARMACIA','InkaFarma','Vendo pastillas','ese',-11.9560480,-77.0644450,1,'2026-10-18 04:21:57'),(9,'HOSPITAL','Hospital Vergara','Atendemos a todos','idk',-11.9570143,-77.0626747,1,'2026-10-18 04:53:03'),(11,'FARMACIA','Mifarma','Parque verde','avenida',-11.9582092,-77.0625340,1,'2026-10-19 04:51:39'),(14,'HOTEL','Vhh','Vvc','hhh',-11.9588235,-77.0639157,1,'2026-10-19 05:16:12');
/*!40000 ALTER TABLE `lugares` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `perfiles_usuario_respaldo`
--

DROP TABLE IF EXISTS `perfiles_usuario_respaldo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perfiles_usuario_respaldo` (
  `id_usuario` int unsigned NOT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `imagen_perfil_url` varchar(500) DEFAULT NULL,
  `imagen_public_id` varchar(255) CHARACTER SET ascii COLLATE ascii_bin DEFAULT NULL,
  `fecha_actualizacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_perfiles_usuario_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `perfiles_usuario_respaldo`
--

LOCK TABLES `perfiles_usuario_respaldo` WRITE;
/*!40000 ALTER TABLE `perfiles_usuario_respaldo` DISABLE KEYS */;
INSERT INTO `perfiles_usuario_respaldo` VALUES (1,NULL,NULL,NULL,NULL,'https://res.cloudinary.com/dindulij3/image/upload/v1792220258/app_mapas/perfiles/usuario_1.jpg','app_mapas/perfiles/usuario_1','2026-10-17 06:58:33');
/*!40000 ALTER TABLE `perfiles_usuario_respaldo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resenas_lugar`
--

DROP TABLE IF EXISTS `resenas_lugar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resenas_lugar` (
  `id_resena` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_lugar` bigint unsigned NOT NULL,
  `id_usuario` int unsigned NOT NULL,
  `estrellas` int NOT NULL,
  `comentario` varchar(2000) DEFAULT NULL,
  `imagen_url` varchar(500) DEFAULT NULL,
  `imagen_public_id` varchar(255) DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_actualizacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_resena`),
  UNIQUE KEY `uq_resena_lugar_usuario` (`id_lugar`,`id_usuario`),
  KEY `idx_resenas_lugar_fecha` (`id_lugar`,`fecha_creacion`),
  KEY `fk_resena_usuario` (`id_usuario`),
  CONSTRAINT `fk_resena_lugar` FOREIGN KEY (`id_lugar`) REFERENCES `lugares` (`id_lugar`) ON DELETE CASCADE,
  CONSTRAINT `fk_resena_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE,
  CONSTRAINT `chk_resena_estrellas` CHECK ((`estrellas` between 0 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resenas_lugar`
--

LOCK TABLES `resenas_lugar` WRITE;
/*!40000 ALTER TABLE `resenas_lugar` DISABLE KEYS */;
INSERT INTO `resenas_lugar` VALUES (4,9,1,5,'Muy buena atención','https://res.cloudinary.com/dindulij3/image/upload/v1792384718/app_mapas/resenas/a2642d14-a901-4be4-a937-d83a1e7db480.jpg','app_mapas/resenas/a2642d14-a901-4be4-a937-d83a1e7db480','2026-10-19 04:39:38','2026-10-19 04:39:38'),(5,9,3,4,'Buena2','https://res.cloudinary.com/dindulij3/image/upload/v1792387006/app_mapas/resenas/412d97d8-8ff8-45f0-88d1-a33e6baf4939.jpg','app_mapas/resenas/412d97d8-8ff8-45f0-88d1-a33e6baf4939','2026-10-19 04:42:45','2026-10-19 05:17:45'),(8,14,1,4,NULL,NULL,NULL,'2026-10-19 05:16:30','2026-10-19 05:16:30');
/*!40000 ALTER TABLE `resenas_lugar` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sesiones_usuario`
--

DROP TABLE IF EXISTS `sesiones_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sesiones_usuario` (
  `id_sesion` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `token_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `fecha_expiracion` datetime NOT NULL,
  `revocada` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ultimo_uso` datetime DEFAULT NULL,
  PRIMARY KEY (`id_sesion`),
  UNIQUE KEY `uq_sesiones_usuario_token_hash` (`token_hash`),
  KEY `idx_sesiones_usuario_usuario` (`id_usuario`,`revocada`,`fecha_expiracion`),
  CONSTRAINT `fk_sesiones_usuario_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sesiones_usuario`
--

LOCK TABLES `sesiones_usuario` WRITE;
/*!40000 ALTER TABLE `sesiones_usuario` DISABLE KEYS */;
INSERT INTO `sesiones_usuario` VALUES (1,1,'872b8eeb751ea16d931e5c6df31e248fb00bdaf0664c0c01fd6b84b7deb2723a','2026-11-16 01:57:47',0,'2026-10-17 06:57:47','2026-10-17 01:59:31'),(2,1,'7b2034a7224b03c95a864c199df235d9e5829a587060c7ef1ea90a8c44ae428b','2026-11-16 02:10:24',0,'2026-10-17 07:10:24','2026-10-17 02:11:27'),(6,1,'6302f32981166f0e4b247007d37213a555001b182abc91dc229fd3194700a484','2026-11-16 22:41:37',0,'2026-10-18 03:41:37','2026-10-17 22:43:02'),(9,1,'0983c88e17d50ac27b24d9770b0976a4eb2ce434acaa39e81baecc5abfd75b2e','2026-11-16 23:02:10',0,'2026-10-18 04:02:10',NULL),(10,1,'cea2d244ce82ca62ae91d4defd351be28b02ec2238aec097a3d98e4028baccfe','2026-11-16 23:03:44',0,'2026-10-18 04:03:44','2026-10-17 23:07:19'),(11,1,'ab3549e23ccab762b2b0674b7639556d50086f6d0961af826f7673caf8ee18f3','2026-11-16 23:18:14',0,'2026-10-18 04:18:13','2026-10-17 23:21:58'),(13,1,'cb65683e227df84930e00dc93f813bf685f5ae9e2b4c9addaf0313bd55e57be1','2026-11-16 23:43:04',0,'2026-10-18 04:43:04','2026-10-17 23:43:05'),(14,1,'977be387a451075c811ad5bbca6bb892e6fde88217d770ca8f07c30b74e39f0f','2026-11-16 23:43:38',0,'2026-10-18 04:43:37','2026-10-17 23:43:38'),(15,1,'7e61279dc4c7e3ef438e274efad01acd3bf806619f2401bb51b84c2383197d7c','2026-11-16 23:44:02',0,'2026-10-18 04:44:01','2026-10-17 23:44:24'),(16,1,'13bb4bb384fa8a98a04a8c35a0180fac4a15df547a66cb93dc708a0307d7747a','2026-11-16 23:45:37',0,'2026-10-18 04:45:37','2026-10-17 23:45:37'),(17,1,'d19a866d1dcfdb99b6821b46db079cc1cac9523f6d4b7bb7964b2e9c716991e2','2026-11-16 23:48:49',0,'2026-10-18 04:48:49','2026-10-17 23:48:50'),(18,1,'a5ebf523836d4bf3be02db9955852b190a5c012f2e1bec34c3502bf5db8882ec','2026-11-16 23:51:51',0,'2026-10-18 04:51:51','2026-10-17 23:54:44'),(19,1,'396041cbdaed1a065b86ed580bdc47955f92b99605f5bc0ebe7cd7998c251ace','2026-11-17 23:24:51',0,'2026-10-19 04:24:51',NULL),(20,1,'9a029a74fb2fc15ceb2e0261fa8f1e7ce934f1ad4e632383692e59952c7824ab','2026-11-17 23:25:03',0,'2026-10-19 04:25:02',NULL),(21,1,'220694d5ec4c21b539df37128c4f6c59f06a1d31d28992f18815c4dc19ce5687','2026-11-17 23:25:46',0,'2026-10-19 04:25:45',NULL),(22,1,'c4b217b2899ff71d84fed57ad789e12431d524d0d552481744e718f4775d1cf4','2026-11-17 23:30:25',0,'2026-10-19 04:30:24','2026-10-18 23:32:26'),(24,1,'5b5deaeef27ad522380f011fe015ed88ba487b4c739a39e3318bb29e5a186c8f','2026-11-17 23:38:59',0,'2026-10-19 04:38:58','2026-10-18 23:39:49'),(25,3,'e5d0ff1eead581f345e305aa2b0d547656d32e40baab61597f612755551f917c','2026-11-17 23:41:11',0,'2026-10-19 04:41:11','2026-10-18 23:43:05'),(26,1,'187ee2968a554a9f3df8cbcd93ae139d600f63f872a7b53a81b2c9e5af080c4b','2026-11-17 23:43:16',0,'2026-10-19 04:43:15','2026-10-18 23:44:12'),(27,1,'c4e81cbfb0d7cd83a50d4682a9ba465f3863a2817c5a2c28334f883f74dc21bf','2026-11-17 23:50:25',0,'2026-10-19 04:50:24','2026-10-18 23:51:44'),(30,1,'430f9de9bab8a30d28aae1dc744e697f6f69cfecb779c8f7cfa1fa5999e62026','2026-11-18 00:14:10',0,'2026-10-19 05:14:09','2026-10-19 00:17:10'),(31,3,'d01c05b4e3c43ec0a1af8a46c0f3ec3f1a1e24a7310a450734acc0a61e3005d5','2026-11-18 00:17:31',0,'2026-10-19 05:17:30','2026-10-19 00:18:01'),(34,1,'e116f155808ef91c35e3257417319e9567eac1ed2edd7f2680bc08e22c32d4be','2026-11-18 00:26:17',0,'2026-10-19 05:26:16','2026-10-19 00:27:27'),(35,1,'6ea7a8a7e062cdb17fefb283bdc0d2883604626c95384b292e72c045e9cc8570','2026-11-18 00:51:42',0,'2026-10-19 05:51:41','2026-10-19 00:52:25'),(36,1,'d476203015773922b9c0e5f7c4d26fe54609bcb560d7ea0d9467f0afc09df4b8','2026-11-18 00:58:21',0,'2026-10-19 05:58:21','2026-10-19 00:59:00'),(37,1,'51061309f7baa34f7a37f08ced700f6e316afaf418d5948e56f10962e211eb2d','2026-11-18 01:02:49',0,'2026-10-19 06:02:49','2026-10-19 01:04:06'),(38,1,'e1ed39bb91da0a5a6000a809d99ce765d34102bb8a3bb4e0f1c6580e0e0b71ee','2026-11-18 01:10:24',0,'2026-10-19 06:10:24','2026-10-19 01:13:05');
/*!40000 ALTER TABLE `sesiones_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id_usuario` int unsigned NOT NULL AUTO_INCREMENT,
  `primer_nombre` varchar(50) NOT NULL,
  `segundo_nombre` varchar(50) DEFAULT NULL,
  `apellido_paterno` varchar(50) NOT NULL,
  `apellido_materno` varchar(50) DEFAULT NULL,
  `correo` varchar(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_nacimiento` date DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `imagen_perfil_url` varchar(500) DEFAULT NULL,
  `imagen_public_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuarios_correo` (`correo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'Daniel','Elias','Guerrero','Arango','gdani4l981@gmail.com','$2a$10$y.G6sShljWu5q4VEkAmeWuyp.lWTQp3xTRk/lAa1mF8JreAbR6dGO','2026-10-15 02:57:08','2008-10-17','Comas','945163144','Av. Rosa de america 948','https://res.cloudinary.com/dindulij3/image/upload/v1792220258/app_mapas/perfiles/usuario_1.jpg','app_mapas/perfiles/usuario_1'),(2,'Juan','Carlos','Fujimori','Chavez','smurfsteam981@gmail.com','$2a$10$VXJeQ4/fKyUgae82EpnFlubgFzQapOn5EPi.FwZlPEa8xJx8t2luO','2026-10-16 21:06:51',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Elias','Gas','Jfj','Hdbs','avenida@m.com','$2a$10$22jsGUSlwCNk93lKEXqZpesPE6Ddz2ZEo8UBPNrLuv.g714EU6tRi','2026-10-19 04:40:57',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'app_mapas'
--

--
-- Dumping routines for database 'app_mapas'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-19 18:44:32

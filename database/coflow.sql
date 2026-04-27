-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: coflow
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `comentario`
--

DROP TABLE IF EXISTS `comentario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comentario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contenido` text,
  `fecha_creacion` datetime DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `tarea_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_comentario_usuario` (`usuario_id`),
  KEY `fk_comentario_tarea` (`tarea_id`),
  CONSTRAINT `fk_comentario_tarea` FOREIGN KEY (`tarea_id`) REFERENCES `tarea` (`id`),
  CONSTRAINT `fk_comentario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comentario`
--

LOCK TABLES `comentario` WRITE;
/*!40000 ALTER TABLE `comentario` DISABLE KEYS */;
INSERT INTO `comentario` VALUES (1,'Empiezo con la creacion de entidades','2026-03-28 13:14:53',1,1),(2,'Ok, cuando lo tengas avisa','2026-03-28 13:15:34',5,1);
/*!40000 ALTER TABLE `comentario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empresa`
--

DROP TABLE IF EXISTS `empresa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresa` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `fecha_creacion` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empresa`
--

LOCK TABLES `empresa` WRITE;
/*!40000 ALTER TABLE `empresa` DISABLE KEYS */;
INSERT INTO `empresa` VALUES (1,'TechNova Solutions','2026-03-28'),(2,'Creativa Studio','2026-03-28'),(3,'DataBridge Corp','2026-03-28');
/*!40000 ALTER TABLE `empresa` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipo`
--

DROP TABLE IF EXISTS `equipo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `descripcion` text,
  `empresa_id` bigint DEFAULT NULL,
  `proyecto_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_equipo_empresa` (`empresa_id`),
  KEY `fk_equipo_proyecto` (`proyecto_id`),
  CONSTRAINT `fk_equipo_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`),
  CONSTRAINT `fk_equipo_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipo`
--

LOCK TABLES `equipo` WRITE;
/*!40000 ALTER TABLE `equipo` DISABLE KEYS */;
/*!40000 ALTER TABLE `equipo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `equipo_usuario`
--

DROP TABLE IF EXISTS `equipo_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipo_usuario` (
  `equipo_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`equipo_id`,`usuario_id`),
  KEY `fk_equipo_usuario_usuario` (`usuario_id`),
  CONSTRAINT `fk_equipo_usuario_equipo` FOREIGN KEY (`equipo_id`) REFERENCES `equipo` (`id`),
  CONSTRAINT `fk_equipo_usuario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipo_usuario`
--

LOCK TABLES `equipo_usuario` WRITE;
/*!40000 ALTER TABLE `equipo_usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `equipo_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificacion`
--

DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mensaje` text,
  `leida` tinyint(1) DEFAULT '0',
  `fecha_creacion` datetime DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_notificacion_usuario` (`usuario_id`),
  CONSTRAINT `fk_notificacion_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacion`
--

LOCK TABLES `notificacion` WRITE;
/*!40000 ALTER TABLE `notificacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proyecto`
--

DROP TABLE IF EXISTS `proyecto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proyecto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `descripcion` text,
  `fecha_incio` varchar(50) DEFAULT NULL,
  `fecha_fin` varchar(50) DEFAULT NULL,
  `empresa_id` bigint DEFAULT NULL,
  `equipo_id` bigint DEFAULT NULL,
  `fecha_fin_estimada` varchar(255) DEFAULT NULL,
  `titulo` varchar(255) DEFAULT NULL,
  `lider_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_proyecto_empresa` (`empresa_id`),
  KEY `fk_proyecto_equipo` (`equipo_id`),
  KEY `fk_proyecto_lider` (`lider_id`),
  CONSTRAINT `fk_proyecto_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`),
  CONSTRAINT `fk_proyecto_equipo` FOREIGN KEY (`equipo_id`) REFERENCES `equipo` (`id`),
  CONSTRAINT `fk_proyecto_lider` FOREIGN KEY (`lider_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proyecto`
--

LOCK TABLES `proyecto` WRITE;
/*!40000 ALTER TABLE `proyecto` DISABLE KEYS */;
INSERT INTO `proyecto` VALUES (1,'Creacin del Backend',NULL,NULL,NULL,1,NULL,'2026-04-05T13:13','Creacin del Backend',NULL);
/*!40000 ALTER TABLE `proyecto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proyecto_usuario`
--

DROP TABLE IF EXISTS `proyecto_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proyecto_usuario` (
  `proyecto_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`proyecto_id`,`usuario_id`),
  KEY `fk_proyecto_usuario_usuario` (`usuario_id`),
  CONSTRAINT `fk_proyecto_usuario_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_proyecto_usuario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proyecto_usuario`
--

LOCK TABLES `proyecto_usuario` WRITE;
/*!40000 ALTER TABLE `proyecto_usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `proyecto_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tarea`
--

DROP TABLE IF EXISTS `tarea`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tarea` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `descripcion` text,
  `estado_tarea` enum('PENDIENTE','ASIGNADA','EN_PROCESO','BLOQUEADA','EN_REVISION','APROBADA','RECHAZADA','COMPLETADA') DEFAULT NULL,
  `prioridad` enum('BAJA','MEDIA','URGENTE','CRITICA') DEFAULT NULL,
  `fecha_inicio` varchar(50) DEFAULT NULL,
  `fecha_fin` varchar(50) DEFAULT NULL,
  `proyecto_id` bigint DEFAULT NULL,
  `creado_por` bigint DEFAULT NULL,
  `created_at` varchar(255) DEFAULT NULL,
  `fecha_limite` varchar(255) DEFAULT NULL,
  `titulo` varchar(255) DEFAULT NULL,
  `updated_at` varchar(255) DEFAULT NULL,
  `usuario_asignado_id` bigint DEFAULT NULL,
  `fecha_bloqueo` varchar(255) DEFAULT NULL,
  `requiere_revision` bit(1) NOT NULL,
  `tiempo_estimado` int DEFAULT NULL,
  `tiempo_real` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tarea_proyecto` (`proyecto_id`),
  KEY `fk_tarea_creado_por` (`creado_por`),
  KEY `fk_tarea_asignado` (`usuario_asignado_id`),
  CONSTRAINT `fk_tarea_asignado` FOREIGN KEY (`usuario_asignado_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_tarea_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_tarea_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tarea`
--

LOCK TABLES `tarea` WRITE;
/*!40000 ALTER TABLE `tarea` DISABLE KEYS */;
INSERT INTO `tarea` VALUES (1,'Creacion del modelo de base de datos',NULL,'EN_PROCESO','MEDIA',NULL,NULL,1,1,'2026-03-28T13:14:08.959028100','2026-04-04T13:13','Creacion del modelo de base de datos','2026-03-28T13:14:22.536291400',1,NULL,_binary '\0',NULL,0);
/*!40000 ALTER TABLE `tarea` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tarea_usuario`
--

DROP TABLE IF EXISTS `tarea_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tarea_usuario` (
  `tarea_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`tarea_id`,`usuario_id`),
  KEY `fk_tarea_usuario_usuario` (`usuario_id`),
  CONSTRAINT `fk_tarea_usuario_tarea` FOREIGN KEY (`tarea_id`) REFERENCES `tarea` (`id`),
  CONSTRAINT `fk_tarea_usuario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tarea_usuario`
--

LOCK TABLES `tarea_usuario` WRITE;
/*!40000 ALTER TABLE `tarea_usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `tarea_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) DEFAULT NULL,
  `apellidos` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `fecha_creacion` date DEFAULT NULL,
  `rol` enum('ADMIN','REVISOR','USUARIO') DEFAULT NULL,
  `empresa_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_usuario_empresa` (`empresa_id`),
  CONSTRAINT `fk_usuario_empresa` FOREIGN KEY (`empresa_id`) REFERENCES `empresa` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Carlos','García López','admin@technova.com','$2a$10$PmFgwUGXOoRJaGdZHaZDIOf9DNAjZBXxWFRZyNwLG5jasVnJlrA5C','2026-03-28','ADMIN',1),(2,'Laura','Martínez Ruiz','admin@creativa.com','$2a$10$Fz8.EkPoc9QUHqIl7/qUy.6P.D0gwcSp0fm1d.7fhcHNRr3QWEYKa','2026-03-28','ADMIN',2),(3,'Marcos','Fernández Gil','admin@databridge.com','$2a$10$qJJ9Dpovxwj/Ah58yI0Mo.4Y8wpRU1dazWKX3rYl0ceq9UV5FmFe2','2026-03-28','ADMIN',3),(4,'Jorge','Vizuete Mendez','jorge.vizuete.mendez@gmail.com','$2a$10$Hijy83.B2fl0p8b0zKNBW.TeUwlYdp6fOmF6Y3WyNkk4e9SVTG7LG','2026-03-28','USUARIO',2),(5,'Jorge','Vizuete Mendez','jorge@gmail.com','$2a$10$QxMkwM91jrNXJFKol3P9NO2MySXdiJaGL.YpXiz4PNw55P2D.Wwo2','2026-03-28','USUARIO',1);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-27 16:36:13

-- MySQL dump 10.13  Distrib 5.7.11, for Win64 (x86_64)
--
-- Host: localhost    Database: relojd
-- ------------------------------------------------------
-- Server version	5.7.11-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `equipos`
--

DROP TABLE IF EXISTS `equipos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `equipos` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `IP` varchar(25) DEFAULT NULL,
  `Nombre` varchar(30) DEFAULT NULL,
  `Latencia` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `equipos`
--

LOCK TABLES `equipos` WRITE;
/*!40000 ALTER TABLE `equipos` DISABLE KEYS */;
INSERT INTO `equipos` VALUES (1,'10.100.12.100','Rodos',10),(2,'10.100.12.100','Rodos',10);
/*!40000 ALTER TABLE `equipos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horacentral`
--

DROP TABLE IF EXISTS `horacentral`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `horacentral` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `hPrev` varchar(15) DEFAULT NULL,
  `hRef` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horacentral`
--

LOCK TABLES `horacentral` WRITE;
/*!40000 ALTER TABLE `horacentral` DISABLE KEYS */;
INSERT INTO `horacentral` VALUES (1,'1','1931');
/*!40000 ALTER TABLE `horacentral` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horaequipos`
--

DROP TABLE IF EXISTS `horaequipos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `horaequipos` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `IDhSincr` int(11) DEFAULT NULL,
  `IDEquipo` int(11) DEFAULT NULL,
  `hEquipo` varchar(15) DEFAULT NULL,
  `aEquipo` int(11) DEFAULT NULL,
  `ralentizar` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `IDhSincr` (`IDhSincr`),
  KEY `IDEquipo` (`IDEquipo`),
  CONSTRAINT `horaequipos_ibfk_1` FOREIGN KEY (`IDhSincr`) REFERENCES `horacentral` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `horaequipos_ibfk_2` FOREIGN KEY (`IDEquipo`) REFERENCES `equipos` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horaequipos`
--

LOCK TABLES `horaequipos` WRITE;
/*!40000 ALTER TABLE `horaequipos` DISABLE KEYS */;
INSERT INTO `horaequipos` VALUES (1,1,1,'s',1,8),(2,1,2,'h',2,9),(3,1,2,'q',3,7),(4,1,1,'j',4,5);
/*!40000 ALTER TABLE `horaequipos` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-15 23:48:16

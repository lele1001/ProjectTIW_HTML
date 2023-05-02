-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: projecttiw_db
-- ------------------------------------------------------
-- Server version	8.0.32

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
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `articleID` int NOT NULL AUTO_INCREMENT,
  `ownerID` int NOT NULL,
  `name` varchar(20) NOT NULL,
  `description` varchar(100) NOT NULL,
  `price` float NOT NULL,
  `auctionID` int DEFAULT '0',
  `image` varchar(10000) DEFAULT 'null',
  PRIMARY KEY (`articleID`),
  UNIQUE KEY `articleID_UNIQUE` (`articleID`),
  KEY `userID` (`ownerID`),
  CONSTRAINT `userID` FOREIGN KEY (`ownerID`) REFERENCES `persons` (`userID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `article_chk_1` CHECK ((`price` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=9932 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (5,123,'macchina fotografica','macchina fotografica digitale compatta',80,65,'macchinaFotografica.jpg'),(23,123,'peluche pinguino','nuovo ancora con l\'etichetta',5,5754,'peluchePinguino.jpeg'),(123,123,'piatti fondi','6 piatti fondi',7.5,111,'piattiFondi.jpeg'),(124,123,'piatti piani','6 piatti piani',7.5,111,'piattiPiani.jpeg'),(300,123,'monopoly','nuovo',15,223,'monopoly.jpeg'),(301,123,'scarabeo','mancano alcune lettere',5,223,'scarabeo.jpeg'),(453,123,'nokia 3310','come nuovo e ancora funzionante',200,98,'nokia3310.jpeg'),(454,123,'iPhone 2G','ancora funzionante',250,98,'iphone2g.jpeg'),(492,888,'Harry Potter 1','in perfette condizioni',5,456,'harryPotter.jpeg'),(493,888,'Harry Potter 2','in perfette condizioni',5,456,'harryPotter.jpeg'),(494,888,'Harry Potter 3','in perfette condizioni',5,456,'harryPotter.jpeg'),(495,888,'Harry Potter 4','in perfette condizioni',5,456,'harryPotter.jpeg'),(496,888,'Harry Potter 5','un po\' rovinato a pagina 232',5,456,'harryPotter.jpeg'),(497,888,'Harry Potter 6','in perfette condizioni',5,456,'harryPotter.jpeg'),(498,888,'Harry Potter 7','in perfette condizioni',5,456,'harryPotter.jpeg'),(547,888,'casse','7 diffusori acustici di ultima generazione',500,765,'diffusoreAcustico.jpeg'),(548,888,'subwoofer','subwoofer per home theater',300,765,'subwoofer.jpeg'),(3465,4353,'Fifa 2022','utilizzato 2 volte, per ps4',20,0,'fifa22.jpeg'),(7023,2352,'mountain bike','mountain bike nuova',1000,4284,'mountainBike.jpeg'),(7024,2352,'mountain bike','mountain bike nuova',1000,0,'mountainBike.jpeg'),(7025,2352,'scarpe da ciclismo','numero 42',30,0,'scarpeCiclismo.jpeg'),(7656,2352,'barbecue','in perfette condizioni',400,3457,'barbecue.jpeg'),(8138,1343,'quadro','rappresenta il mare, dimensioni 30x50',50,8829,'quadroMare.jpeg');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-05-01  8:17:26

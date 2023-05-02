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
-- Table structure for table `auction`
--

DROP TABLE IF EXISTS `auction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auction` (
  `auctionID` int NOT NULL AUTO_INCREMENT,
  `ownerID` int NOT NULL,
  `title` varchar(20) NOT NULL,
  `startingPrice` float NOT NULL,
  `minIncrease` float NOT NULL,
  `expiryDate` datetime NOT NULL,
  `winnerID` int DEFAULT '0',
  `actualPrice` float NOT NULL DEFAULT '0',
  `isClosed` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`auctionID`),
  UNIQUE KEY `auctionID_UNIQUE` (`auctionID`),
  KEY `ownerID` (`ownerID`),
  CONSTRAINT `ownerID` FOREIGN KEY (`ownerID`) REFERENCES `persons` (`userID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `auction_chk_1` CHECK ((`startingPrice` > 0)),
  CONSTRAINT `auction_chk_2` CHECK ((`minIncrease` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=9766 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction`
--

LOCK TABLES `auction` WRITE;
/*!40000 ALTER TABLE `auction` DISABLE KEYS */;
INSERT INTO `auction` VALUES (65,123,'macchina fotografica',80,5,'2023-02-13 10:30:00',1343,100,1),(98,123,'telefoni vintage',450,50,'2022-01-23 23:17:00',2352,625,1),(111,123,'piatti',15,2,'2024-05-23 12:12:35',0,20,0),(223,123,'giochi da tavolo',20,2,'2023-12-23 00:00:00',0,20,0),(456,888,'libri fantasy',35,5,'2024-06-23 01:01:01',0,45,0),(765,888,'home theater',800,25,'2020-10-23 17:00:30',2352,975,1),(3457,2352,'barbecue',400,20,'2024-08-17 12:00:00',0,400,0),(4284,2352,'bici',1000,50,'2023-04-24 07:36:00',0,1000,1),(8829,1343,'quadro',50,5,'2023-04-27 21:00:00',0,50,0);
/*!40000 ALTER TABLE `auction` ENABLE KEYS */;
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

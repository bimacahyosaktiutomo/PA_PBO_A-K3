-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 31, 2024 at 01:27 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `dbifandipbo`
--

-- --------------------------------------------------------

--
-- Table structure for table `akun`
--

CREATE TABLE `akun` (
  `uid` int(11) NOT NULL,
  `username` varchar(11) NOT NULL,
  `password` varchar(11) NOT NULL,
  `email` text NOT NULL,
  `izin` varchar(11) NOT NULL,
  `gambar` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `akun`
--

INSERT INTO `akun` (`uid`, `username`, `password`, `email`, `izin`, `gambar`) VALUES
(1, 'admin', 'admin', 'admin@gmail.com', 'admin', ''),
(10, 'a', 'a', 'a', 'user', '');

--
-- Triggers `akun`
--
DELIMITER $$
CREATE TRIGGER `delete_bookmark_before_akun` BEFORE DELETE ON `akun` FOR EACH ROW DELETE FROM bookmark
WHERE uid = OLD.uid
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `remove_komen_before_akun` BEFORE DELETE ON `akun` FOR EACH ROW DELETE FROM komentar
WHERE uid = OLD.uid
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `remove_resep_after_akun` AFTER DELETE ON `akun` FOR EACH ROW DELETE FROM resep
WHERE
	id_user = OLD.uid
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `bookmark`
--

CREATE TABLE `bookmark` (
  `idr` int(11) NOT NULL,
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `komentar`
--

CREATE TABLE `komentar` (
  `idk` int(255) NOT NULL,
  `rating` int(1) NOT NULL,
  `komen` varchar(100) NOT NULL,
  `idr` int(11) NOT NULL,
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `komentar`
--

INSERT INTO `komentar` (`idk`, `rating`, `komen`, `idr`, `uid`) VALUES
(1, 5, 'b', 30, 1);

--
-- Triggers `komentar`
--
DELIMITER $$
CREATE TRIGGER `add_rating_resep_after_komen` AFTER INSERT ON `komentar` FOR EACH ROW UPDATE resep
SET 
	rating = rating + NEW.rating
WHERE
	idr = NEW.idr
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `reduce_rating_after_komen` AFTER DELETE ON `komentar` FOR EACH ROW UPDATE resep
SET
	rating = rating - OLD.rating
WHERE idr = OLD.idr
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `resep`
--

CREATE TABLE `resep` (
  `idr` int(11) NOT NULL,
  `nama` text NOT NULL,
  `kategori` text NOT NULL,
  `deskripsi` text NOT NULL,
  `alat` text NOT NULL,
  `bahan` text NOT NULL,
  `instruksi` text NOT NULL,
  `status` varchar(11) NOT NULL,
  `gambar` text NOT NULL,
  `id_user` int(11) NOT NULL,
  `rating` int(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `resep`
--
DELIMITER $$
CREATE TRIGGER `delete_bookmark_before_resep` BEFORE DELETE ON `resep` FOR EACH ROW DELETE FROM bookmark
WHERE idr = OLD.idr
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `akun`
--
ALTER TABLE `akun`
  ADD PRIMARY KEY (`uid`);

--
-- Indexes for table `bookmark`
--
ALTER TABLE `bookmark`
  ADD KEY `idr` (`idr`),
  ADD KEY `uid` (`uid`);

--
-- Indexes for table `komentar`
--
ALTER TABLE `komentar`
  ADD PRIMARY KEY (`idk`),
  ADD KEY `komentar_ibfk_1` (`idr`),
  ADD KEY `komentar_ibfk_2` (`uid`);

--
-- Indexes for table `resep`
--
ALTER TABLE `resep`
  ADD PRIMARY KEY (`idr`),
  ADD KEY `property` (`id_user`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `akun`
--
ALTER TABLE `akun`
  MODIFY `uid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `komentar`
--
ALTER TABLE `komentar`
  MODIFY `idk` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `resep`
--
ALTER TABLE `resep`
  MODIFY `idr` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

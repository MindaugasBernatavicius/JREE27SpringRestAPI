DROP TABLE IF EXISTS `item`;

CREATE TABLE IF NOT EXISTS `item` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `count` int(11) DEFAULT NULL,
    `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


INSERT INTO db.item (title, count) VALUES ('Snowboard', 100);
INSERT INTO db.item (title, count) VALUES ('Teddy', 100);
INSERT INTO db.item (title, count) VALUES ('Suit', 100);
INSERT INTO db.item (title, count) VALUES ('Car', 100);
INSERT INTO db.item (title, count) VALUES ('Skies', 100);
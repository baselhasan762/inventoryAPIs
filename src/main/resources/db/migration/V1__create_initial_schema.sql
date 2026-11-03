CREATE TABLE `categories` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `is_active` bit(1) NOT NULL,
                              `description` varchar(255) DEFAULT NULL,
                              `name` varchar(100) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `locations` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `is_active` bit(1) NOT NULL,
                             `address` varchar(255) DEFAULT NULL,
                             `code` varchar(30) NOT NULL,
                             `name` varchar(100) NOT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `UKnjcw38t3qcy312pglqpf3pd59` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `is_active` bit(1) NOT NULL,
                         `created_at` datetime(6) NOT NULL,
                         `email` varchar(150) NOT NULL,
                         `full_name` varchar(100) NOT NULL,
                         `password` varchar(255) NOT NULL,
                         `role` enum('ADMIN','STAFF') NOT NULL,
                         `updated_at` datetime(6) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `products` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `is_active` bit(1) NOT NULL,
                            `category_id` bigint DEFAULT NULL,
                            `cost_price` decimal(12,2) NOT NULL,
                            `created_at` datetime(6) NOT NULL,
                            `description` text,
                            `name` varchar(150) NOT NULL,
                            `reorder_level` int NOT NULL,
                            `unit_of_measure` varchar(20) NOT NULL,
                            `unit_price` decimal(12,2) NOT NULL,
                            `updated_at` datetime(6) NOT NULL,
                            `user_id` bigint DEFAULT NULL,
                            `sku` varchar(50) NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UKfhmd06dsmj6k0n90swsh8ie9g` (`sku`),
                            KEY `FKdb050tk37qryv15hd932626th` (`user_id`),
                            KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
                            CONSTRAINT `FKdb050tk37qryv15hd932626th` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                            CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `refresh_tokens` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime(6) NOT NULL,
                                  `expires_at` datetime(6) NOT NULL,
                                  `revoked` bit(1) NOT NULL,
                                  `token_hash` varchar(64) NOT NULL,
                                  `user_id` bigint NOT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `UKo2mlirhldriil2y7krapq4frt` (`token_hash`),
                                  KEY `FK1lih5y2npsf8u5o3vhdb9y0os` (`user_id`),
                                  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `stock_level` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `quantity` int NOT NULL,
                               `location_id` bigint NOT NULL,
                               `product_id` bigint NOT NULL,
                               `updated_at` datetime(6) NOT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `UKffg5n4oaso573s2ay3uf57nix` (`product_id`,`location_id`),
                               KEY `FKr9x4o4tcu9m1vsf5ukuuo7lmu` (`location_id`),
                               CONSTRAINT `FKpwj5xiu633tafi4latwfdrq2l` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
                               CONSTRAINT `FKr9x4o4tcu9m1vsf5ukuuo7lmu` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `stock_movement` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime(6) NOT NULL,
                                  `quantity` int NOT NULL,
                                  `reason` varchar(255) DEFAULT NULL,
                                  `type` enum('IN','OUT','TRANSFER_IN','TRANSFER_OUT') NOT NULL,
                                  `created_by` bigint NOT NULL,
                                  `location_id` bigint NOT NULL,
                                  `product_id` bigint NOT NULL,
                                  `transfer_id` varchar(36) DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKnx83m5v2f9lf3cvp10wky26u` (`created_by`),
                                  KEY `FK50ugdsd393hlmqvowh2oyn1eo` (`location_id`),
                                  KEY `FKhj6nusfm483wy67vt7v7hjdma` (`product_id`),
                                  CONSTRAINT `FK50ugdsd393hlmqvowh2oyn1eo` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`),
                                  CONSTRAINT `FKhj6nusfm483wy67vt7v7hjdma` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
                                  CONSTRAINT `FKnx83m5v2f9lf3cvp10wky26u` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS ad;
USE ad;

CREATE TABLE `bidders`
(
    `id`      int(11)     NOT NULL,
    `name`    varchar(64) NOT NULL,
    `rating`  int(11)      DEFAULT NULL,
    `country` varchar(64)  DEFAULT NULL,
    `place`   varchar(256) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items`
(
    `id`             int           NOT NULL,
    `owner_id`       int           NOT NULL,
    `name`           varchar(128)  NOT NULL,
    `currently`      decimal(8, 2) NOT NULL,
    `first_bid`      decimal(8, 2) NOT NULL,
    `number_of_bids` int           NOT NULL,
    `started`        datetime      NOT NULL,
    `ends`           datetime      NOT NULL,
    `description`    varchar(4000) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    KEY `fk_from_items_to_bidders_idx` (`owner_id`),
    CONSTRAINT `fk_from_items_to_bidders` FOREIGN KEY (`owner_id`) REFERENCES `bidders` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `categories`
(
    `id`   int(11)     NOT NULL,
    `name` varchar(64) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items_locations`
(
    `item_id`   int NOT NULL,
    `place`     varchar(256) DEFAULT NULL,
    `latitude`  varchar(64)  DEFAULT NULL,
    `longitude` varchar(64)  DEFAULT NULL,
    PRIMARY KEY (`item_id`),
    UNIQUE KEY `item_id_UNIQUE` (`item_id`),
    KEY `fk_from_items_locations_to_items_idx` (`item_id`),
    CONSTRAINT `fk_from_items_locations_to_items` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items_categories`
(
    `item_id`     int NOT NULL,
    `category_id` int NOT NULL,
    PRIMARY KEY (`item_id`, `category_id`),
    KEY `fk_from_items_categories_to_items_idx` (`item_id`),
    KEY `fk_from_tems_categories_to_categories_idx` (`category_id`),
    CONSTRAINT `fk_from_items_categories_to_items` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`),
    CONSTRAINT `fk_from_tems_categories_to_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `bids`
(
    `id`        int           NOT NULL,
    `bidder_id` int           NOT NULL,
    `item_id`   int           NOT NULL,
    `time`      datetime      NOT NULL,
    `amount`    decimal(8, 2) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    KEY `fk_from_bids_to_bidders_idx` (`bidder_id`),
    KEY `fk_from_bids_to_items_idx` (`item_id`),
    CONSTRAINT `fk_from_bids_to_bidders` FOREIGN KEY (`bidder_id`) REFERENCES `bidders` (`id`),
    CONSTRAINT `fk_from_bids_to_items` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

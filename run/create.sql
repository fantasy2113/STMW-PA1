CREATE DATABASE IF NOT EXISTS ad;
USE ad;

CREATE TABLE `users`
(
    `id`      int(11)     NOT NULL,
    `name`    varchar(64) NOT NULL,
    `rating`  int(11)      DEFAULT 0,
    `country` varchar(64)  DEFAULT '',
    `place`   varchar(256) DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items`
(
    `id`             int(11)       NOT NULL,
    `user_id`        int(11)       NOT NULL,
    `name`           varchar(128)  NOT NULL,
    `currently`      decimal(8, 2) NOT NULL,
    `first_bid`      decimal(8, 2) NOT NULL,
    `number_of_bids` int(11)       NOT NULL,
    `buy_price`      decimal(8, 2) NOT NULL,
    `started`        datetime      NOT NULL,
    `ends`           datetime      NOT NULL,
    `description`    varchar(4000) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    KEY `fk_from_items_to_users_idx` (`user_id`),
    CONSTRAINT `fk_from_items_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
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
    `item_id`   int(11) NOT NULL,
    `place`     varchar(256) DEFAULT '',
    `latitude`  varchar(64)  DEFAULT '',
    `longitude` varchar(64)  DEFAULT '',
    PRIMARY KEY (`item_id`),
    UNIQUE KEY `item_id_UNIQUE` (`item_id`),
    KEY `fk_from_items_locations_to_items_idx` (`item_id`),
    CONSTRAINT `fk_from_items_locations_to_items` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items_categories`
(
    `item_id`     int(11) NOT NULL,
    `category_id` int(11) NOT NULL,
    PRIMARY KEY (`item_id`, `category_id`),
    KEY `fk_from_items_categories_to_items_idx` (`item_id`),
    KEY `fk_from_items_categories_to_categories_idx` (`category_id`),
    CONSTRAINT `fk_from_items_categories_to_items` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`),
    CONSTRAINT `fk_from_items_categories_to_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `bids`
(
    `id`      int(11)       NOT NULL,
    `user_id` int(11)       NOT NULL,
    `item_id` int(11)       NOT NULL,
    `time`    datetime      NOT NULL,
    `amount`  decimal(8, 2) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    KEY `fk_from_bids_to_users_idx` (`user_id`),
    KEY `fk_from_bids_to_items_idx` (`item_id`),
    CONSTRAINT `fk_from_bids_to_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_from_bids_to_items` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

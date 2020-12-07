CREATE DATABASE IF NOT EXISTS ad;
USE ad;

CREATE TABLE `items`
(
    `id`             int(11) NOT NULL,
    `user_id`        int(11) NOT NULL,
    `name`           varchar(128)  DEFAULT NULL,
    `currently`      decimal(8, 2) DEFAULT NULL,
    `first_bid`      decimal(8, 2) DEFAULT NULL,
    `number_of_bids` int(11)       DEFAULT NULL,
    `started`        datetime      DEFAULT NULL,
    `ends`           datetime      DEFAULT NULL,
    `description`    varchar(4000) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `bids`
(
    `id`      int(11) NOT NULL,
    `user_id` int(11) NOT NULL,
    `item_id` int(11) NOT NULL,
    `time`    datetime      DEFAULT NULL,
    `amount`  decimal(8, 2) DEFAULT NULL,
    PRIMARY KEY (`id`, `user_id`, `item_id`),
    UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items_locations`
(
    `item_id`   int(11) NOT NULL,
    `place`     varchar(64) DEFAULT NULL,
    `latitude`  varchar(64) DEFAULT NULL,
    `longitude` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`item_id`),
    UNIQUE KEY `item_id_UNIQUE` (`item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `categories`
(
    `id`   int(11) NOT NULL,
    `name` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `users`
(
    `id`      int(11)     NOT NULL,
    `name`    varchar(64) NOT NULL,
    `rating`  int(11)      DEFAULT NULL,
    `country` varchar(64)  DEFAULT NULL,
    `place`   varchar(128) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `id_UNIQUE` (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

CREATE TABLE `items_categories`
(
    `item_id`     int(11) NOT NULL,
    `category_id` int(11) NOT NULL,
    PRIMARY KEY (`item_id`, `category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
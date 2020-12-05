CREATE DATABASE IF NOT EXISTS ad;
USE ad;

CREATE TABLE `items`
(
    `id`             int(11)       DEFAULT NULL,
    `user_id`        varchar(64)   DEFAULT NULL,
    `name`           varchar(128)  DEFAULT NULL,
    `currently`      decimal(8, 2) DEFAULT NULL,
    `first_bid`      decimal(8, 2) DEFAULT NULL,
    `number_of_bids` int(11)       DEFAULT NULL,
    `country`        varchar(64)   DEFAULT NULL,
    `started`        datetime      DEFAULT NULL,
    `ends`           datetime      DEFAULT NULL,
    `description`    varchar(4000) DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
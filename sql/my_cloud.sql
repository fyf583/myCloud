-- ============================================================
-- myCloud 建表脚本（MySQL 8.0）
-- 使用方式：
--   mysql -uroot -p < sql/my_cloud.sql
-- 说明：
--   1. create_time / update_time 由服务端 AutoFill 切面填充，不设数据库默认值；
--   2. status 统一约定：1 正常，2 已删除（软删除）；
--   3. user_id 由网关鉴权后透传的 X-User-Id 写入，业务层不接受前端传入。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `my_cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `my_cloud`;

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `password` varchar(100) NOT NULL COMMENT 'BCrypt 密文，长度必须 >= 60',
  `phone` varchar(20) NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_pk` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 留言
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `message` varchar(500) NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `message_create_time_id_index` (`create_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 笔记 / 笔记分组
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `note` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `note` text NOT NULL,
  `type_id` int DEFAULT NULL COMMENT '所属分组 id，NULL 表示未分组',
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_note_create_time` (`create_time`),
  KEY `idx_note_type_id` (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `note_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `group_name` varchar(50) NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 日记
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `diary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `diary` text NOT NULL,
  `status` int NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_diary_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 相册：文件落盘，表里只存相对网关的访问地址
--   url 形如 /album/file/{uuid}.jpg，真实文件在 mycloud.album.storage-path 目录下
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `album` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `photo_name` varchar(100) NOT NULL COMMENT '照片名称',
  `description` varchar(255) DEFAULT NULL COMMENT '照片描述',
  `url` varchar(255) NOT NULL COMMENT '访问地址（相对网关：/album/file/xxx.jpg）',
  `status` int NOT NULL DEFAULT '1' COMMENT '1 正常、2 已删除',
  `create_time` datetime NOT NULL COMMENT '上传时间',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_album_user_create_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

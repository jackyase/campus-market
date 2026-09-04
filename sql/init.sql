-- 校园二手集市 数据库初始化脚本（MySQL 8.x）
-- 数据表由 JPA（ddl-auto: update）自动创建，本脚本仅负责建库。
-- 执行：mysql -u root -p < sql/init.sql

CREATE DATABASE IF NOT EXISTS campus_market
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_market;

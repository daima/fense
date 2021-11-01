/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50505
Source Host           : 127.0.0.1:3306
Source Database       : fense

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2021-10-28 23:41:46
*/

SET FOREIGN_KEY_CHECKS=0;
CREATE SCHEMA IF NOT EXISTS fense;
USE fense;
-- ----------------------------
-- Table structure for apply
-- ----------------------------
CREATE TABLE IF NOT EXISTS `apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `applicant` int(11) NOT NULL COMMENT '申请者',
  `role_id` int(11) DEFAULT NULL COMMENT '角色ID',
  `privilege_id` bigint(20) NOT NULL COMMENT '申请的权限ID',
  `reason` varchar(1000) NOT NULL COMMENT '申请事由',
  `status` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '申请时间',
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for apply_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS `apply_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apply_id` bigint(20) NOT NULL COMMENT '申请ID',
  `approver` int(11) NOT NULL COMMENT '审批者',
  `status` tinyint(4) NOT NULL COMMENT '同意/不同意',
  `opinion` varchar(1000) DEFAULT NULL,
  `next_approver` int(11) DEFAULT 1,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for dataset
-- ----------------------------
CREATE TABLE IF NOT EXISTS `dataset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent` bigint(20) NOT NULL DEFAULT 1,
  `name` varchar(200) NOT NULL COMMENT '名称',
  `datasource_id` int(11) NOT NULL COMMENT '数据源ID',
  `type` tinyint(1) NOT NULL COMMENT '类型',
  `owner` int(11) NOT NULL DEFAULT 1 COMMENT '负责人',
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=930 DEFAULT CHARSET=utf8 COMMENT='数据集';

-- ----------------------------
-- Table structure for datasource
-- ----------------------------
CREATE TABLE IF NOT EXISTS `datasource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '名称',
  `jdbc_url` varchar(500) NOT NULL COMMENT '连接URL',
  `user` varchar(100) NOT NULL COMMENT '用户名',
  `pass` varchar(200) NOT NULL COMMENT '密码',
  `engine` varchar(20) NOT NULL COMMENT '引擎',
  `pool_conf` varchar(1000) DEFAULT NULL,
  `create_user` int(11) NOT NULL COMMENT '创建者',
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='数据源';

-- ----------------------------
-- Table structure for privilege
-- ----------------------------
CREATE TABLE IF NOT EXISTS `privilege` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '名称',
  `dataset_id` bigint(20) NOT NULL COMMENT '数据集ID',
  `mode` varchar(100) NOT NULL DEFAULT 'SELECT' COMMENT '权限模式',
  `create_user` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='权限';

-- ----------------------------
-- Table structure for privilege_grant
-- ----------------------------
CREATE TABLE IF NOT EXISTS `privilege_grant` (
  `privilege_id` bigint(20) NOT NULL COMMENT '权限ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '创建时间',
  PRIMARY KEY (`privilege_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色权限关系表';

-- ----------------------------
-- Table structure for query_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS `query_log` (
  `type` int(11) DEFAULT NULL,
  `event_date` date DEFAULT NULL,
  `event_time` datetime DEFAULT NULL,
  `query_start_time` datetime DEFAULT NULL,
  `query_duration_ms` bigint(20) DEFAULT NULL,
  `result_rows` bigint(20) DEFAULT NULL,
  `result_bytes` bigint(20) DEFAULT NULL,
  `query` text DEFAULT NULL,
  `exception` text DEFAULT NULL,
  `user` varchar(100) DEFAULT NULL,
  `query_id` varchar(100) DEFAULT NULL,
  `address` varchar(100) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `os_user` varchar(100) DEFAULT NULL,
  `client_hostname` varchar(100) DEFAULT NULL,
  `client_name` varchar(100) DEFAULT NULL,
  `client_version` varchar(100) DEFAULT NULL,
  `http_method` int(11) DEFAULT NULL,
  `http_user_agent` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for role
-- ----------------------------
CREATE TABLE IF NOT EXISTS `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '角色名称',
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='角色表';

-- ----------------------------
-- Table structure for role_grant
-- ----------------------------
CREATE TABLE IF NOT EXISTS `role_grant` (
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `role_id` int(11) NOT NULL COMMENT '角色ID',
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户角色关系';

-- ----------------------------
-- Table structure for user
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '姓名',
  `email` varchar(200) NOT NULL COMMENT '邮箱',
  `password` varchar(200) NOT NULL COMMENT '登录密码',
  `is_admin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是管理员',
  `last_login_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '最后登录时间',
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用户表';

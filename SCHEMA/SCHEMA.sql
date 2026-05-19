-- MySQL dump
--   Database: elms
-- ------------------------------------------------------
-- Server version	9.7.0
create database elms;
use elms;


CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
);

-- ------------------------------------------------------

 

CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
);

-- ------------------------------------------------------



CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` text NOT NULL,
  `role_id` bigint NOT NULL,
  `department_id` bigint DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_role_id` (`role_id`),
  KEY `idx_users_department_id` (`department_id`),

  CONSTRAINT `fk_user_department`
      FOREIGN KEY (`department_id`)
      REFERENCES `departments` (`id`),

  CONSTRAINT `fk_user_role`
      FOREIGN KEY (`role_id`)
      REFERENCES `roles` (`id`)
);

-- ------------------------------------------------------



CREATE TABLE `leave_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `status` enum('ACTIVE','INACTIVE')
      NOT NULL DEFAULT 'ACTIVE',

  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
);

-- ------------------------------------------------------



CREATE TABLE `leave_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  `user_id` bigint NOT NULL,

  `leave_type_id` bigint DEFAULT NULL,

  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,

  `reason` varchar(50) DEFAULT NULL,

  `status` varchar(30) NOT NULL,

  `year` int DEFAULT NULL,

  `created_at`
      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

  `submitted_at`
      timestamp NULL DEFAULT NULL,

  `decision_at`
      timestamp NULL DEFAULT NULL,

  `approver_id`
      bigint DEFAULT NULL,

  PRIMARY KEY (`id`),

  KEY `idx_leave_requests_user_id` (`user_id`),
  KEY `idx_leave_requests_leave_type_id` (`leave_type_id`),
  KEY `idx_leave_requests_approver_id` (`approver_id`),

  CONSTRAINT `fk_leave_approver`
      FOREIGN KEY (`approver_id`)
      REFERENCES `users` (`id`),

  CONSTRAINT `fk_leave_type`
      FOREIGN KEY (`leave_type_id`)
      REFERENCES `leave_types` (`id`),

  CONSTRAINT `fk_leave_user`
      FOREIGN KEY (`user_id`)
      REFERENCES `users` (`id`),

  CONSTRAINT `chk_dates`
      CHECK ((`end_date` >= `start_date`))
);

-- ------------------------------------------------------


CREATE TABLE `leave_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  `leave_id` bigint NOT NULL,

  `user_id` bigint NOT NULL,

  `message` text NOT NULL,

  `created_at`
      timestamp NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),

  KEY `idx_leave_comments_leave_id` (`leave_id`),
  KEY `idx_leave_comments_user_id` (`user_id`),

  CONSTRAINT `fk_comment_leave`
      FOREIGN KEY (`leave_id`)
      REFERENCES `leave_requests` (`id`),

  CONSTRAINT `fk_comment_user`
      FOREIGN KEY (`user_id`)
      REFERENCES `users` (`id`)
);

-- ------------------------------------------------------



CREATE TABLE `leave_policies` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  `year` int NOT NULL,

  `leave_type_id` bigint NOT NULL,

  `allocated_leave` int NOT NULL,

  PRIMARY KEY (`id`),

  UNIQUE KEY `uq_policy`
      (`year`,`leave_type_id`),

  KEY `idx_leave_policies_leave_type_id`
      (`leave_type_id`),

  CONSTRAINT `fk_policy_leave_type`
      FOREIGN KEY (`leave_type_id`)
      REFERENCES `leave_types` (`id`),

  CONSTRAINT `leave_policies_chk_1`
      CHECK ((`allocated_leave` >= 0))
);

-- ------------------------------------------------------



CREATE TABLE `leave_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  `leave_id` bigint NOT NULL,

  `action` varchar(50) NOT NULL,

  `actor_id` bigint NOT NULL,

  `actor_role_id` bigint NOT NULL,

  `created_at`
      timestamp NULL DEFAULT CURRENT_TIMESTAMP,

  `metadata` json DEFAULT NULL,

  PRIMARY KEY (`id`),

  KEY `idx_leave_audit_leave_id` (`leave_id`),
  KEY `idx_leave_audit_actor_id` (`actor_id`),
  KEY `idx_leave_audit_actor_role_id` (`actor_role_id`),

  CONSTRAINT `fk_audit_actor`
      FOREIGN KEY (`actor_id`)
      REFERENCES `users` (`id`),

  CONSTRAINT `fk_audit_actor_role`
      FOREIGN KEY (`actor_role_id`)
      REFERENCES `roles` (`id`),

  CONSTRAINT `fk_audit_leave`
      FOREIGN KEY (`leave_id`)
      REFERENCES `leave_requests` (`id`)
);

-- ------------------------------------------------------



CREATE TABLE `user_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  `user_id` bigint NOT NULL,

  `action` varchar(50) NOT NULL,

  `created_at`
      timestamp NULL DEFAULT CURRENT_TIMESTAMP,

  `metadata` json DEFAULT NULL,

  PRIMARY KEY (`id`),

  KEY `idx_user_audit_user_id` (`user_id`),

  CONSTRAINT `fk_user_audit_user`
      FOREIGN KEY (`user_id`)
      REFERENCES `users` (`id`)
);

-- ------------------------------------------------------


CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,

  `token` varchar(512) NOT NULL,

  `user_id` bigint NOT NULL,

  `expiry_date` timestamp NOT NULL,

  `revoked` tinyint(1)
      NOT NULL DEFAULT '0',

  PRIMARY KEY (`id`),

  UNIQUE KEY `token` (`token`),

  KEY `fk_refresh_token_user` (`user_id`),

  CONSTRAINT `fk_refresh_token_user`
      FOREIGN KEY (`user_id`)
      REFERENCES `users` (`id`)
      ON DELETE CASCADE
);
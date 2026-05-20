CREATE DATABASE IF NOT EXISTS elms;

USE elms;

CREATE TABLE roles (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,

                       PRIMARY KEY (id),
                       UNIQUE KEY uq_roles_name (name)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE departments (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             name VARCHAR(100) NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NULL DEFAULT NULL,

                             PRIMARY KEY (id),
                             UNIQUE KEY uq_departments_name (name)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL,
                       password_hash TEXT NOT NULL,

                       role_id BIGINT NOT NULL,
                       department_id BIGINT DEFAULT NULL,

                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NULL DEFAULT NULL,

                       PRIMARY KEY (id),

                       UNIQUE KEY uq_users_email (email),

                       KEY idx_users_role_id (role_id),
                       KEY idx_users_department_id (department_id),

                       CONSTRAINT fk_user_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles(id),

                       CONSTRAINT fk_user_department
                           FOREIGN KEY (department_id)
                               REFERENCES departments(id)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE leave_types (
                             id BIGINT NOT NULL AUTO_INCREMENT,

                             name VARCHAR(50) NOT NULL,

                             status ENUM(
        'ACTIVE',
        'INACTIVE'
    ) NOT NULL DEFAULT 'ACTIVE',

                             PRIMARY KEY (id),

                             UNIQUE KEY uq_leave_types_name (name)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE leave_policies (
                                id BIGINT NOT NULL AUTO_INCREMENT,

                                year INT NOT NULL,

                                leave_type_id BIGINT NOT NULL,

                                allocated_leave INT NOT NULL,

                                PRIMARY KEY (id),

                                UNIQUE KEY uq_policy (
                                    year,
                                    leave_type_id
                                    ),

                                KEY idx_leave_policies_leave_type_id (
        leave_type_id
    ),

                                CONSTRAINT fk_policy_leave_type
                                    FOREIGN KEY (leave_type_id)
                                        REFERENCES leave_types(id),

                                CONSTRAINT chk_allocated_leave
                                    CHECK (allocated_leave >= 0)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE leave_requests (
                                id BIGINT NOT NULL AUTO_INCREMENT,

                                user_id BIGINT NOT NULL,

                                leave_type_id BIGINT DEFAULT NULL,

                                approver_id BIGINT DEFAULT NULL,

                                start_date DATE DEFAULT NULL,

                                end_date DATE DEFAULT NULL,

                                reason VARCHAR(50) DEFAULT NULL,

                                status VARCHAR(30) NOT NULL,

                                year INT DEFAULT NULL,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                submitted_at TIMESTAMP NULL DEFAULT NULL,

                                decision_at TIMESTAMP NULL DEFAULT NULL,

                                PRIMARY KEY (id),

                                KEY idx_leave_requests_user_id (user_id),

                                KEY idx_leave_requests_leave_type_id (leave_type_id),

                                KEY idx_leave_requests_approver_id (approver_id),

                                CONSTRAINT fk_leave_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id),

                                CONSTRAINT fk_leave_type
                                    FOREIGN KEY (leave_type_id)
                                        REFERENCES leave_types(id),

                                CONSTRAINT fk_leave_approver
                                    FOREIGN KEY (approver_id)
                                        REFERENCES users(id),

                                CONSTRAINT chk_leave_dates
                                    CHECK (end_date >= start_date)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE leave_comments (
                                id BIGINT NOT NULL AUTO_INCREMENT,

                                leave_id BIGINT NOT NULL,

                                user_id BIGINT NOT NULL,

                                message TEXT NOT NULL,

                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                PRIMARY KEY (id),

                                KEY idx_leave_comments_leave_id (leave_id),

                                KEY idx_leave_comments_user_id (user_id),

                                CONSTRAINT fk_comment_leave
                                    FOREIGN KEY (leave_id)
                                        REFERENCES leave_requests(id),

                                CONSTRAINT fk_comment_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE leave_audit_logs (
                                  id BIGINT NOT NULL AUTO_INCREMENT,

                                  leave_id BIGINT NOT NULL,

                                  actor_id BIGINT NOT NULL,

                                  actor_role_id BIGINT NOT NULL,

                                  action VARCHAR(50) NOT NULL,

                                  metadata JSON DEFAULT NULL,

                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  PRIMARY KEY (id),

                                  KEY idx_leave_audit_leave_id (leave_id),

                                  KEY idx_leave_audit_actor_id (actor_id),

                                  KEY idx_leave_audit_actor_role_id (actor_role_id),

                                  CONSTRAINT fk_audit_leave
                                      FOREIGN KEY (leave_id)
                                          REFERENCES leave_requests(id),

                                  CONSTRAINT fk_audit_actor
                                      FOREIGN KEY (actor_id)
                                          REFERENCES users(id),

                                  CONSTRAINT fk_audit_actor_role
                                      FOREIGN KEY (actor_role_id)
                                          REFERENCES roles(id)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE user_audit_logs (
                                 id BIGINT NOT NULL AUTO_INCREMENT,

                                 user_id BIGINT NOT NULL,

                                 action VARCHAR(50) NOT NULL,

                                 metadata JSON DEFAULT NULL,

                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (id),

                                 KEY idx_user_audit_user_id (user_id),

                                 CONSTRAINT fk_user_audit_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE refresh_tokens (
                                id BIGINT NOT NULL AUTO_INCREMENT,

                                token VARCHAR(512) NOT NULL,

                                user_id BIGINT NOT NULL,

                                expiry_date TIMESTAMP NOT NULL,

                                revoked BOOLEAN NOT NULL DEFAULT FALSE,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                PRIMARY KEY (id),

                                UNIQUE KEY uq_refresh_token_token (token),

                                KEY idx_refresh_token_user_id (user_id),

                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;
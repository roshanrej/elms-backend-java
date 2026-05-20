CREATE DATABASE elms;

\c elms;


CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,

                       name VARCHAR(100) NOT NULL UNIQUE
);


CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,

                             name VARCHAR(100) NOT NULL UNIQUE,

                             status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             updated_at TIMESTAMP DEFAULT NULL
);


CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       name VARCHAR(100) NOT NULL,

                       email VARCHAR(150) NOT NULL UNIQUE,

                       password_hash TEXT NOT NULL,

                       role_id BIGINT NOT NULL,

                       department_id BIGINT,

                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP DEFAULT NULL,

                       CONSTRAINT fk_user_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles(id),

                       CONSTRAINT fk_user_department
                           FOREIGN KEY (department_id)
                               REFERENCES departments(id)
);


CREATE TABLE leave_types (
                             id BIGSERIAL PRIMARY KEY,

                             name VARCHAR(50) NOT NULL UNIQUE,

                             status VARCHAR(20)
                                              NOT NULL
                                 DEFAULT 'ACTIVE'
                                 CHECK (
                                     status IN (
                                                'ACTIVE',
                                                'INACTIVE'
                                         )
                                     )
);


CREATE TABLE leave_policies (
                                id BIGSERIAL PRIMARY KEY,

                                year INTEGER NOT NULL,

                                leave_type_id BIGINT NOT NULL,

                                allocated_leave INTEGER NOT NULL
                                    CHECK (
                                        allocated_leave >= 0
                                        ),

                                CONSTRAINT uq_policy
                                    UNIQUE (
                                            year,
                                            leave_type_id
                                        ),

                                CONSTRAINT fk_policy_leave_type
                                    FOREIGN KEY (leave_type_id)
                                        REFERENCES leave_types(id)
);


CREATE TABLE leave_requests (
                                id BIGSERIAL PRIMARY KEY,

                                user_id BIGINT NOT NULL,

                                leave_type_id BIGINT,

                                approver_id BIGINT,

                                start_date DATE,

                                end_date DATE,

                                reason VARCHAR(50),

                                status VARCHAR(30) NOT NULL,

                                year INTEGER,

                                created_at TIMESTAMP
                                    NOT NULL
                                    DEFAULT CURRENT_TIMESTAMP,

                                submitted_at TIMESTAMP,

                                decision_at TIMESTAMP,

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
                                    CHECK (
                                        end_date >= start_date
                                        )
);


CREATE TABLE leave_comments (
                                id BIGSERIAL PRIMARY KEY,

                                leave_id BIGINT NOT NULL,

                                user_id BIGINT NOT NULL,

                                message TEXT NOT NULL,

                                created_at TIMESTAMP
                                    DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_comment_leave
                                    FOREIGN KEY (leave_id)
                                        REFERENCES leave_requests(id),

                                CONSTRAINT fk_comment_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
);


CREATE TABLE leave_audit_logs (
                                  id BIGSERIAL PRIMARY KEY,

                                  leave_id BIGINT NOT NULL,

                                  actor_id BIGINT NOT NULL,

                                  actor_role_id BIGINT NOT NULL,

                                  action VARCHAR(50) NOT NULL,

                                  metadata JSONB,

                                  created_at TIMESTAMP
                                      DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_audit_leave
                                      FOREIGN KEY (leave_id)
                                          REFERENCES leave_requests(id),

                                  CONSTRAINT fk_audit_actor
                                      FOREIGN KEY (actor_id)
                                          REFERENCES users(id),

                                  CONSTRAINT fk_audit_actor_role
                                      FOREIGN KEY (actor_role_id)
                                          REFERENCES roles(id)
);


CREATE TABLE user_audit_logs (
                                 id BIGSERIAL PRIMARY KEY,

                                 user_id BIGINT NOT NULL,

                                 action VARCHAR(50) NOT NULL,

                                 metadata JSONB,

                                 created_at TIMESTAMP
                                     DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_user_audit_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
);


CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,

                                token VARCHAR(512)
                                                      NOT NULL
                                    UNIQUE,

                                user_id BIGINT NOT NULL,

                                expiry_date TIMESTAMP NOT NULL,

                                revoked BOOLEAN
                                                      NOT NULL
                                    DEFAULT FALSE,

                                created_at TIMESTAMP
                                                      NOT NULL
                                    DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);


CREATE INDEX idx_users_role_id
    ON users(role_id);

CREATE INDEX idx_users_department_id
    ON users(department_id);

CREATE INDEX idx_leave_policies_leave_type_id
    ON leave_policies(leave_type_id);

CREATE INDEX idx_leave_requests_user_id
    ON leave_requests(user_id);

CREATE INDEX idx_leave_requests_leave_type_id
    ON leave_requests(leave_type_id);

CREATE INDEX idx_leave_requests_approver_id
    ON leave_requests(approver_id);

CREATE INDEX idx_leave_comments_leave_id
    ON leave_comments(leave_id);

CREATE INDEX idx_leave_comments_user_id
    ON leave_comments(user_id);

CREATE INDEX idx_leave_audit_leave_id
    ON leave_audit_logs(leave_id);

CREATE INDEX idx_leave_audit_actor_id
    ON leave_audit_logs(actor_id);

CREATE INDEX idx_leave_audit_actor_role_id
    ON leave_audit_logs(actor_role_id);

CREATE INDEX idx_user_audit_user_id
    ON user_audit_logs(user_id);

CREATE INDEX idx_refresh_token_user_id
    ON refresh_tokens(user_id);
-- =========================
-- RESET DATABASE (optional)
-- =========================
DROP DATABASE IF EXISTS elms;
CREATE DATABASE elms;
USE elms;

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- =========================
-- DEPARTMENTS
-- =========================
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,

    role_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_department_id ON users(department_id);

-- =========================
-- LEAVE TYPES
-- =========================
CREATE TABLE leave_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL
);

-- =========================
-- LEAVE POLICIES
-- =========================
CREATE TABLE leave_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    allocated_leave INT NOT NULL CHECK (allocated_leave >= 0),

    CONSTRAINT uq_policy UNIQUE (year, leave_type_id),
    CONSTRAINT fk_policy_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE INDEX idx_leave_policies_leave_type_id ON leave_policies(leave_type_id);

-- =========================
-- LEAVE REQUESTS
-- =========================
CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    reason TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    year INT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP NULL,
    decision_at TIMESTAMP NULL,

    approver_id BIGINT NULL,

    CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id),
    CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES users(id),

    CONSTRAINT chk_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_leave_requests_user_id ON leave_requests(user_id);
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests(leave_type_id);
CREATE INDEX idx_leave_requests_approver_id ON leave_requests(approver_id);

-- =========================
-- LEAVE COMMENTS
-- =========================
CREATE TABLE leave_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    leave_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    message TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_leave FOREIGN KEY (leave_id) REFERENCES leave_requests(id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_leave_comments_leave_id ON leave_comments(leave_id);
CREATE INDEX idx_leave_comments_user_id ON leave_comments(user_id);

-- =========================
-- LEAVE AUDIT LOGS
-- =========================
CREATE TABLE leave_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    leave_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,

    actor_id BIGINT NOT NULL,
    actor_role_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    metadata JSON,

    CONSTRAINT fk_audit_leave FOREIGN KEY (leave_id) REFERENCES leave_requests(id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id),
    CONSTRAINT fk_audit_actor_role FOREIGN KEY (actor_role_id) REFERENCES roles(id)
);

CREATE INDEX idx_leave_audit_leave_id ON leave_audit_logs(leave_id);
CREATE INDEX idx_leave_audit_actor_id ON leave_audit_logs(actor_id);
CREATE INDEX idx_leave_audit_actor_role_id ON leave_audit_logs(actor_role_id);

-- =========================
-- USER AUDIT LOGS
-- =========================
CREATE TABLE user_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    metadata JSON,

    CONSTRAINT fk_user_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_audit_user_id ON user_audit_logs(user_id);
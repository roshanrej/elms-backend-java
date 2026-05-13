-- ------------------------------------------------------
CREATE TABLE departments (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT NULL,
  CONSTRAINT uq_departments_name UNIQUE (name)
);

-- ------------------------------------------------------
CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT uq_roles_name UNIQUE (name)
);

-- ------------------------------------------------------
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password_hash TEXT NOT NULL,
  role_id BIGINT NOT NULL,
  department_id BIGINT DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT NULL,
  CONSTRAINT uq_users_email UNIQUE (email),
  CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments (id),
  CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- ------------------------------------------------------
CREATE TABLE leave_types (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- Changed from ENUM to VARCHAR(20)
  CONSTRAINT uq_leave_types_name UNIQUE (name)
);

-- ------------------------------------------------------
CREATE TABLE leave_requests (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  leave_type_id BIGINT DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  reason VARCHAR(50) DEFAULT NULL,
  status VARCHAR(30) NOT NULL,
  year INT DEFAULT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  submitted_at TIMESTAMPTZ DEFAULT NULL,
  decision_at TIMESTAMPTZ DEFAULT NULL,
  approver_id BIGINT DEFAULT NULL,
  CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES users (id),
  CONSTRAINT fk_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types (id),
  CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT chk_dates CHECK (end_date >= start_date)
);

-- ------------------------------------------------------
CREATE TABLE leave_comments (
  id BIGSERIAL PRIMARY KEY,
  leave_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  message TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_leave FOREIGN KEY (leave_id) REFERENCES leave_requests (id),
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ------------------------------------------------------
CREATE TABLE leave_policies (
  id BIGSERIAL PRIMARY KEY,
  year INT NOT NULL,
  leave_type_id BIGINT NOT NULL,
  allocated_leave INT NOT NULL,
  CONSTRAINT uq_policy UNIQUE (year, leave_type_id),
  CONSTRAINT fk_policy_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types (id),
  CONSTRAINT leave_policies_chk_1 CHECK (allocated_leave >= 0)
);

-- ------------------------------------------------------
CREATE TABLE leave_audit_logs (
  id BIGSERIAL PRIMARY KEY,
  leave_id BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL,
  actor_id BIGINT NOT NULL,
  actor_role_id BIGINT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  metadata JSONB DEFAULT NULL,
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id),
  CONSTRAINT fk_audit_actor_role FOREIGN KEY (actor_role_id) REFERENCES roles (id),
  CONSTRAINT fk_audit_leave FOREIGN KEY (leave_id) REFERENCES leave_requests (id)
);

-- ------------------------------------------------------
CREATE TABLE user_audit_logs (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  metadata JSONB DEFAULT NULL,
  CONSTRAINT fk_user_audit_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ------------------------------------------------------
CREATE TABLE refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  token VARCHAR(512) NOT NULL,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMPTZ NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
  CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ------------------------------------------------------
-- Standalone Secondary Indexes
-- ------------------------------------------------------
CREATE INDEX idx_users_role_id ON users (role_id);
CREATE INDEX idx_users_department_id ON users (department_id);
CREATE INDEX idx_leave_requests_user_id ON leave_requests (user_id);
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests (leave_type_id);
CREATE INDEX idx_leave_requests_approver_id ON leave_requests (approver_id);
CREATE INDEX idx_leave_comments_leave_id ON leave_comments (leave_id);
CREATE INDEX idx_leave_comments_user_id ON leave_comments (user_id);
CREATE INDEX idx_leave_policies_leave_type_id ON leave_policies (leave_type_id);
CREATE INDEX idx_leave_audit_leave_id ON leave_audit_logs (leave_id);
CREATE INDEX idx_leave_audit_actor_id ON leave_audit_logs (actor_id);
CREATE INDEX idx_leave_audit_actor_role_id ON leave_audit_logs (actor_role_id);
CREATE INDEX idx_user_audit_user_id ON user_audit_logs (user_id);
CREATE INDEX idx_refresh_token_user ON refresh_tokens (user_id);

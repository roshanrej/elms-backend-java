USE elms;



-- =========================================================
-- ROLES
-- =========================================================

INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('MANAGER'),
    ('EMPLOYEE');



-- =========================================================
-- DEPARTMENTS
-- =========================================================

INSERT INTO departments (
    name,
    status
)
VALUES
    ('HUMAN_RESOURCES', 'ACTIVE'),
    ('ENGINEERING', 'ACTIVE'),
    ('FINANCE', 'ACTIVE'),
    ('OPERATIONS', 'ACTIVE'),
    ('PRODUCT_MANAGEMENT', 'ACTIVE'),
    ('QUALITY_ASSURANCE', 'ACTIVE');



-- =========================================================
-- LEAVE TYPES
-- =========================================================

INSERT INTO leave_types (
    name,
    status
)
VALUES
    ('ANNUAL', 'ACTIVE'),
    ('SICK', 'ACTIVE'),
    ('CASUAL', 'ACTIVE'),
    ('MATERNITY', 'ACTIVE'),
    ('PATERNITY', 'ACTIVE'),
    ('UNPAID', 'ACTIVE'),




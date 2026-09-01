CREATE TABLE auth_users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    failed_login_attempts INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_auth_users_role
        CHECK (role IN ('CUSTOMER', 'ADMIN')),

    CONSTRAINT chk_auth_users_status
        CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED')),

    CONSTRAINT chk_auth_users_failed_login_attempts
        CHECK (failed_login_attempts >= 0)
);

CREATE UNIQUE INDEX ux_auth_users_email_lower
    ON auth_users (LOWER(email));

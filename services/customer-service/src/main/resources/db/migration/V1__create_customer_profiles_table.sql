CREATE TABLE customer_profiles (
    id UUID PRIMARY KEY,
    auth_user_id UUID NOT NULL,

    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(32),
    date_of_birth DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_customer_profiles_auth_user
        UNIQUE (auth_user_id),

    CONSTRAINT chk_customer_profiles_status
        CHECK (
            status IN (
                'ACTIVE',
                'SUSPENDED',
                'CLOSED'
            )
        )
);

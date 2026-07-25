CREATE TABLE IF not EXISTS app_users
(
    id                        bigint generated always AS identity PRIMARY KEY,
    email                     VARCHAR(255) NOT NULL UNIQUE,
    password_hash             VARCHAR(255) NOT NULL,
    first_name                VARCHAR(100) NOT NULL,
    last_name                 VARCHAR(100) NOT NULL,
    ROLE                      VARCHAR(20) NOT NULL,
    active                    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT chk_app_users_role CHECK (ROLE IN ('STUDENT',
                             'TEACHER',
                             'ADMIN'))
    );

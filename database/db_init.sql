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

INSERT INTO app_users
    (
        email,
        password_hash,
        first_name,
        last_name,
        ROLE,
        active
    )
VALUES
    (
     'admin@radegast.com',
     '$2a$10$0zJQX.4sm5g0FI2VB0O31u0xqKJOdzhJ/lFDUkvM.K9u5ICvjAQY2', -- password
     'Radegast',
     'Administrator',
     'ADMIN',
     TRUE
    );

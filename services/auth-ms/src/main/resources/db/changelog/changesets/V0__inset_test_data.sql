-- Вставка тестовых данных
CREATE TABLE IF NOT EXISTS "users"
(
    id       UUID PRIMARY KEY,
    email    VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL,
    role     VARCHAR(50)         NOT NULL
);

INSERT INTO "users" (id, email, password, role)
VALUES ('223e4567-e89b-12d3-a456-426614174006',
        'testuser@test.com',
        '$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu',
        'ADMIN');

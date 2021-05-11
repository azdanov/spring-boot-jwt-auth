CREATE TABLE IF NOT EXISTS hibernate_sequence
(
    next_val BIGINT NULL
);
INSERT INTO hibernate_sequence (next_val) VALUES (0);

CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT       NOT NULL
        PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR(120) NULL UNIQUE,
    password VARCHAR(200) NULL,
    username VARCHAR(40)  NULL
);


CREATE TABLE IF NOT EXISTS roles
(
    id   BIGINT      NOT NULL
        PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NULL UNIQUE
);
-- Defaults
INSERT INTO roles(name)
VALUES ('ROLE_USER');
INSERT INTO roles(name)
VALUES ('ROLE_MODERATOR');
INSERT INTO roles(name)
VALUES ('ROLE_ADMIN');


CREATE TABLE IF NOT EXISTS users_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_roles
        FOREIGN KEY (role_id) REFERENCES roles (id)
);

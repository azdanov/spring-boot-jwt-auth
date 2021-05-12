CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    name VARCHAR(50) NULL UNIQUE
);

INSERT INTO roles(name)
VALUES ('ROLE_USER');
INSERT INTO roles(name)
VALUES ('ROLE_MODERATOR');
INSERT INTO roles(name)
VALUES ('ROLE_ADMIN');

CREATE TABLE users
(
    id       BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    email    VARCHAR(120) NULL UNIQUE,
    password VARCHAR(200) NULL,
    username VARCHAR(40)  NULL UNIQUE
);

CREATE TABLE refreshtoken
(
    id          BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    expiry_date DATETIME(6)  NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NULL,
    CONSTRAINT fk_refreshtoken_users
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE users_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_users
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_users_roles_roles
        FOREIGN KEY (role_id) REFERENCES roles (id)
);


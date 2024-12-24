--liquibase formatted sql
--changeset author:owpk dbms:postgresql

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    middle_name VARCHAR(255),
    last_name VARCHAR(255),
    login VARCHAR(255)
);

CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE user_role (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id)
);

ALTER TABLE user_role
    ADD FOREIGN KEY (user_id) REFERENCES "user"(id);

ALTER TABLE user_role
    ADD FOREIGN KEY (role_id) REFERENCES role(id);

INSERT INTO role ("id", "name") VALUES (1, 'ROLE_USER');
INSERT INTO role ("id", "name") VALUES (2, 'ROLE_ADM');

INSERT INTO "user" ("id", "name", "last_name", "middle_name", "login") VALUES (1, 'Igor', 'Abobenko', 'Makadamov', 'IgroLoh');

INSERT INTO user_role ("user_id", "role_id") VALUES (1, 1);
INSERT INTO user_role ("user_id", "role_id") VALUES (1, 2);
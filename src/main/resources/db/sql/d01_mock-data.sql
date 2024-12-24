--liquibase formatted sql
--changeset author:owpk dbms:postgresql

INSERT INTO role ("id", "name") VALUES (1, 'ROLE_USER');
INSERT INTO role ("id", "name") VALUES (2, 'ROLE_ADM');

INSERT INTO "user" ("id", "name", "last_name", "middle_name", "login") VALUES (1, 'Igor', 'Abobenko', 'Makadamov', 'IgroLoh');

INSERT INTO "user_role" ("user_id", "role_id") VALUES (1, 1);
INSERT INTO "user_role" ("user_id", "role_id") VALUES (1, 2);

INSERT INTO "service_info" ("id", "service_name", "service_owner_id", "editable", "service_description", "record_limit", "time_duration")
    VALUES (1, 'test_resnichkee', 1, true, 'test', 1, 1);
INSERT INTO "record" ("id", "service_info_id", "record_owner_id", "is_public", "limit", "time_from", "time_to") VALUES (1, 1, 1, false, 1, 123123, 1231500);
INSERT INTO "record" ("id", "service_info_id", "record_owner_id", "is_public", "limit", "time_from", "time_to") VALUES (2, 1, 1, true, 1, 123500, 123600);
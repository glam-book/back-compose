--liquibase formatted sql
--changeset author:owpk dbms:postgresql

CREATE TABLE "user" (
  "id" bigserial PRIMARY KEY,
  "name" varchar,
  "last_name" varchar,
  "middle_name" varchar,
  "login" varchar
);

CREATE TABLE "telegram_user" (
  "user_id" bigint,
  "telegram_id" varchar,
  PRIMARY KEY ("user_id", "telegram_id")
);

CREATE TABLE "user_role" (
  "user_id" bigint,
  "role_id" bigint,
  PRIMARY KEY ("user_id", "role_id")
);

CREATE TABLE "role" (
  "id" bigserial PRIMARY KEY,
  "name" varchar
);

CREATE TABLE "service_info" (
  "id" bigserial PRIMARY KEY,
  "service_owner_id" bigint,
  "service_name" varchar(75),
  "editable" boolean,
  "service_description" text,
  "record_limit" int,
  "time_duration" int
);

CREATE TABLE "record" (
  "id" bigserial PRIMARY KEY,
  "service_info_id" bigint,
  "record_owner_id" bigint,
  "is_public" boolean,
  "limit" int,
  "time_from" bigint,
  "time_to" bigint
);

CREATE TABLE "record_pending" (
  "record_id" bigint,
  "client_id" bigint,
  "request_time" bigint,
  "confirmed" boolean,
  PRIMARY KEY ("client_id", "record_id")
);

CREATE UNIQUE INDEX ON "service_info" ("id", "service_owner_id");

CREATE INDEX ON "record" ("record_owner_id", "service_info_id");

ALTER TABLE "telegram_user" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id");

ALTER TABLE "user_role" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id");

ALTER TABLE "user_role" ADD FOREIGN KEY ("role_id") REFERENCES "role" ("id");

ALTER TABLE "service_info" ADD FOREIGN KEY ("service_owner_id") REFERENCES "user" ("id");

ALTER TABLE "record" ADD FOREIGN KEY ("service_info_id") REFERENCES "service_info" ("id");

ALTER TABLE "record" ADD FOREIGN KEY ("record_owner_id") REFERENCES "user" ("id");

ALTER TABLE "record_pending" ADD FOREIGN KEY ("record_id") REFERENCES "record" ("id");

ALTER TABLE "record_pending" ADD FOREIGN KEY ("client_id") REFERENCES "user" ("id");


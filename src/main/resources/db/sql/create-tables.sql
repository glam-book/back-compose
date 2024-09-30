CREATE TABLE "inner_user" (
  "id" bigserial PRIMARY KEY,
  "name" varchar(255),
  "last_name" varchar(255),
  "middle_name" varchar(255),
  "login" varchar(255)
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

CREATE TABLE "service_owner_info" (
  "id" bigserial PRIMARY KEY,
  "service_owner_id" bigint,
  "service_name" varchar(75),
  "service_description" text,
  "record_limit" int,
  "time_duration" int
);

CREATE TABLE "record" (
  "id" bigserial PRIMARY KEY,
  "service_info_id" bigint,
  "record_owner_id" bigint,
  "is_public" boolean,
  "time_from" timestamp,
  "time_to" timestamp
);

CREATE TABLE "record_pending" (
  "service_info_id" bigint,
  "record_id" bigint,
  "request_time" timestamp,
  "confirmed" boolean,
  PRIMARY KEY ("service_info_id", "record_id")
);

CREATE UNIQUE INDEX ON "service_owner_info" ("id", "service_owner_id");

CREATE INDEX ON "record" ("record_owner_id", "service_info_id");

ALTER TABLE "telegram_user" ADD FOREIGN KEY ("user_id") REFERENCES "inner_user" ("id");

ALTER TABLE "user_role" ADD FOREIGN KEY ("user_id") REFERENCES "inner_user" ("id");

ALTER TABLE "user_role" ADD FOREIGN KEY ("role_id") REFERENCES "role" ("id");

ALTER TABLE "service_owner_info" ADD FOREIGN KEY ("service_owner_id") REFERENCES "inner_user" ("id");

ALTER TABLE "record" ADD FOREIGN KEY ("service_info_id") REFERENCES "service_owner_info" ("id");

ALTER TABLE "record" ADD FOREIGN KEY ("record_owner_id") REFERENCES "inner_user" ("id");

ALTER TABLE "record_pending" ADD FOREIGN KEY ("service_info_id") REFERENCES "service_owner_info" ("id");

ALTER TABLE "record_pending" ADD FOREIGN KEY ("record_id") REFERENCES "record" ("id");

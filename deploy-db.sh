#!/bin/sh

CONTAINER="tantal-db"

DB_NAME="${1:-tl_back}"
DB_PORT="${2:-5599}"
DB_PASS="${3:-postgres}"
DB_USER="${4:-postgres}"


echo "Deploying database:"
echo "Db name: $DB_NAME"
echo "Db port: $DB_PORT"
echo "Db pass: $DB_PASS"
echo "Db user: $DB_USER"

docker stop $CONTAINER 2> /dev/null

docker run --name $CONTAINER \
    -p $DB_PORT:5432 \
    -e POSTGRES_PASSWORD=$DB_PASS \
    -e POSTGRES_USER=$DB_USER \
    -e POSTGRES_DB=$DB_NAME \
    -d --rm postgres:17

    


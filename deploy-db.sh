#!/bin/sh

CONTAINER="tantal-db"

export DB_NAME="${1:-tl_back}"
export DB_PORT="${2:-5599}"
export DB_PASSWORD="${3:-postgres}"
export DB_USER="${4:-postgres}"
export DB_HOST="${5:-localhost}"

echo "Deploying database:"
echo "Db name: $DB_NAME"
echo "Db port: $DB_PORT"
echo "Db pass: $DB_PASSWORD"
echo "Db user: $DB_USER"

docker stop $CONTAINER 2> /dev/null

docker run --name $CONTAINER \
    -p $DB_PORT:5432 \
    -e POSTGRES_PASSWORD=$DB_PASSWORD \
    -e POSTGRES_USER=$DB_USER \
    -e POSTGRES_DB=$DB_NAME \
    -d --rm postgres:17

./gradlew update
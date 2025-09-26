#!/bin/sh

CONTAINER="tantal-db"

export TL_DB_NAME="${1:-tl_back}"
export TL_DB_PORT="${2:-5599}"
export TL_DB_PASSWORD="${3:-postgres}"
export TL_DB_USER="${4:-postgres}"
export TL_DB_HOST="${5:-localhost}"

echo "Deploying database:"
echo "Db name: $TL_DB_NAME"
echo "Db port: $TL_DB_PORT"
echo "Db pass: $TL_DB_PASSWORD"
echo "Db user: $TL_DB_USER"

docker stop $CONTAINER 2> /dev/null

docker run --name $CONTAINER \
    -p $TL_DB_PORT:5432 \
    -e POSTGRES_PASSWORD=$TL_DB_PASSWORD \
    -e POSTGRES_USER=$TL_DB_USER \
    -e POSTGRES_DB=$TL_DB_NAME \
    -d --rm postgres:17

sleep 3

./gradlew update

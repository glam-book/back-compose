#!/bin/sh

export DB_NAME="${1:-tl_back}"
export DB_PORT="${2:-5599}"
export DB_PASSWORD="${3:-postgres}"
export DB_USER="${4:-postgres}"
export DB_HOST="${5:-localhost}"

if [ -f "./credentials" ]; then
    export "$(xargs < ./credentials)"
fi

docker compose -f ../docker/docker-compose-middleware.yml down
docker compose -f ../docker/docker-compose-middleware.yml up -d

sleep 5

cd ..
./gradlew update
./gradlew clean bootRun --args="--spring.profiles.active=dev"

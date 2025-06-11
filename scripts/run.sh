#!/bin/sh

export DB_NAME="${1:-tl_back}"
export DB_PORT="${2:-5599}"
export DB_PASSWORD="${3:-postgres}"
export DB_USER="${4:-postgres}"
export DB_HOST="${5:-localhost}"

if [ -f "./credentials" ]; then
    export "$(xargs < ./credentials)"
fi

./deploy-db.sh
./deploy-cache.sh
sleep 5
./gradlew update
./gradlew clean bootRun --args="--spring.profiles.active=dev"

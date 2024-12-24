#!/bin/sh

./deploy-db.sh
./gradlew update
./gradlew clean bootRun

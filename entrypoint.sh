#!/bin/bash
set -e
./gradlew update
exec "$@"
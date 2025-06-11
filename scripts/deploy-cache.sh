docker run -d --name redis -p 6379:6379 arm64v8/redis:alpine
docker run -d --name redis-commander -p 8081:8081 -e REDIS_HOSTS=local:redis://host.docker.internal:6379 rediscommander/redis-commander

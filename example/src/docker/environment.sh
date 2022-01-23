docker pull postgres
docker pull redis

docker network create --driver bridge graphql-provider-example

docker run \
    -d \
    --restart=always \
    --name graphql-provider-example-postgres \
    --network graphql-provider-example \
    --network-alias postgres \
    -e POSTGRES_DB=db \
    -e POSTGRES_USER=sa \
    -e POSTGRES_PASSWORD=123456 \
    -p 5432:5432 \
    postgres

docker run \
    -d \
    --restart=always \
    --name graphql-provider-example-redis \
    --network graphql-provider-example \
    --network-alias redis \
    -p 6379:6379 \
    redis



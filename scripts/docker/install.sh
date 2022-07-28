P_DOCKER_COMPOSE_PATH=/usr/bin/docker-compose

curl https://get.docker.com | sh
curl -L https://github.com/docker/compose/releases/download/v2.6.0/docker-compose-linux-x86_64 -o ${P_DOCKER_COMPOSE_PATH}

chmod +x $P_DOCKER_COMPOSE_PATH

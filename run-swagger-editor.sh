#!/bin/bash
docker pull swaggerapi/swagger-editor
PORT=8900
docker run -d --name swagger-editor -p $PORT:8080 swaggerapi/swagger-editor
echo "Started swagger-editor on port $PORT. Press CTRL-C to stop!"
#trap ctrl_c SIGINT

function ctrl_c() {
  docker stop swagger-editor
  docker rm swagger-editor
  exit 0
}

while :
do
  trap ctrl_c SIGINT
done


#!/usr/bin/env bash
# Settings.
PASS=$1
MOUNT_PATH="$(pwd)/data"

printf "\nMounting path: %s\n\n" "$MOUNT_PATH"

# Check if container exists.
VOOBLY_CONTAINER=$(docker ps -al | grep "voobly-postgres")

if [ -z "$VOOBLY_CONTAINER" ]; then
  # Variable empty, run new container.
  docker run -it --name voobly-postgres -p 5432:5432 -v "$MOUNT_PATH"/voobly-postgres:/voobly-postgres -e POSTGRES_PASSWORD="$PASS" postgres:latest
else
  # Container exists.
  if [[ $VOOBLY_CONTAINER == *Exited* ]]; then
    # Stopped, restart.
    docker start "voobly-postgres"
  else
    # Already running.
    docker container ls
  fi
fi

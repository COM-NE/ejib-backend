#!/bin/bash

set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/ejib-backend}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
ENV_FILE="${ENV_FILE:-.env.prod}"
APP_SERVICE="${APP_SERVICE:-app}"
APP_CONTAINER="${APP_CONTAINER:-ejib-api}"
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8080/actuator/health}"
HEALTH_MAX_RETRIES="${HEALTH_MAX_RETRIES:-12}"
HEALTH_RETRY_INTERVAL="${HEALTH_RETRY_INTERVAL:-5}"

cd "$APP_DIR"

if [ $# -ge 1 ] && [ -n "$1" ]; then
  export IMAGE_TAG="$1"
  echo "Deploy image tag: $IMAGE_TAG"
fi

echo "[1/5] Validate required files"
test -f "$COMPOSE_FILE"
test -f "$ENV_FILE"
test -f "./secrets/google-vision.json"

echo "[2/5] Validate docker compose config"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" config > /dev/null

echo "[3/5] Pull latest image"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" pull "$APP_SERVICE"

echo "[4/5] Restart application"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --remove-orphans "$APP_SERVICE"

echo "[5/5] Check app health"
for i in $(seq 1 "$HEALTH_MAX_RETRIES"); do
  if curl -fsS "$HEALTH_URL" > /dev/null; then
    echo "Health check passed"
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps "$APP_SERVICE"
    docker image prune -f > /dev/null
    echo "Deploy completed successfully"
    exit 0
  fi

  echo "Health check failed. retry=$i/$HEALTH_MAX_RETRIES"

  if [ "$i" -eq "$HEALTH_MAX_RETRIES" ]; then
    echo "Health check failed after $HEALTH_MAX_RETRIES attempts" >&2
    echo "Recent container logs:" >&2
    docker logs --tail=150 "$APP_CONTAINER" >&2 || true
    exit 1
  fi

  sleep "$HEALTH_RETRY_INTERVAL"
done

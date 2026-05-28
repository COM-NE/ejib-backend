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

echo "[1/5] Pull latest source"
git pull origin main

echo "[2/5] Validate docker compose config"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" config > /dev/null

echo "[3/5] Build and restart application"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --build "$APP_SERVICE"

echo "[4/5] Check app health"
for i in $(seq 1 "$HEALTH_MAX_RETRIES"); do
  if curl -fsS "$HEALTH_URL" > /dev/null; then
    echo "Health check passed"
    break
  fi

  echo "Health check failed. retry=$i/$HEALTH_MAX_RETRIES"

  if [ "$i" -eq "$HEALTH_MAX_RETRIES" ]; then
    echo "Health check failed after $HEALTH_MAX_RETRIES attempts" >&2
    echo "Recent container logs:" >&2
    docker logs --tail=100 "$APP_CONTAINER" >&2 || true
    exit 1
  fi

  sleep "$HEALTH_RETRY_INTERVAL"
done

echo "[5/5] Cleanup unused Docker images"
docker image prune -f

echo "Deploy completed successfully"
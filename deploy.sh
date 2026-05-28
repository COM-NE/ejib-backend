#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/ejib-backend}"
BRANCH="${BRANCH:-main}"

cd "$APP_DIR"

echo "[1/5] Pull latest source"
git fetch origin "$BRANCH"
git reset --hard "origin/$BRANCH"

echo "[2/5] Build and restart container"
docker compose -f docker-compose.prod.yml up -d --build

echo "[3/5] Check container status"
docker compose -f docker-compose.prod.yml ps

echo "[4/5] Check app health"
sleep 5
curl -fsS http://127.0.0.1:8080/actuator/health || true

echo "[5/5] Remove unused docker images"
docker image prune -f

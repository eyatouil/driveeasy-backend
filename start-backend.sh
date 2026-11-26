#!/usr/bin/env bash
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "=== Démarrage DriveEasy (Sans Docker) ==="
echo "[1/4] Service Registry (8761)..."
(cd "$ROOT/service-registry" && mvn spring-boot:run) &
sleep 25

echo "[2/4] Config Server (8888)..."
(cd "$ROOT/config-server" && mvn spring-boot:run) &
sleep 20

echo "[3/4] Microservices métier..."
(cd "$ROOT/vehicule-service" && mvn spring-boot:run) &
(cd "$ROOT/client-service" && mvn spring-boot:run) &
(cd "$ROOT/location-service" && mvn spring-boot:run) &
sleep 25

echo "[4/4] API Gateway (8080)..."
(cd "$ROOT/api-gateway" && mvn spring-boot:run) &

echo "Prêt. Eureka : http://localhost:8761 - Gateway : http://localhost:8080"
wait

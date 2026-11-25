#!/usr/bin/env bash
# Linux / macOS : démarre toute la plateforme (JDK 21, Maven, Docker requis)
ROOT="$(cd "$(dirname "$0")" && pwd)"
docker compose -f "$ROOT/docker-compose.yml" up -d
run() { (cd "$ROOT/$1" && mvn -q spring-boot:run > "$ROOT/$1.log" 2>&1 &) ; }
run service-registry; sleep 15
run config-server;    sleep 15
run vehicule-service; run client-service; run location-service; sleep 20
run api-gateway
echo "Eureka http://localhost:8761 | Zipkin http://localhost:9411 | Gateway http://localhost:8080"

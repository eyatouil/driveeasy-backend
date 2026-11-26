@echo off
chcp 65001 > nul
REM Demarrage de toute la plateforme DriveEasy (Zipkin via Docker).
set "ROOT=%~dp0"

echo ========================================================
echo        DEMARRAGE DE LA PLATEFORME DRIVE-EASY
echo ========================================================

echo [0/5] Zipkin via Docker (port 9411)...
cd /d "%ROOT%"
docker compose up -d
if errorlevel 1 (
    echo ERREUR : Docker n'est pas demarre. Lancez Docker Desktop puis relancez ce script.
    pause
    exit /b 1
)
timeout /t 10 /nobreak > nul

echo [1/5] Service Registry Eureka (port 8761)...
start "Eureka [8761]" cmd /k "cd /d %ROOT%service-registry && mvn spring-boot:run"
echo En attente de l'initialisation d'Eureka (25s)...
timeout /t 25 /nobreak > nul

echo [2/5] Config Server (port 8888)...
start "Config Server [8888]" cmd /k "cd /d %ROOT%config-server && mvn spring-boot:run"
echo En attente de l'initialisation du Config Server (20s)...
timeout /t 20 /nobreak > nul

echo [3/5] Microservices metier (ports 8081, 8082, 8083)...
start "Vehicule-Service [8081]" cmd /k "cd /d %ROOT%vehicule-service && mvn spring-boot:run"
start "Client-Service [8082]"   cmd /k "cd /d %ROOT%client-service && mvn spring-boot:run"
start "Location-Service [8083]" cmd /k "cd /d %ROOT%location-service && mvn spring-boot:run"
echo En attente des 3 microservices (25s)...
timeout /t 25 /nobreak > nul

echo [4/5] API Gateway (port 8080)...
start "API-Gateway [8080]" cmd /k "cd /d %ROOT%api-gateway && mvn spring-boot:run"

echo ========================================================
echo  TOUS LES SERVICES SONT EN COURS D'EXECUTION !
echo  - Eureka Dashboard : http://localhost:8761
echo  - Zipkin Traces    : http://localhost:9411
echo  - Config Server    : http://localhost:8888/location-service/dev
echo  - API Gateway      : http://localhost:8080/api/vehicules
echo ========================================================
pause
@echo off
chcp 65001 > nul
REM DÃ©marre toute la plateforme. PrÃ©requis : JDK 21 et Maven dans le PATH, Docker dÃ©marrÃ©.
set "ROOT=%~dp0"
echo [0] Zipkin (Docker)...
pushd "%ROOT%" & docker compose up -d & popd

echo [1/5] Service Registry (8761)...
start "Eureka [8761]" cmd /k "cd /d %ROOT%service-registry && mvn spring-boot:run"
timeout /t 40 /nobreak > nul

echo [2/5] Config Server (8888)...
start "Config Server [8888]" cmd /k "cd /d %ROOT%config-server && mvn spring-boot:run"
timeout /t 40 /nobreak > nul

echo [3/5] Microservices metier...
start "Vehicule [8081]"  cmd /k "cd /d %ROOT%vehicule-service && mvn spring-boot:run"
start "Client [8082]"    cmd /k "cd /d %ROOT%client-service && mvn spring-boot:run"
start "Location [8083]"  cmd /k "cd /d %ROOT%location-service && mvn spring-boot:run"
timeout /t 45 /nobreak > nul

echo [4/5] API Gateway (8080)...
start "Gateway [8080]" cmd /k "cd /d %ROOT%api-gateway && mvn spring-boot:run"
echo Eureka : http://localhost:8761 - Zipkin : http://localhost:9411 - Gateway : http://localhost:8080


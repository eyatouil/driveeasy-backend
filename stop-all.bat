@echo off
taskkill /F /IM java.exe 2>nul
pushd "%~dp0" & docker compose down 2>nul & popd
echo Tous les services sont arretes.

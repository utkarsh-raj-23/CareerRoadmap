@echo off
echo ====================================================
echo      Starting CareerRoadmap Fullstack Services
echo ====================================================
echo 1. Launching AI Service (FastAPI) on port 8000...
start "CareerRoadmap - AI Service (8000)" cmd /k "%~dp0start-ai-service.bat"

echo 2. Launching Backend (Spring Boot) on port 8080...
start "CareerRoadmap - Backend (8080)" cmd /k "%~dp0start-backend.bat"

echo.
echo Both services have been launched in separate terminal windows!
echo - AI Service: http://localhost:8000 (Swagger docs: http://localhost:8000/docs)
echo - Backend:    http://localhost:8080
echo ====================================================
timeout /t 5

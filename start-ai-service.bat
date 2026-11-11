@echo off
echo Starting CareerRoadmap AI Service on port 8000...
cd /d "%~dp0ai-service"
call .venv\Scripts\activate.bat
uvicorn main:app --host 127.0.0.1 --port 8000 --reload
pause

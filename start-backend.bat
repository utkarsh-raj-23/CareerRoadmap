@echo off
echo Starting CareerRoadmap Spring Boot Backend on port 8080...
cd /d "%~dp0backend\careerroadmap"
call mvnw.cmd spring-boot:run
pause

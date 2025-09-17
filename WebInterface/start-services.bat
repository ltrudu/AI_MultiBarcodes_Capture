@echo off
REM AI MultiBarcode Capture - Unified Container Startup Script for Windows
REM This script builds and starts the unified multibarcode-webinterface container

echo AI MultiBarcode Capture - Unified Container Startup
echo ======================================================

REM Load environment variables from .env file
if exist .env (
    for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
        if not "%%a"=="" if not "%%b"=="" (
            set %%a=%%b
        )
    )
    echo [OK] Loaded configuration from .env file
) else (
    echo [WARNING] .env file not found, using default values
)

REM Set defaults
if "%WEB_PORT%"=="" set WEB_PORT=3500
if "%EXPOSE_PHPMYADMIN%"=="" set EXPOSE_PHPMYADMIN=false
if "%EXPOSE_MYSQL%"=="" set EXPOSE_MYSQL=false

echo.
echo Configuration Summary:
echo - Container Image: multibarcode-webinterface
echo - Web Interface Port: %WEB_PORT%
if "%EXPOSE_PHPMYADMIN%"=="true" (
    echo - phpMyAdmin Access: Enabled at /phpmyadmin
) else (
    echo - phpMyAdmin Access: Disabled
)
echo - MySQL Direct Access: Internal only
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running or not accessible
    echo    Please start Docker Desktop and try again
    pause
    exit /b 1
)

REM Build the unified image
echo Building unified Docker image...
docker build -t multibarcode-webinterface:latest .

if errorlevel 1 (
    echo [ERROR] Failed to build Docker image
    pause
    exit /b 1
)

echo [OK] Docker image built successfully

REM Stop existing container if running
echo Stopping existing container (if any)...
docker-compose down

REM Start the unified container
echo Starting unified container...
docker-compose up -d

if errorlevel 1 (
    echo [ERROR] Failed to start container
    pause
    exit /b 1
)

echo.
echo [OK] Container started successfully!
echo.

REM Wait for services to be ready
echo Waiting for services to be ready...
timeout /t 10 >nul

REM Check if container is running
docker ps -q -f name=multibarcode-webinterface >nul
if errorlevel 1 (
    echo [ERROR] Container is not running
    pause
    exit /b 1
)

echo.
echo AI MultiBarcode Capture is now running!
echo.
echo Access Points:
echo - Web Management System: http://localhost:%WEB_PORT%

if "%EXPOSE_PHPMYADMIN%"=="true" (
    echo - phpMyAdmin: http://localhost:%WEB_PORT%/phpmyadmin
)

echo.
echo Management Commands:
echo - View logs: docker logs multibarcode-webinterface
echo - Stop container: docker-compose down
echo - Restart container: docker restart multibarcode-webinterface
echo.
echo Android App Configuration:
echo - Endpoint URL: http://YOUR_IP:%WEB_PORT%/api/barcodes.php
echo - Example: http://192.168.1.100:%WEB_PORT%/api/barcodes.php
echo.
echo Everything is ready! You can now start scanning with your Android app.
echo.
pause
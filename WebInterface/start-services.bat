@echo off
setlocal enabledelayedexpansion

echo AI MultiBarcode Capture - Unified Container Startup
echo ======================================================

REM Set default values
set WEB_PORT=3500
set EXPOSE_PHPMYADMIN=false
set EXPOSE_MYSQL=false
set MYSQL_ROOT_PASSWORD=root_password
set DB_NAME=barcode_wms
set DB_USER=wms_user
set DB_PASS=wms_password

REM Load environment variables from .env file (simple approach)
if exist .env (
    echo [OK] Loading configuration from .env file
    call :load_env
) else (
    echo [WARNING] .env file not found, using default values
)

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
    echo Please start Docker Desktop and try again
    pause
    exit /b 1
)

REM Clean up existing containers and images
echo Cleaning up existing containers and images...
docker stop multibarcode-webinterface >nul 2>&1
docker rm multibarcode-webinterface >nul 2>&1
docker volume rm multibarcode_mysql_data >nul 2>&1
docker rmi -f multibarcode-webinterface:latest >nul 2>&1

echo [OK] Cleanup completed

REM Build the image
echo Building Docker image...
docker build -t multibarcode-webinterface:latest .

if errorlevel 1 (
    echo [ERROR] Failed to build Docker image
    pause
    exit /b 1
)

echo [OK] Docker image built successfully

REM Start the container
echo Starting container...
docker run -d ^
    --name multibarcode-webinterface ^
    -p %WEB_PORT%:%WEB_PORT% ^
    -e MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD% ^
    -e MYSQL_DATABASE=%DB_NAME% ^
    -e MYSQL_USER=%DB_USER% ^
    -e MYSQL_PASSWORD=%DB_PASS% ^
    -e DB_HOST=127.0.0.1 ^
    -e DB_NAME=%DB_NAME% ^
    -e DB_USER=%DB_USER% ^
    -e DB_PASS=%DB_PASS% ^
    -e WEB_PORT=%WEB_PORT% ^
    -e EXPOSE_PHPMYADMIN=%EXPOSE_PHPMYADMIN% ^
    -e EXPOSE_MYSQL=%EXPOSE_MYSQL% ^
    -v multibarcode_mysql_data:/var/lib/mysql ^
    --restart unless-stopped ^
    multibarcode-webinterface:latest

if errorlevel 1 (
    echo [ERROR] Failed to start container
    pause
    exit /b 1
)

echo.
echo [OK] Container started successfully!
echo.

REM Wait for services
echo Waiting for services to be ready...
timeout /t 10 /nobreak >nul

REM Check container status
for /f %%i in ('docker ps -q -f name=multibarcode-webinterface 2^>nul') do set CONTAINER_ID=%%i
if "%CONTAINER_ID%"=="" (
    echo [ERROR] Container is not running
    docker logs multibarcode-webinterface --tail 20
    pause
    exit /b 1
)

echo [OK] Container is running with ID: %CONTAINER_ID%

echo.
echo AI MultiBarcode Capture is now running!
echo.
echo Access Points:
echo - Web Management System: http://localhost:%WEB_PORT%
echo.
echo Management Commands:
echo - View logs: docker logs multibarcode-webinterface
echo - Stop container: docker stop multibarcode-webinterface
echo - Remove container: docker rm multibarcode-webinterface
echo.
echo Android App Configuration:
echo - Endpoint URL: http://YOUR_IP:%WEB_PORT%/api/barcodes.php
echo - Example: http://192.168.1.100:%WEB_PORT%/api/barcodes.php
echo.
pause
exit /b 0

:load_env
REM Simple environment loader that handles key=value pairs
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if not "%%a"=="" (
        set line=%%a
        if not "!line:~0,1!"=="#" (
            if not "%%b"=="" set %%a=%%b
        )
    )
)
goto :eof
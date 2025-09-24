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

REM Check for SSL certificates and generate if needed
echo Checking SSL certificates...
if not exist "ssl" (
    echo [INFO] SSL directory does not exist, creating...
    mkdir ssl
)

set MISSING_CERTS=false
if not exist "ssl\wms_ca.crt" set MISSING_CERTS=true
if not exist "ssl\wms.crt" set MISSING_CERTS=true
if not exist "ssl\wms.key" set MISSING_CERTS=true
if not exist "ssl\android_ca_system.pem" set MISSING_CERTS=true

if "%MISSING_CERTS%"=="true" (
    echo [INFO] SSL certificates missing, generating certificates...
    call create-certificates.bat
    if errorlevel 1 (
        echo [ERROR] Failed to generate SSL certificates
        echo Please check the certificate generation script and try again
        pause
        exit /b 1
    )
    echo [OK] SSL certificates generated successfully
) else (
    echo [OK] SSL certificates found
)

REM Copy CA certificates to web directory for download
echo [INFO] Copying CA certificates to web directory...
if not exist "src\certificates" mkdir "src\certificates"
copy "ssl\wms_ca.crt" "src\certificates\" >nul 2>&1
copy "ssl\android_ca_system.pem" "src\certificates\" >nul 2>&1
echo [OK] CA certificates copied to web directory

REM Verify all required certificates exist
set CERT_ERROR=false
if not exist "ssl\wms_ca.crt" (
    echo [ERROR] Missing CA certificate: ssl\wms_ca.crt
    set CERT_ERROR=true
)
if not exist "ssl\wms.crt" (
    echo [ERROR] Missing server certificate: ssl\wms.crt
    set CERT_ERROR=true
)
if not exist "ssl\wms.key" (
    echo [ERROR] Missing server private key: ssl\wms.key
    set CERT_ERROR=true
)
if not exist "ssl\android_ca_system.pem" (
    echo [ERROR] Missing Android system certificate: ssl\android_ca_system.pem
    set CERT_ERROR=true
)

if "%CERT_ERROR%"=="true" (
    echo [ERROR] Certificate validation failed - required certificates are missing
    echo Please run create-certificates.bat manually to generate certificates
    pause
    exit /b 1
)

echo [OK] All required SSL certificates are present
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running or not accessible
    echo Please start Docker Desktop and try again
    pause
    exit /b 1
)

REM Clean up existing containers and images (project-specific only)
echo Cleaning up existing project containers and images...
docker stop multibarcode-webinterface >nul 2>&1
docker rm multibarcode-webinterface >nul 2>&1
docker volume rm multibarcode_mysql_data >nul 2>&1

REM Safely remove only unused multibarcode-webinterface images (without force flag)
docker rmi multibarcode-webinterface:latest >nul 2>&1
REM If image is in use, it will be safely skipped without -f flag

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

REM Get host IP address for proper Android connectivity
echo Detecting host IP address...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "192.168"') do (
    set HOST_IP_RAW=%%a
    goto :found_ip
)
:found_ip
REM Remove leading spaces
for /f "tokens=*" %%a in ("%HOST_IP_RAW%") do set HOST_IP=%%a

if "%HOST_IP%"=="" (
    REM Fallback: try to get any private IP
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "10\."') do (
        set HOST_IP_RAW=%%a
        goto :found_fallback_ip
    )
    :found_fallback_ip
    for /f "tokens=*" %%a in ("%HOST_IP_RAW%") do set HOST_IP=%%a
)

if "%HOST_IP%"=="" (
    echo [WARNING] Could not detect host IP automatically, using localhost
    set HOST_IP=127.0.0.1
) else (
    echo [OK] Detected host IP: %HOST_IP%
)

echo [INFO] IP addresses will be detected automatically by the website

REM Start the container
echo Starting container with SSL certificates...
docker run -d ^
    --name multibarcode-webinterface ^
    -p %WEB_PORT%:%WEB_PORT% ^
    -p 3543:3543 ^
    -e MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD% ^
    -e MYSQL_DATABASE=%DB_NAME% ^
    -e MYSQL_USER=%DB_USER% ^
    -e MYSQL_PASSWORD=%DB_PASS% ^
    -e DB_HOST=127.0.0.1 ^
    -e DB_NAME=%DB_NAME% ^
    -e DB_USER=%DB_USER% ^
    -e DB_PASS=%DB_PASS% ^
    -e WEB_PORT=%WEB_PORT% ^
    -e HOST_IP=%HOST_IP% ^
    -e EXPOSE_PHPMYADMIN=%EXPOSE_PHPMYADMIN% ^
    -e EXPOSE_MYSQL=%EXPOSE_MYSQL% ^
    -v multibarcode_mysql_data:/var/lib/mysql ^
    -v "%CD%\ssl":/etc/ssl/certs ^
    -v "%CD%\ssl":/etc/ssl/private ^
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
echo - Secure Web Management: https://localhost:3543/
echo.
echo Management Commands:
echo - View logs: docker logs multibarcode-webinterface
echo - Stop container: docker stop multibarcode-webinterface
echo - Remove container: docker rm multibarcode-webinterface
echo.
echo Android App Configuration:
echo - HTTP Endpoint: http://YOUR_IP:%WEB_PORT%/api/barcodes.php
echo - HTTPS Endpoint: https://YOUR_IP:3543/api/barcodes.php
echo - Example HTTP: http://192.168.1.100:%WEB_PORT%/api/barcodes.php
echo - Example HTTPS: https://192.168.1.100:3543/api/barcodes.php
echo.
echo SSL Certificate Installation:
echo - For Android: Install ssl\android_ca_system.pem as system certificate
echo - For browsers: Import ssl\wms_ca.crt to trusted root certificates
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
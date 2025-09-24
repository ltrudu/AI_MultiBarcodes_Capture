@echo off
setlocal enabledelayedexpansion

echo AI MultiBarcode Capture - Web Server Update
echo =============================================
echo This script updates the website files in the Docker container

REM Check if Docker container exists
docker ps -a -q -f name=multibarcode-webinterface >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker container 'multibarcode-webinterface' does not exist
    echo Please run start-services.bat first to create the container
    pause
    exit /b 1
)

REM Check if container is running
docker ps -q -f name=multibarcode-webinterface >nul 2>&1
if errorlevel 1 (
    echo [INFO] Container exists but is not running, starting it...
    docker start multibarcode-webinterface
    if errorlevel 1 (
        echo [ERROR] Failed to start container
        pause
        exit /b 1
    )
    echo [OK] Container started successfully
    echo Waiting for services to initialize...
    timeout /t 10 /nobreak >nul
) else (
    echo [OK] Container is running
)

echo.
echo [STEP 1] Copying website files to container...

REM Copy main website files
docker cp src\index.html multibarcode-webinterface:/var/www/html/
docker cp src\css multibarcode-webinterface:/var/www/html/
docker cp src\js multibarcode-webinterface:/var/www/html/
docker cp src\lang multibarcode-webinterface:/var/www/html/
docker cp src\lib multibarcode-webinterface:/var/www/html/

echo [STEP 2] Copying API files...
docker cp src\api multibarcode-webinterface:/var/www/html/

echo [STEP 3] Copying configuration files...
docker cp src\config multibarcode-webinterface:/var/www/html/

echo [STEP 4] Updating SSL certificates...
if exist "ssl" (
    echo [INFO] SSL certificates found, updating container...
    docker cp ssl multibarcode-webinterface:/etc/ssl/certs/
    docker cp ssl multibarcode-webinterface:/etc/ssl/private/

    echo [INFO] Copying CA certificates to web directory...
    if not exist "src\certificates" mkdir "src\certificates"
    copy "ssl\wms_ca.crt" "src\certificates\"
    copy "ssl\android_ca_system.pem" "src\certificates\"
    docker cp src\certificates multibarcode-webinterface:/var/www/html/
    echo [OK] SSL certificates updated
) else (
    echo [WARNING] SSL directory not found, skipping SSL certificate update
    echo [INFO] Run create-certificates.bat to generate SSL certificates
)

echo [STEP 5] Setting proper permissions...
docker exec multibarcode-webinterface bash -c "chown -R www-data:www-data /var/www/html"
docker exec multibarcode-webinterface bash -c "chmod -R 755 /var/www/html"

echo [STEP 6] Reloading Apache configuration...
docker exec multibarcode-webinterface bash -c "service apache2 reload"

if %errorlevel% equ 0 (
    echo.
    echo =============================================
    echo [SUCCESS] Web server updated successfully!
    echo =============================================
    echo.
    echo The website has been updated with the latest files.
    echo You can access it at:
    echo - HTTP: http://localhost:3500
    echo - HTTPS: https://localhost:3543
    echo.
) else (
    echo [ERROR] Failed to reload Apache configuration
    pause
    exit /b 1
)

echo Update completed successfully!
pause
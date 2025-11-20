@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo AI MultiBarcode Capture - XAMPP Server Startup
echo =========================================================
echo.

REM Detect XAMPP installation location (C:\xampp or D:\xampp)
set XAMPP_PATH=
if exist "C:\xampp" (
    set XAMPP_PATH=C:\xampp
    echo [INFO] XAMPP detected at C:\xampp
	goto installation
) else if exist "D:\xampp" (
    set XAMPP_PATH=D:\xampp
    echo [INFO] XAMPP detected at D:\xampp
	goto installation
) else (
    echo [ERROR] XAMPP not found at C:\xampp or D:\xampp
    echo.
    echo Please install XAMPP first:
    echo 1. Download from: https://www.apachefriends.org/
    echo 2. Install to C:\xampp or D:\xampp
    echo 3. Or extract xampp.zip to C:\ or D:\ (creates xampp folder automatically)
    echo 4. Run this script again
    echo.
    pause
    exit /b 1
)

:installation
echo [INFO] Using XAMPP installation at: %XAMPP_PATH%
echo.

REM Check if XAMPP is already running
echo [INFO] Checking if XAMPP services are running...
tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [WARNING] Apache is already running
    echo Stopping Apache...
    taskkill /F /IM httpd.exe >nul 2>&1
    timeout /t 3 /nobreak >nul
)

tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [WARNING] MySQL is already running
    echo Stopping MySQL...
    taskkill /F /IM mysqld.exe >nul 2>&1
    timeout /t 3 /nobreak >nul
)

echo.
echo =========================================================
echo STEP 1: Updating XAMPP Installation
echo =========================================================
echo.

REM Call the update script to install/update all files and configurations
echo [INFO] Updating XAMPP installation with latest files...
call "%~dp0xampp_update_server.bat"
if errorlevel 1 (
    echo [ERROR] XAMPP update failed!
    echo.
    pause
    exit /b 1
)
echo [OK] XAMPP installation updated successfully

echo.
echo =========================================================
echo STEP 2: Starting XAMPP Services
echo =========================================================
echo.

REM Fix ALL configuration paths if on D: drive
if "%XAMPP_PATH%"=="D:\xampp" (
    echo [INFO] Comprehensive path update for D: drive - this may take a moment...

    REM Fix ALL .ini, .conf, and .cnf files in entire XAMPP directory
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='SilentlyContinue'; $utf8 = New-Object System.Text.UTF8Encoding $false; $files = Get-ChildItem '%XAMPP_PATH%' -Include *.ini,*.conf,*.cnf -Recurse -File -ErrorAction SilentlyContinue; $count=0; foreach($f in $files){ try{ $content = [System.IO.File]::ReadAllText($f.FullName); $updated = $content -replace 'C:/xampp','D:/xampp' -replace 'C:\\xampp','D:\\xampp' -replace 'C:\\\\xampp','D:\\\\xampp'; if($content -ne $updated){ [System.IO.File]::WriteAllText($f.FullName, $updated, $utf8); $count++ } }catch{} } Write-Host \"Updated $count configuration files\""

    if errorlevel 1 (
        echo [WARNING] Some configuration files may not have been updated
    ) else (
        echo [OK] All configurations updated for D: drive
    )
)

REM Start MySQL
echo [INFO] Starting MySQL...
start "" "%XAMPP_PATH%\mysql\bin\mysqld.exe" --defaults-file="%XAMPP_PATH%\mysql\bin\my.ini" --standalone
echo [OK] MySQL starting...
timeout /t 5 /nobreak >nul

REM Check if MySQL is running
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MySQL is running
) else (
    echo [ERROR] MySQL failed to start
    echo Check %XAMPP_PATH%\mysql\data\*.err for errors
    pause
    exit /b 1
)

REM Initialize database if needed
echo [INFO] Initializing database and user...

REM Create database if it doesn't exist
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS barcode_wms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >nul 2>&1

REM Create MySQL user and grant privileges (matching Docker setup)
echo [INFO] Creating database user and setting permissions...
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "CREATE USER IF NOT EXISTS 'wms_user'@'localhost' IDENTIFIED BY 'wms_password';" >nul 2>&1
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "CREATE USER IF NOT EXISTS 'wms_user'@'%%' IDENTIFIED BY 'wms_password';" >nul 2>&1
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "GRANT ALL PRIVILEGES ON barcode_wms.* TO 'wms_user'@'localhost';" >nul 2>&1
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "GRANT ALL PRIVILEGES ON barcode_wms.* TO 'wms_user'@'%%';" >nul 2>&1
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "FLUSH PRIVILEGES;" >nul 2>&1
echo [OK] Database user created and permissions granted

REM Initialize/Update database schema (safe to run multiple times)
echo [INFO] Initializing database schema...
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms < "database\init.sql" 2>nul
if errorlevel 1 (
    echo [ERROR] Database schema initialization failed!
    echo.
    echo Troubleshooting steps:
    echo 1. Check if MySQL is running properly
    echo 2. Verify init.sql exists at: database\init.sql
    echo 3. Check MySQL error logs at: %XAMPP_PATH%\mysql\data\*.err
    echo.
    echo Attempting manual initialization...
    "%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms < "database\init.sql"
    pause
    exit /b 1
)

REM Verify database schema
echo [INFO] Verifying database schema...
for /f %%i in ('"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms -sN -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='barcode_wms' AND TABLE_NAME IN ('capture_sessions', 'barcodes', 'symbologies');" 2^>nul') do set TABLE_COUNT=%%i
for /f %%i in ('"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms -sN -e "SELECT COUNT(*) FROM information_schema.VIEWS WHERE TABLE_SCHEMA='barcode_wms' AND TABLE_NAME IN ('session_statistics', 'barcode_details');" 2^>nul') do set VIEW_COUNT=%%i

if "%TABLE_COUNT%"=="3" (
    if "%VIEW_COUNT%"=="2" (
        echo [OK] Database schema verified successfully (3 tables, 2 views)
    ) else (
        echo [WARNING] Database views incomplete - found !VIEW_COUNT! of 2 expected views
        echo The web interface may not work correctly
    )
) else (
    echo [WARNING] Database tables incomplete - found !TABLE_COUNT! of 3 expected tables
    echo The web interface may not work correctly
)

REM Start Apache
echo [INFO] Starting Apache...
start "" "%XAMPP_PATH%\apache\bin\httpd.exe"
echo [OK] Apache starting...
timeout /t 3 /nobreak >nul

REM Check if Apache is running
tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] Apache is running
) else (
    echo [ERROR] Apache failed to start
    echo Check %XAMPP_PATH%\apache\logs\error.log for details
    pause
    exit /b 1
)

echo.
echo =========================================================
echo STEP 3: Updating Network IP Configuration
echo =========================================================
echo.

REM Update IP configuration file for web interface
echo [INFO] Updating network IP configuration...
call "%~dp0xampp_update_network_IP.bat" >nul 2>&1
if errorlevel 1 (
    echo [WARNING] IP configuration update failed - QR codes may not work correctly
    echo [INFO] You can manually run xampp_update_network_IP.bat to update IPs
) else (
    echo [OK] Network IP configuration updated successfully
)

REM Get host IP address for display
echo.
echo [INFO] Detecting host IP address...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "192.168"') do (
    set HOST_IP_RAW=%%a
    goto :found_ip
)
:found_ip
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
    echo [WARNING] Could not detect host IP automatically
    set HOST_IP=YOUR_COMPUTER_IP
) else (
    echo [OK] Detected host IP: %HOST_IP%
)

echo.
echo =========================================================
echo SUCCESS! AI MultiBarcode Capture is now running!
echo =========================================================
echo.
echo XAMPP Installation: %XAMPP_PATH%
echo.
echo Access Points:
echo - Web Management System: http://localhost:3500
echo - Secure Management:      https://localhost:3543
echo - phpMyAdmin:             http://localhost:3500/phpmyadmin
echo.
echo From other devices on your network:
echo - HTTP:  http://%HOST_IP%:3500
echo - HTTPS: https://%HOST_IP%:3543
echo.
echo Android App Configuration:
echo - HTTP Endpoint:  http://%HOST_IP%:3500/api/barcodes.php
echo - HTTPS Endpoint: https://%HOST_IP%:3543/api/barcodes.php
echo.
echo SSL Certificate Installation:
echo - For Android: Install ssl\android_ca_system.pem
echo - For browsers: Import ssl\wms_ca.crt to trusted root
echo.
echo Database Credentials:
echo - Host: localhost
echo - Database: barcode_wms
echo - Username: wms_user
echo - Password: wms_password
echo.
echo Management Commands:
echo - Stop Apache:  %XAMPP_PATH%\apache\bin\httpd.exe -k stop
echo - Stop MySQL:   %XAMPP_PATH%\mysql\bin\mysqladmin.exe -u root shutdown
echo - View Logs:    %XAMPP_PATH%\apache\logs\
echo.
echo XAMPP Control Panel:
echo - Run: %XAMPP_PATH%\xampp-control.exe (for GUI control)
echo.
pause

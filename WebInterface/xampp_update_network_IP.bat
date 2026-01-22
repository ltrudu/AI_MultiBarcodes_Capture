@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo AI MultiBarcode Capture - Network IP Update
echo =========================================================
echo.

REM Detect XAMPP installation location (C:\xampp or D:\xampp)
set XAMPP_PATH=
if exist "C:\xampp" (
    set XAMPP_PATH=C:\xampp
    echo [INFO] XAMPP detected at C:\xampp
) else if exist "D:\xampp" (
    set XAMPP_PATH=D:\xampp
    echo [INFO] XAMPP detected at D:\xampp
) else (
    echo [ERROR] XAMPP not found at C:\xampp or D:\xampp
    echo Please install XAMPP first
    pause
    exit /b 1
)

echo [INFO] Detecting network IP addresses...

REM Collect all IPv4 addresses with interface names using PowerShell
set COUNT=0
for /f "tokens=1,2,3 delims=|" %%A in ('powershell -NoProfile -Command "Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -match '^(192\.168\.|10\.|172\.(1[6-9]|2[0-9]|3[0-1])\.)' } | ForEach-Object { $iface = (Get-NetAdapter -InterfaceIndex $_.InterfaceIndex -ErrorAction SilentlyContinue).Name; if(-not $iface){ $iface='Unknown' }; Write-Output ($_.IPAddress + '|' + $iface + '|') }"') do (
    set /a COUNT+=1
    set "IP_!COUNT!=%%A"
    set "IFACE_!COUNT!=%%B"
)

REM Handle based on number of interfaces found
if !COUNT!==0 (
    echo [WARNING] No private IP addresses found, using localhost
    set LOCAL_IP=127.0.0.1
    goto :ip_selected
)

if !COUNT!==1 (
    set "LOCAL_IP=!IP_1!"
    echo [OK] Local IP detected: !LOCAL_IP! ^(!IFACE_1!^)
    goto :ip_selected
)

REM Multiple interfaces found - let user select
echo.
echo Found !COUNT! network interfaces:
echo.
for /L %%i in (1,1,!COUNT!) do (
    echo   %%i. !IP_%%i! - !IFACE_%%i!
)
echo.
set /p "CHOICE=Select interface [1-!COUNT!]: "

REM Validate choice
if "!CHOICE!"=="" set CHOICE=1
if !CHOICE! LSS 1 set CHOICE=1
if !CHOICE! GTR !COUNT! set CHOICE=!COUNT!

set "LOCAL_IP=!IP_%CHOICE%!"
echo.
echo [OK] Selected: !LOCAL_IP! ^(!IFACE_%CHOICE%!^)

:ip_selected

REM Try to get external IP using PowerShell
echo [INFO] Detecting external IP address...
set EXTERNAL_IP=
for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $ip = (Invoke-WebRequest -Uri 'http://ipinfo.io/ip' -TimeoutSec 5 -UseBasicParsing).Content.Trim(); if($ip -match '^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$'){ Write-Output $ip } else { Write-Output 'Unable to detect' } } catch { Write-Output 'Unable to detect' }" 2^>nul`) do set EXTERNAL_IP=%%i

if "%EXTERNAL_IP%"=="" set EXTERNAL_IP=Unable to detect

if "%EXTERNAL_IP%"=="Unable to detect" (
    echo [WARNING] External IP could not be detected
) else (
    echo [OK] External IP detected: %EXTERNAL_IP%
)

REM Get current timestamp
for /f "tokens=1-6 delims=/: " %%a in ("%date% %time%") do (
    set YEAR=%%c
    set MONTH=%%a
    set DAY=%%b
    set HOUR=%%d
    set MINUTE=%%e
    set SECOND=%%f
)

REM Ensure two-digit format
if "%MONTH:~1%"=="" set MONTH=0%MONTH%
if "%DAY:~1%"=="" set DAY=0%DAY%
if "%HOUR:~1%"=="" set HOUR=0%HOUR%
if "%MINUTE:~1%"=="" set MINUTE=0%MINUTE%
if "%SECOND:~1%"=="" set SECOND=0%SECOND%

set TIMESTAMP=%YEAR%-%MONTH%-%DAY%T%HOUR%:%MINUTE%:%SECOND%Z

REM Create config directory if it doesn't exist
if not exist "%XAMPP_PATH%\htdocs\config" (
    mkdir "%XAMPP_PATH%\htdocs\config"
    echo [INFO] Created config directory
)

REM Create ip-config.json file
echo [INFO] Writing IP configuration to %XAMPP_PATH%\htdocs\config\ip-config.json
(
echo {
echo   "local_ip": "%LOCAL_IP%",
echo   "external_ip": "%EXTERNAL_IP%",
echo   "last_updated": "%TIMESTAMP%",
echo   "detection_method": "xampp_batch_update"
echo }
) > "%XAMPP_PATH%\htdocs\config\ip-config.json"

if errorlevel 1 (
    echo [ERROR] Failed to create IP configuration file
    exit /b 1
) else (
    echo [OK] IP configuration updated successfully
)

echo.
echo =========================================================
echo Network IP Configuration Updated
echo =========================================================
echo.
echo Local IP:    %LOCAL_IP%
echo External IP: %EXTERNAL_IP%
echo Config File: %XAMPP_PATH%\htdocs\config\ip-config.json
echo.
echo You can now configure your Android app with:
echo - HTTP Endpoint:  http://%LOCAL_IP%:3500/api/barcodes.php
echo - HTTPS Endpoint: https://%LOCAL_IP%:3543/api/barcodes.php
echo.

exit /b 0

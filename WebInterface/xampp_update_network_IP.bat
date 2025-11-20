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

REM Get local IP address - try 192.168.x.x first
set LOCAL_IP=
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "192.168"') do (
    set LOCAL_IP_RAW=%%a
    goto :found_local_ip
)

REM Fallback to 10.x.x.x range
if "%LOCAL_IP%"=="" (
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "10\."') do (
        set LOCAL_IP_RAW=%%a
        goto :found_local_ip
    )
)

:found_local_ip
REM Trim whitespace from LOCAL_IP
if defined LOCAL_IP_RAW (
    for /f "tokens=*" %%a in ("%LOCAL_IP_RAW%") do set LOCAL_IP=%%a
)

REM Set fallback if no IP detected
if "%LOCAL_IP%"=="" set LOCAL_IP=127.0.0.1

echo [OK] Local IP detected: %LOCAL_IP%

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

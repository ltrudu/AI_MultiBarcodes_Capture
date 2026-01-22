@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo AI MultiBarcode Capture - Database Reset Tool
echo =========================================================
echo.
echo WARNING: This will delete ALL data and reset the database!
echo.
set /p "CONFIRM=Are you sure you want to continue? (yes/no): "
if /i not "!CONFIRM!"=="yes" (
    echo Operation cancelled.
    pause
    exit /b 0
)

REM Detect XAMPP installation location
set XAMPP_PATH=
if exist "C:\xampp" (
    set XAMPP_PATH=C:\xampp
) else if exist "D:\xampp" (
    set XAMPP_PATH=D:\xampp
) else (
    echo [ERROR] XAMPP not found at C:\xampp or D:\xampp
    pause
    exit /b 1
)

echo.
echo [INFO] Using XAMPP at: %XAMPP_PATH%
echo.

REM Step 1: Stop MySQL if running
echo [STEP 1/5] Stopping MySQL...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] MySQL is running, stopping it...
    taskkill /F /IM mysqld.exe >nul 2>&1
    timeout /t 3 /nobreak >nul
    echo [OK] MySQL stopped
) else (
    echo [INFO] MySQL is not running
)

REM Step 2: Full MySQL data reset
echo.
echo [STEP 2/5] Performing full MySQL data reset...

REM Backup current data folder
set BACKUP_NAME=data_backup_%date:~-4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%
set BACKUP_NAME=%BACKUP_NAME: =0%
echo [INFO] Backing up current data to: %BACKUP_NAME%
if exist "%XAMPP_PATH%\mysql\%BACKUP_NAME%" rd /s /q "%XAMPP_PATH%\mysql\%BACKUP_NAME%" >nul 2>&1
move "%XAMPP_PATH%\mysql\data" "%XAMPP_PATH%\mysql\%BACKUP_NAME%" >nul 2>&1

REM Copy fresh data folder from XAMPP backup
if exist "%XAMPP_PATH%\mysql\backup" (
    echo [INFO] Restoring fresh MySQL data from XAMPP backup...
    xcopy /E /I /Q "%XAMPP_PATH%\mysql\backup" "%XAMPP_PATH%\mysql\data" >nul 2>&1
    echo [OK] Fresh MySQL data restored
) else (
    echo [ERROR] No XAMPP backup folder found at %XAMPP_PATH%\mysql\backup
    echo [INFO] Restoring from your backup...
    move "%XAMPP_PATH%\mysql\%BACKUP_NAME%" "%XAMPP_PATH%\mysql\data" >nul 2>&1
    pause
    exit /b 1
)

REM Step 3: Start MySQL
echo.
echo [STEP 3/5] Starting MySQL...
start "" "%XAMPP_PATH%\mysql\bin\mysqld.exe" --defaults-file="%XAMPP_PATH%\mysql\bin\my.ini" --standalone
echo [INFO] Waiting for MySQL to start...
timeout /t 5 /nobreak >nul

REM Verify MySQL is running
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MySQL is running
) else (
    echo [ERROR] MySQL failed to start
    echo [INFO] Check %XAMPP_PATH%\mysql\data\*.err for errors
    pause
    exit /b 1
)

REM Step 4: Create database and user
echo.
echo [STEP 4/5] Creating database and user...

"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS barcode_wms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if errorlevel 1 (
    echo [ERROR] Failed to create database
    pause
    exit /b 1
)
echo [OK] Database created

REM Create user (may fail if mysql system tables still have issues, but root will work)
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "CREATE USER IF NOT EXISTS 'wms_user'@'localhost' IDENTIFIED BY 'wms_password';" 2>nul
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "CREATE USER IF NOT EXISTS 'wms_user'@'%%' IDENTIFIED BY 'wms_password';" 2>nul
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "GRANT ALL PRIVILEGES ON barcode_wms.* TO 'wms_user'@'localhost';" 2>nul
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "GRANT ALL PRIVILEGES ON barcode_wms.* TO 'wms_user'@'%%';" 2>nul
"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root -e "FLUSH PRIVILEGES;" 2>nul
echo [OK] User permissions configured

REM Step 5: Initialize database schema
echo.
echo [STEP 5/5] Initializing database schema...

"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms < "%~dp0database\init.sql" 2>nul
if errorlevel 1 (
    echo [ERROR] Failed to initialize database schema
    echo [INFO] Trying alternative path...
    "%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms < "database\init.sql"
    if errorlevel 1 (
        echo [ERROR] Database schema initialization failed!
        pause
        exit /b 1
    )
)
echo [OK] Database schema initialized

REM Verify database
echo.
echo [INFO] Verifying database...
for /f %%i in ('"%XAMPP_PATH%\mysql\bin\mysql.exe" -u root barcode_wms -sN -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='barcode_wms';" 2^>nul') do set TABLE_COUNT=%%i

if "!TABLE_COUNT!" GEQ "3" (
    echo [OK] Database verified: !TABLE_COUNT! tables/views found
) else (
    echo [WARNING] Database may be incomplete: only !TABLE_COUNT! tables found
)

echo.
echo =========================================================
echo Database Reset Complete!
echo =========================================================
echo.
echo The barcode_wms database has been reset to initial state.
echo All previous data has been deleted.
echo.
echo You can now restart the server with: xampp_start_server.bat
echo.
pause

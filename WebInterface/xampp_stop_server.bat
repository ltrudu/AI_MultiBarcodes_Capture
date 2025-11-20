@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo AI MultiBarcode Capture - XAMPP Server Shutdown
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
    echo [WARNING] XAMPP not found at C:\xampp or D:\xampp
    echo Will attempt to stop services anyway...
    echo.
)

if not "%XAMPP_PATH%"=="" (
    echo [INFO] Using XAMPP installation at: %XAMPP_PATH%
    echo.
)

echo =========================================================
echo Stopping XAMPP Services
echo =========================================================
echo.

REM Stop Apache
echo [INFO] Checking for Apache (httpd.exe)...
tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] Apache is running - stopping...
    taskkill /F /IM httpd.exe >nul 2>&1
    timeout /t 2 /nobreak >nul

    REM Verify Apache stopped
    tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
    if "%ERRORLEVEL%"=="0" (
        echo [WARNING] Apache may still be running
    ) else (
        echo [OK] Apache stopped successfully
    )
) else (
    echo [INFO] Apache is not running
)

echo.

REM Stop MySQL
echo [INFO] Checking for MySQL (mysqld.exe)...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] MySQL is running - stopping...
    taskkill /F /IM mysqld.exe >nul 2>&1
    timeout /t 3 /nobreak >nul

    REM Verify MySQL stopped
    tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
    if "%ERRORLEVEL%"=="0" (
        echo [WARNING] MySQL may still be running
    ) else (
        echo [OK] MySQL stopped successfully
    )
) else (
    echo [INFO] MySQL is not running
)

echo.

REM Check for other XAMPP-related processes
echo [INFO] Checking for other XAMPP processes...

REM Check for PHP-CGI
tasklist /FI "IMAGENAME eq php-cgi.exe" 2>NUL | find /I /N "php-cgi.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] PHP-CGI is running - stopping...
    taskkill /F /IM php-cgi.exe >nul 2>&1
    echo [OK] PHP-CGI stopped
)

REM Check for FileZilla (if running from XAMPP)
tasklist /FI "IMAGENAME eq filezillaftpserver.exe" 2>NUL | find /I /N "filezillaftpserver.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] FileZilla FTP Server is running - stopping...
    taskkill /F /IM filezillaftpserver.exe >nul 2>&1
    echo [OK] FileZilla FTP Server stopped
)

REM Check for Mercury (mail server)
tasklist /FI "IMAGENAME eq mercury.exe" 2>NUL | find /I /N "mercury.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] Mercury Mail Server is running - stopping...
    taskkill /F /IM mercury.exe >nul 2>&1
    echo [OK] Mercury Mail Server stopped
)

REM Check for Tomcat (if running from XAMPP)
tasklist /FI "IMAGENAME eq tomcat*.exe" 2>NUL | find /I /N "tomcat">NUL
if "%ERRORLEVEL%"=="0" (
    echo [INFO] Tomcat is running - stopping...
    taskkill /F /IM tomcat*.exe >nul 2>&1
    echo [OK] Tomcat stopped
)

echo.
echo =========================================================
echo XAMPP Services Shutdown Complete
echo =========================================================
echo.
echo All XAMPP services have been stopped.
echo.
pause

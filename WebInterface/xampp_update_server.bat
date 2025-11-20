@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo AI MultiBarcode Capture - XAMPP Server Update
echo =========================================================
echo.
echo This script updates the XAMPP installation with the latest
echo website files from the src directory.
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
echo.
echo =========================================================
echo STEP 1: Updating Website Files
echo =========================================================
echo.

REM Backup existing htdocs if it exists
if exist "%XAMPP_PATH%\htdocs\index.html" (
    echo [INFO] Backing up existing htdocs...
    if not exist "%XAMPP_PATH%\htdocs_backup" mkdir "%XAMPP_PATH%\htdocs_backup"
    set BACKUP_NAME=backup_%date:~-4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
    set BACKUP_NAME=!BACKUP_NAME: =0!
    xcopy /E /Y /I "%XAMPP_PATH%\htdocs" "%XAMPP_PATH%\htdocs_backup\!BACKUP_NAME!" >nul 2>&1
    echo [OK] Backup created: %XAMPP_PATH%\htdocs_backup\!BACKUP_NAME!
)

REM Copy website files from src
echo [INFO] Copying website files from src to %XAMPP_PATH%\htdocs...
if exist "%XAMPP_PATH%\htdocs" (
    REM Remove old files but keep vendor if it exists
    if exist "%XAMPP_PATH%\htdocs\vendor" (
        echo [INFO] Preserving Composer vendor directory...
        move "%XAMPP_PATH%\htdocs\vendor" "%XAMPP_PATH%\vendor_temp" >nul 2>&1
    )
    del /Q /F "%XAMPP_PATH%\htdocs\*.*" >nul 2>&1
    for /d %%p in ("%XAMPP_PATH%\htdocs\*") do rmdir "%%p" /s /q >nul 2>&1
    if exist "%XAMPP_PATH%\vendor_temp" (
        move "%XAMPP_PATH%\vendor_temp" "%XAMPP_PATH%\htdocs\vendor" >nul 2>&1
    )
)
xcopy /E /Y /I "src\*" "%XAMPP_PATH%\htdocs\" >nul
echo [OK] Website files copied

REM Fix XAMPP-specific paths in server-info.php
echo [INFO] Applying XAMPP-specific configuration...
if exist "%XAMPP_PATH%\htdocs\api\server-info.php" (
    powershell -Command "(Get-Content '%XAMPP_PATH%\htdocs\api\server-info.php') -replace '/\.\./\.\./config', '/../config' | Set-Content '%XAMPP_PATH%\htdocs\api\server-info.php'" >nul 2>&1
    echo [OK] server-info.php configured for XAMPP
)

echo.
echo =========================================================
echo STEP 2: Updating Apache Configurations
echo =========================================================
echo.

REM Check and generate SSL certificates if needed
echo [INFO] Checking SSL certificates...
if not exist "ssl" (
    echo [WARNING] SSL directory does not exist
    set MISSING_CERTS=true
) else (
    set MISSING_CERTS=false
    if not exist "ssl\wms_ca.crt" set MISSING_CERTS=true
    if not exist "ssl\wms.crt" set MISSING_CERTS=true
    if not exist "ssl\wms.key" set MISSING_CERTS=true
    if not exist "ssl\android_ca_system.pem" set MISSING_CERTS=true
)

if "%MISSING_CERTS%"=="true" (
    echo [WARNING] SSL certificates missing
    echo Please run create-certificates.bat first, or use HTTP only
) else (
    echo [OK] SSL certificates found

    REM Copy SSL certificates
    echo [INFO] Copying SSL certificates...
    if not exist "%XAMPP_PATH%\apache\conf\ssl" mkdir "%XAMPP_PATH%\apache\conf\ssl"
    copy /Y "ssl\wms.crt" "%XAMPP_PATH%\apache\conf\ssl\server.crt" >nul 2>&1
    copy /Y "ssl\wms.key" "%XAMPP_PATH%\apache\conf\ssl\server.key" >nul 2>&1
    copy /Y "ssl\wms_ca.crt" "%XAMPP_PATH%\apache\conf\ssl\ca.crt" >nul 2>&1
    echo [OK] SSL certificates copied
)

REM Update httpd.conf to include our configurations
echo [INFO] Updating Apache main configuration...
findstr /C:"httpd-multibarcode.conf" "%XAMPP_PATH%\apache\conf\httpd.conf" >nul 2>&1
if errorlevel 1 (
    echo. >> "%XAMPP_PATH%\apache\conf\httpd.conf"
    echo # AI MultiBarcode Capture Configuration >> "%XAMPP_PATH%\apache\conf\httpd.conf"
    echo Include conf/extra/httpd-multibarcode.conf >> "%XAMPP_PATH%\apache\conf\httpd.conf"
    echo Include conf/extra/httpd-multibarcode-ssl.conf >> "%XAMPP_PATH%\apache\conf\httpd.conf"
    echo [OK] Configuration includes added
) else (
    echo [OK] Configuration includes already present
)

REM Update httpd.conf to change default port
echo [INFO] Configuring Apache to listen on port 3500...
powershell -Command "(Get-Content '%XAMPP_PATH%\apache\conf\httpd.conf') -replace 'Listen 80$', 'Listen 3500' | Set-Content '%XAMPP_PATH%\apache\conf\httpd.conf'" >nul 2>&1
echo [OK] Port configured

REM Ensure required Apache modules are enabled
echo [INFO] Enabling required Apache modules...
powershell -Command "(Get-Content '%XAMPP_PATH%\apache\conf\httpd.conf') -replace '#LoadModule rewrite_module', 'LoadModule rewrite_module' | Set-Content '%XAMPP_PATH%\apache\conf\httpd.conf'" >nul 2>&1
powershell -Command "(Get-Content '%XAMPP_PATH%\apache\conf\httpd.conf') -replace '#LoadModule ssl_module', 'LoadModule ssl_module' | Set-Content '%XAMPP_PATH%\apache\conf\httpd.conf'" >nul 2>&1
powershell -Command "(Get-Content '%XAMPP_PATH%\apache\conf\httpd.conf') -replace '#LoadModule headers_module', 'LoadModule headers_module' | Set-Content '%XAMPP_PATH%\apache\conf\httpd.conf'" >nul 2>&1
powershell -Command "(Get-Content '%XAMPP_PATH%\apache\conf\httpd.conf') -replace '#LoadModule expires_module', 'LoadModule expires_module' | Set-Content '%XAMPP_PATH%\apache\conf\httpd.conf'" >nul 2>&1
echo [OK] Apache modules enabled

REM Update PHP configuration for database connectivity
echo [INFO] Configuring PHP for database access...
if exist "%XAMPP_PATH%\htdocs\config" (
    if exist "%XAMPP_PATH%\htdocs\config\database.php" (
        REM Update database configuration
        powershell -Command "(Get-Content '%XAMPP_PATH%\htdocs\config\database.php') -replace \"'host' => '.*'\", \"'host' => '127.0.0.1'\" | Set-Content '%XAMPP_PATH%\htdocs\config\database.php'" >nul 2>&1
        echo [OK] PHP database configuration updated
    )
)

REM Copy Apache configuration files
echo [INFO] Creating Apache configuration files...
if not exist "%XAMPP_PATH%\apache\conf\extra" mkdir "%XAMPP_PATH%\apache\conf\extra"

REM Create HTTP configuration
(
echo ^<VirtualHost *:3500^>
echo     ServerAdmin webmaster@localhost
echo     DocumentRoot "%XAMPP_PATH:\=/%/htdocs"
echo.
echo     ^<Directory "%XAMPP_PATH:\=/%/htdocs"^>
echo         Options Indexes FollowSymLinks
echo         AllowOverride All
echo         Require all granted
echo     ^</Directory^>
echo.
echo     # Enable CORS for API endpoints
echo     ^<Directory "%XAMPP_PATH:\=/%/htdocs/api"^>
echo         Header always set Access-Control-Allow-Origin "*"
echo         Header always set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
echo         Header always set Access-Control-Allow-Headers "Content-Type, Authorization, X-Requested-With"
echo.
echo         # Handle preflight requests
echo         RewriteEngine On
echo         RewriteCond %%{REQUEST_METHOD} OPTIONS
echo         RewriteRule ^^^(.*^^^)$ $1 [R=200,L]
echo     ^</Directory^>
echo.
echo     # Security headers
echo     Header always set X-Content-Type-Options nosniff
echo     Header always set X-Frame-Options DENY
echo     Header always set X-XSS-Protection "1; mode=block"
echo     Header always set Referrer-Policy "strict-origin-when-cross-origin"
echo.
echo     ErrorLog "logs/multibarcode_error.log"
echo     CustomLog "logs/multibarcode_access.log" combined
echo ^</VirtualHost^>
) > "%XAMPP_PATH%\apache\conf\extra\httpd-multibarcode.conf"
echo [OK] HTTP configuration created

REM Create SSL configuration if certificates exist
if "%MISSING_CERTS%"=="false" (
    (
    echo ^<IfModule mod_ssl.c^>
    echo     # Listen on port 3543 for HTTPS
    echo     Listen 3543 https
    echo.
    echo     ^<VirtualHost *:3543^>
    echo         ServerAdmin webmaster@localhost
    echo         DocumentRoot "%XAMPP_PATH:\=/%/htdocs"
    echo.
    echo         # SSL Configuration
    echo         SSLEngine on
    echo         SSLCertificateFile "conf/ssl/server.crt"
    echo         SSLCertificateKeyFile "conf/ssl/server.key"
    echo.
    echo         # Modern SSL configuration
    echo         SSLProtocol -all +TLSv1.2 +TLSv1.3
    echo         SSLCipherSuite ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384
    echo         SSLHonorCipherOrder off
    echo         SSLSessionTickets off
    echo.
    echo         ^<Directory "%XAMPP_PATH:\=/%/htdocs"^>
    echo             Options Indexes FollowSymLinks
    echo             AllowOverride All
    echo             Require all granted
    echo         ^</Directory^>
    echo.
    echo         # Enable CORS for API endpoints
    echo         ^<Directory "%XAMPP_PATH:\=/%/htdocs/api"^>
    echo             Header always set Access-Control-Allow-Origin "*"
    echo             Header always set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
    echo             Header always set Access-Control-Allow-Headers "Content-Type, Authorization, X-Requested-With"
    echo.
    echo             # Handle preflight requests
    echo             RewriteEngine On
    echo             RewriteCond %%{REQUEST_METHOD} OPTIONS
    echo             RewriteRule ^^^(.*^^^)$ $1 [R=200,L]
    echo         ^</Directory^>
    echo.
    echo         # Security headers
    echo         Header always set Strict-Transport-Security "max-age=63072000; includeSubDomains; preload"
    echo         Header always set X-Content-Type-Options nosniff
    echo         Header always set X-Frame-Options DENY
    echo         Header always set X-XSS-Protection "1; mode=block"
    echo.
    echo         ErrorLog "logs/multibarcode_ssl_error.log"
    echo         CustomLog "logs/multibarcode_ssl_access.log" combined
    echo     ^</VirtualHost^>
    echo ^</IfModule^>
    ) > "%XAMPP_PATH%\apache\conf\extra\httpd-multibarcode-ssl.conf"
    echo [OK] SSL configuration created
)

echo.
echo =========================================================
echo [SUCCESS] XAMPP Server Updated!
echo =========================================================
echo.
echo XAMPP Installation: %XAMPP_PATH%
echo.
echo Updated components:
echo - Website files copied from src to htdocs
echo - XAMPP-specific configurations applied
echo - Apache configuration files updated
if "%MISSING_CERTS%"=="false" (
    echo - SSL certificates installed
) else (
    echo - SSL certificates NOT installed (HTTP only)
)
echo.
echo Next step:
echo - Run xampp_start_server.bat to start services
echo - Or manually start Apache and MySQL from XAMPP Control Panel
echo.

exit /b 0

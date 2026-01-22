@echo off
setlocal enabledelayedexpansion

echo Updating network IP in Docker container...
echo.

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
    echo [OK] Detected local IP: !LOCAL_IP! ^(!IFACE_1!^)
    goto :ip_selected
)

REM Multiple interfaces found - let user select
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
echo.

REM Update IP configuration directly in container
docker exec multibarcode-webinterface bash -c "mkdir -p /var/www/html/config"

docker exec multibarcode-webinterface bash -c "cat > /var/www/html/config/ip-config.json << 'EOF'
{
  \"local_ip\": \"%LOCAL_IP%\",
  \"external_ip\": \"Unable to detect\",
  \"last_updated\": \"$(date -u +%%Y-%%m-%%dT%%H:%%M:%%SZ)\",
  \"detection_method\": \"batch_update\"
}
EOF"

REM Restart container with new HOST_IP environment variable
docker stop multibarcode-webinterface
docker rm multibarcode-webinterface

docker run -d ^
    --name multibarcode-webinterface ^
    -p 3500:3500 ^
    -p 3543:3543 ^
    -e MYSQL_ROOT_PASSWORD=root_password ^
    -e MYSQL_DATABASE=barcode_wms ^
    -e MYSQL_USER=wms_user ^
    -e MYSQL_PASSWORD=wms_password ^
    -e DB_HOST=127.0.0.1 ^
    -e DB_NAME=barcode_wms ^
    -e DB_USER=wms_user ^
    -e DB_PASS=wms_password ^
    -e WEB_PORT=3500 ^
    -e HOST_IP=%LOCAL_IP% ^
    -e EXPOSE_PHPMYADMIN=false ^
    -e EXPOSE_MYSQL=false ^
    -v multibarcode_mysql_data:/var/lib/mysql ^
    -v "%~dp0ssl":/etc/ssl/certs ^
    -v "%~dp0ssl":/etc/ssl/private ^
    --restart unless-stopped ^
    multibarcode-webinterface:latest

echo Network IP updated to: %LOCAL_IP%
echo Container restarted successfully.
@echo off

echo Updating network IP in Docker container...

REM Get local IP using basic Windows commands
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "192.168"') do (
    for /f "tokens=*" %%b in ("%%a") do set LOCAL_IP=%%b
)

REM Fallback to other private ranges if 192.168 not found
if "%LOCAL_IP%"=="" (
    for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4" ^| findstr "10\."') do (
        for /f "tokens=*" %%b in ("%%a") do set LOCAL_IP=%%b
    )
)

REM Set fallback if no IP detected
if "%LOCAL_IP%"=="" set LOCAL_IP=127.0.0.1

echo Detected local IP: %LOCAL_IP%

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
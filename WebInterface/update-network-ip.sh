#!/bin/bash

echo "Updating network IP in Docker container..."

# Get local IP using standard commands
LOCAL_IP=$(hostname -I 2>/dev/null | tr ' ' '\n' | grep '^192\.168\.' | head -1)

# Fallback to other private ranges
if [ -z "$LOCAL_IP" ]; then
    LOCAL_IP=$(hostname -I 2>/dev/null | tr ' ' '\n' | grep -E '^(10\.|172\.(1[6-9]|2[0-9]|3[0-1]))' | head -1)
fi

# Final fallback
if [ -z "$LOCAL_IP" ]; then
    LOCAL_IP="127.0.0.1"
fi

echo "Detected local IP: $LOCAL_IP"

# Update IP configuration directly in container
docker exec multibarcode-webinterface bash -c "mkdir -p /var/www/html/config"

docker exec multibarcode-webinterface bash -c "cat > /var/www/html/config/ip-config.json << 'EOF'
{
  \"local_ip\": \"$LOCAL_IP\",
  \"external_ip\": \"Unable to detect\",
  \"last_updated\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",
  \"detection_method\": \"shell_update\"
}
EOF"

# Restart container with new HOST_IP environment variable
docker stop multibarcode-webinterface
docker rm multibarcode-webinterface

docker run -d --name multibarcode-webinterface -p 3500:3500 -e MYSQL_ROOT_PASSWORD=root_password -e MYSQL_DATABASE=barcode_wms -e MYSQL_USER=wms_user -e MYSQL_PASSWORD=wms_password -e DB_HOST=127.0.0.1 -e DB_NAME=barcode_wms -e DB_USER=wms_user -e DB_PASS=wms_password -e WEB_PORT=3500 -e HOST_IP="$LOCAL_IP" -e EXPOSE_PHPMYADMIN=false -e EXPOSE_MYSQL=false -v multibarcode_mysql_data:/var/lib/mysql --restart unless-stopped multibarcode-webinterface:latest

echo "Network IP updated to: $LOCAL_IP"
echo "Container restarted successfully."
#!/bin/bash

echo "Updating network IP in Docker container..."
echo ""

# Collect all private IPv4 addresses with interface names
declare -a IPS
declare -a IFACES
COUNT=0

while IFS= read -r line; do
    # Extract interface name and IP
    iface=$(echo "$line" | awk '{print $1}')
    ip=$(echo "$line" | awk '{print $2}')
    # Only include private IPs
    if [[ "$ip" =~ ^(192\.168\.|10\.|172\.(1[6-9]|2[0-9]|3[0-1])\.) ]]; then
        IPS[$COUNT]="$ip"
        IFACES[$COUNT]="$iface"
        ((COUNT++))
    fi
done < <(ip -4 addr show 2>/dev/null | awk '/inet / {gsub(/\/.*/, "", $2); print $NF, $2}' || hostname -I 2>/dev/null | tr ' ' '\n' | grep -E '^[0-9]+\.' | while read ip; do echo "unknown $ip"; done)

# Handle based on number of interfaces found
if [ $COUNT -eq 0 ]; then
    echo "[WARNING] No private IP addresses found, using localhost"
    LOCAL_IP="127.0.0.1"
elif [ $COUNT -eq 1 ]; then
    LOCAL_IP="${IPS[0]}"
    echo "[OK] Detected local IP: $LOCAL_IP (${IFACES[0]})"
else
    # Multiple interfaces found - let user select
    echo "Found $COUNT network interfaces:"
    echo ""
    for i in $(seq 0 $((COUNT-1))); do
        echo "  $((i+1)). ${IPS[$i]} - ${IFACES[$i]}"
    done
    echo ""
    read -p "Select interface [1-$COUNT]: " CHOICE

    # Validate choice
    if [ -z "$CHOICE" ] || [ "$CHOICE" -lt 1 ] 2>/dev/null || [ "$CHOICE" -gt "$COUNT" ] 2>/dev/null; then
        CHOICE=1
    fi

    LOCAL_IP="${IPS[$((CHOICE-1))]}"
    echo ""
    echo "[OK] Selected: $LOCAL_IP (${IFACES[$((CHOICE-1))]})"
fi

echo ""

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
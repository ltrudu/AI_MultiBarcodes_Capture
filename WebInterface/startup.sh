#!/bin/bash
set -e

echo "AI MultiBarcode Capture - Container Startup"
echo "==========================================="

# Set default values if not provided
export WEB_PORT=${WEB_PORT:-3500}
export MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-}
export MYSQL_DATABASE=${MYSQL_DATABASE:-barcode_wms}
export MYSQL_USER=${MYSQL_USER:-wms_user}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-wms_password}

# Try to detect host IP if not provided
if [ -z "$HOST_IP" ]; then
    # Method 1: Try to get host IP from Docker gateway
    HOST_IP=$(ip route | grep default | awk '{print $3}' 2>/dev/null | head -1)

    # Method 2: Try to get from hostname resolution
    if [ -z "$HOST_IP" ] || [[ "$HOST_IP" =~ ^172\.(1[6-9]|2[0-9]|3[0-1])\. ]]; then
        HOST_IP=$(hostname -I 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i ~ /^192\.168\./) print $i}' | head -1)
    fi

    # Method 3: Fallback to any non-Docker private IP
    if [ -z "$HOST_IP" ] || [[ "$HOST_IP" =~ ^172\.(1[6-9]|2[0-9]|3[0-1])\. ]]; then
        HOST_IP=$(hostname -I 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i ~ /^(192\.168\.|10\.)/ && $i !~ /^172\.(1[6-9]|2[0-9]|3[0-1])\./) print $i}' | head -1)
    fi
fi

export HOST_IP

echo "Web Port: $WEB_PORT"
echo "Database: $MYSQL_DATABASE"
echo "Host IP: $HOST_IP"

# Configure Apache port
echo "Listen $WEB_PORT" > /etc/apache2/ports.conf
echo "Listen 80" >> /etc/apache2/ports.conf

# Replace port template in VirtualHost
sed -i "s/\${WEB_PORT}/$WEB_PORT/g" /etc/apache2/sites-available/000-default.conf

# Initialize MySQL if not already done
echo "Initializing MySQL data directory..."
if [ ! -d "/var/lib/mysql/mysql" ]; then
    mysqld --initialize-insecure --user=mysql --datadir=/var/lib/mysql
fi

# Start MySQL in safe mode to initialize
echo "Starting MySQL..."
mysqld_safe --user=mysql --datadir=/var/lib/mysql &
MYSQL_PID=$!

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
for i in {1..60}; do
    if mysqladmin ping --silent 2>/dev/null; then
        echo "MySQL is ready!"
        break
    fi
    echo "Waiting for MySQL... ($i/60)"
    sleep 2
done

# Initialize database if needed
echo "Initializing database..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS $MYSQL_DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true
mysql -u root -e "CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';" 2>/dev/null || true
mysql -u root -e "CREATE USER IF NOT EXISTS '$MYSQL_USER'@'localhost' IDENTIFIED BY '$MYSQL_PASSWORD';" 2>/dev/null || true
mysql -u root -e "GRANT ALL PRIVILEGES ON $MYSQL_DATABASE.* TO '$MYSQL_USER'@'%';" 2>/dev/null || true
mysql -u root -e "GRANT ALL PRIVILEGES ON $MYSQL_DATABASE.* TO '$MYSQL_USER'@'localhost';" 2>/dev/null || true
mysql -u root -e "FLUSH PRIVILEGES;" 2>/dev/null || true

# Import database schema
if [ -f /tmp/init.sql ]; then
    echo "Importing database schema..."
    mysql -u root $MYSQL_DATABASE < /tmp/init.sql 2>/dev/null || echo "Schema already exists or import failed (this may be normal)"
fi

echo "Database initialization complete!"

# Start Apache
echo "Starting Apache on port $WEB_PORT..."
apache2ctl -DFOREGROUND
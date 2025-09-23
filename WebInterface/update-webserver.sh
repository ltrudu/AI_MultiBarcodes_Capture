#!/bin/bash

echo "🔄 AI MultiBarcode Capture - Web Server Update"
echo "==============================================="
echo "This script updates the website files in the Docker container"

# Check if Docker container exists
if ! docker ps -a -q -f name=multibarcode-webinterface > /dev/null 2>&1; then
    echo "❌ Error: Docker container 'multibarcode-webinterface' does not exist"
    echo "   Please run start-services.sh first to create the container"
    exit 1
fi

# Check if container is running
if ! docker ps -q -f name=multibarcode-webinterface > /dev/null 2>&1; then
    echo "ℹ️  Container exists but is not running, starting it..."
    if docker start multibarcode-webinterface; then
        echo "✅ Container started successfully"
        echo "⏳ Waiting for services to initialize..."
        sleep 10
    else
        echo "❌ Error: Failed to start container"
        exit 1
    fi
else
    echo "✅ Container is running"
fi

echo ""
echo "📁 [STEP 1] Copying website files to container..."

# Copy main website files
docker cp src/index.html multibarcode-webinterface:/var/www/html/
docker cp src/css multibarcode-webinterface:/var/www/html/
docker cp src/js multibarcode-webinterface:/var/www/html/
docker cp src/lang multibarcode-webinterface:/var/www/html/
docker cp src/lib multibarcode-webinterface:/var/www/html/

echo "🔌 [STEP 2] Copying API files..."
docker cp src/api multibarcode-webinterface:/var/www/html/

echo "⚙️  [STEP 3] Copying configuration files..."
docker cp src/config multibarcode-webinterface:/var/www/html/

echo "🔒 [STEP 4] Setting proper permissions..."
docker exec multibarcode-webinterface bash -c "chown -R www-data:www-data /var/www/html"
docker exec multibarcode-webinterface bash -c "chmod -R 755 /var/www/html"

echo "🔄 [STEP 5] Reloading Apache configuration..."
if docker exec multibarcode-webinterface bash -c "service apache2 reload"; then
    echo ""
    echo "🎉 ==============================================="
    echo "✅ [SUCCESS] Web server updated successfully!"
    echo "🎉 ==============================================="
    echo ""
    echo "📝 The website has been updated with the latest files."
    echo "🌐 You can access it at: http://localhost:3500"
    echo ""
else
    echo "❌ Error: Failed to reload Apache configuration"
    exit 1
fi

echo "✅ Update completed successfully!"
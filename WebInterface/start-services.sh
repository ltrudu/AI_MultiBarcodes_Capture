#!/bin/bash

# AI MultiBarcode Capture - Unified Container Startup Script
# This script builds and starts the unified multibarcode-webinterface container

echo "🚀 AI MultiBarcode Capture - Unified Container Startup"
echo "======================================================"

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Loaded configuration from .env file"
else
    echo "⚠️  Warning: .env file not found, using default values"
fi

# Set defaults
WEB_PORT=${WEB_PORT:-3500}
EXPOSE_PHPMYADMIN=${EXPOSE_PHPMYADMIN:-false}
EXPOSE_MYSQL=${EXPOSE_MYSQL:-false}

echo ""
echo "📋 Configuration Summary:"
echo "- Container Image: multibarcode-webinterface"
echo "- Web Interface Port: $WEB_PORT"
echo "- phpMyAdmin Access: $([ "$EXPOSE_PHPMYADMIN" = "true" ] && echo "Enabled at /phpmyadmin" || echo "Disabled")"
echo "- MySQL Direct Access: $([ "$EXPOSE_MYSQL" = "true" ] && echo "Internal only" || echo "Internal only")"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running or not accessible"
    echo "   Please start Docker Desktop and try again"
    exit 1
fi

# Build the unified image
echo "🔨 Building unified Docker image..."
docker build -t multibarcode-webinterface:latest .

if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to build Docker image"
    exit 1
fi

echo "✅ Docker image built successfully"

# Stop existing container if running
echo "🛑 Stopping existing container (if any)..."
docker-compose down

# Start the unified container
echo "🚀 Starting unified container..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to start container"
    exit 1
fi

echo ""
echo "✅ Container started successfully!"
echo ""

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check container health
CONTAINER_ID=$(docker ps -q -f name=multibarcode-webinterface)
if [ -z "$CONTAINER_ID" ]; then
    echo "❌ Error: Container is not running"
    exit 1
fi

# Test web interface
if curl -s -f http://localhost:$WEB_PORT/ > /dev/null; then
    echo "✅ Web interface is responding"
else
    echo "⚠️  Warning: Web interface may not be ready yet"
fi

echo ""
echo "🎉 AI MultiBarcode Capture is now running!"
echo ""
echo "📍 Access Points:"
echo "- 🌐 Web Management System: http://localhost:$WEB_PORT"

if [ "$EXPOSE_PHPMYADMIN" = "true" ]; then
    echo "- 📊 phpMyAdmin: http://localhost:$WEB_PORT/phpmyadmin"
fi

echo ""
echo "🔧 Management Commands:"
echo "- View logs: docker logs multibarcode-webinterface"
echo "- Stop container: docker-compose down"
echo "- Restart container: docker restart multibarcode-webinterface"
echo ""
echo "📱 Android App Configuration:"
echo "- Endpoint URL: http://YOUR_IP:$WEB_PORT/api/barcodes.php"
echo "- Example: http://192.168.1.100:$WEB_PORT/api/barcodes.php"
echo ""
echo "🎯 Everything is ready! You can now start scanning with your Android app."
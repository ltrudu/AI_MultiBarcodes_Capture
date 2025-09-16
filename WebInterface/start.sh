#!/bin/bash

# Barcode WMS Startup Script
echo "🚀 Starting Barcode WMS..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker Desktop is not running!"
    echo ""
    echo "🔧 To fix this:"
    echo "   1. Start Docker Desktop application"
    echo "   2. Wait for it to fully start (usually takes 30-60 seconds)"
    echo "   3. Look for the Docker whale icon in your system tray"
    echo "   4. Run this script again"
    echo ""
    echo "💡 On Windows:"
    echo "   - Search for 'Docker Desktop' in Start menu"
    echo "   - Or check if it's running in the system tray"
    echo ""
    read -p "Press Enter to continue once Docker Desktop is started, or Ctrl+C to exit..."

    # Check again after user confirmation
    if ! docker info > /dev/null 2>&1; then
        echo "❌ Docker Desktop is still not running. Please start it first."
        exit 1
    else
        echo "✅ Docker Desktop is now running!"
    fi
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install docker-compose first."
    exit 1
fi

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
fi

# Check for port conflicts
echo "🔍 Checking for port conflicts..."

ports=(8080 8081 3306)
for port in "${ports[@]}"; do
    if lsof -i :$port > /dev/null 2>&1; then
        echo "⚠️  Port $port is already in use. Please stop the service using this port or change the port in docker-compose.yml"
        echo "   You can check what's using the port with: lsof -i :$port"
        read -p "Continue anyway? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
done

# Stop any existing containers
echo "🛑 Stopping any existing containers..."
docker-compose down

# Pull latest images
echo "📥 Pulling latest Docker images..."
docker-compose pull

# Build and start services
echo "🏗️  Building and starting services..."
docker-compose up -d --build

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 10

# Check service health
echo "🏥 Checking service health..."

# Check web service
if curl -s http://localhost:8080 > /dev/null; then
    echo "✅ Web service is running at http://localhost:8080"
else
    echo "❌ Web service is not responding"
fi

# Check database service
if docker-compose exec db mysqladmin ping -h localhost -u root -proot_password > /dev/null 2>&1; then
    echo "✅ Database service is running"
else
    echo "❌ Database service is not responding"
fi

# Check phpMyAdmin
if curl -s http://localhost:8081 > /dev/null; then
    echo "✅ phpMyAdmin is running at http://localhost:8081"
else
    echo "❌ phpMyAdmin is not responding"
fi

echo ""
echo "🎉 Barcode WMS is starting up!"
echo ""
echo "📋 Service URLs:"
echo "   🌐 WMS Dashboard:   http://localhost:8080 (HTTP)"
echo "   🔒 WMS Dashboard:   https://localhost:8443 (HTTPS)"
echo "   🗄️  phpMyAdmin:      http://localhost:8081"
echo "   📡 API Endpoint:     https://localhost:8443/api/barcodes.php (HTTPS)"
echo "   📡 API Endpoint:     http://localhost:8080/api/barcodes.php (HTTP)"
echo ""
echo "🔐 Default Credentials:"
echo "   MySQL Root: root / root_password"
echo "   MySQL WMS:  wms_user / wms_password"
echo ""
echo "📱 Android App Configuration:"
echo "   HTTPS Endpoint: https://192.168.1.188:8443/api/barcodes.php"
echo "   HTTP Endpoint:  http://192.168.1.188:8080/api/barcodes.php"
echo "   ⚠️  Note: HTTPS uses self-signed certificate"
echo ""
echo "📊 To view logs: docker-compose logs -f"
echo "🛑 To stop: docker-compose down"
echo "🔄 To restart: docker-compose restart"
echo ""

# Show real-time logs
read -p "Would you like to view real-time logs? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📊 Showing real-time logs (Ctrl+C to exit)..."
    docker-compose logs -f
fi
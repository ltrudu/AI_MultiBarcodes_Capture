#!/bin/bash

# Run standalone WMS Docker container
echo "🚀 Running standalone WMS container..."

# Stop any existing container
docker stop barcode-wms 2>/dev/null
docker rm barcode-wms 2>/dev/null

# Run the container
docker run -d \
    -p 8080:80 \
    --name barcode-wms \
    --restart unless-stopped \
    barcode-wms:latest

if [ $? -eq 0 ]; then
    echo "✅ WMS container is running!"
    echo ""
    echo "🌐 WMS Dashboard: http://localhost:8080"
    echo "📱 Android Endpoint: http://localhost:8080/api/barcodes.php"
    echo ""
    echo "⏳ Waiting for services to start..."
    sleep 10

    # Check if web service is responding
    if curl -s http://localhost:8080 > /dev/null; then
        echo "✅ WMS is ready and responding!"
    else
        echo "⚠️ WMS may still be starting up..."
    fi

    echo ""
    echo "📊 To view logs: docker logs -f barcode-wms"
    echo "🛑 To stop: docker stop barcode-wms"

    # Ask if user wants to see logs
    read -p "View real-time logs? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker logs -f barcode-wms
    fi
else
    echo "❌ Failed to start container!"
    echo "💡 Make sure you built the image first with: ./build-standalone.sh"
    exit 1
fi
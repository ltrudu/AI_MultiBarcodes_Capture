#!/bin/bash

# Build standalone WMS Docker image
echo "🏗️ Building standalone WMS Docker image..."

# Build the image
docker build -f Dockerfile.standalone -t barcode-wms:latest .

if [ $? -eq 0 ]; then
    echo "✅ Successfully built barcode-wms:latest"
    echo ""
    echo "🚀 To run the standalone container:"
    echo "   docker run -d -p 8080:80 --name barcode-wms barcode-wms:latest"
    echo ""
    echo "🌐 Access the WMS at: http://localhost:8080"
    echo "📱 Android endpoint: http://localhost:8080/api/barcodes.php"
    echo ""
    echo "📊 To view logs:"
    echo "   docker logs -f barcode-wms"
    echo ""
    echo "🛑 To stop:"
    echo "   docker stop barcode-wms"
    echo "   docker rm barcode-wms"
else
    echo "❌ Build failed!"
    exit 1
fi
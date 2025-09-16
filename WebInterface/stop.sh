#!/bin/bash

# Barcode WMS Stop Script
echo "🛑 Stopping Barcode WMS..."

# Stop all services
docker-compose down

echo "✅ All services stopped."
echo ""
echo "💡 To remove all data (including database):"
echo "   docker-compose down -v"
echo ""
echo "🔄 To start again:"
echo "   ./start.sh"
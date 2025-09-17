# Docker WMS Deployment - AI MultiBarcode Capture

This guide provides complete instructions for deploying and using the Web Management System (WMS) using Docker. The WMS allows you to monitor barcode scanning activities in real-time from multiple Android devices through a web interface.

## 🎯 What You'll Achieve

By the end of this guide, you'll have:
- ✅ A running web management system accessible via browser
- ✅ Real-time monitoring of barcode scanning sessions
- ✅ Centralized data management for multiple Android devices
- ✅ Database administration interface
- ✅ Session export and management capabilities

## 📋 Prerequisites

Before starting, ensure you have:
- **Docker Desktop** installed and running
- **Git** for cloning the repository
- **Network connectivity** between your computer and Android devices
- **Web browser** (Chrome, Firefox, Safari, Edge)

## 🚀 Quick Start Deployment

### Step 1: Clone and Navigate
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture/WebInterface
```

### Step 2: Start the WMS
```bash
docker-compose up -d
```

This single command starts all required services:
- **Web Interface** (Port 8080) - Main WMS dashboard
- **MySQL Database** (Port 3306) - Data storage
- **phpMyAdmin** (Port 8081) - Database administration

### Step 3: Verify Deployment
```bash
# Check all containers are running
docker-compose ps

# Expected output:
# NAME                          IMAGE               STATUS
# webinterface-web-1           webinterface-web    Up
# webinterface-db-1            mysql:8.0          Up
# webinterface-phpmyadmin-1    phpmyadmin:latest  Up
```

### Step 4: Access the WMS
Open your web browser and navigate to:
- **Main WMS Dashboard**: http://localhost:8080
- **Database Admin**: http://localhost:8081

You should see the WMS interface with an empty session list (until Android devices start uploading data).

## 📱 Configure Android Devices

### Step 1: Find Your Computer's IP Address

**On Windows:**
```cmd
ipconfig
```
Look for your IPv4 address (e.g., 192.168.1.100)

**On Linux/macOS:**
```bash
ip addr show
# or
ifconfig
```

### Step 2: Configure Android App
1. Open the **AI MultiBarcode Capture** app on your Android device
2. Tap **Settings** (gear icon)
3. Select **Processing Mode** → **HTTP(s) Post**
4. Enter **HTTP(s) Endpoint**:
   ```
   http://YOUR_COMPUTER_IP:8080/api/barcodes.php
   ```
   Example: `http://192.168.1.100:8080/api/barcodes.php`
5. Ensure **Authentication** is disabled
6. Tap **Save**

## 🔄 Using the WMS

### Real-Time Session Monitoring
1. **Start Scanning**: Use the Android app to scan barcodes
2. **Upload Session**: Tap the upload button in the Android app
3. **View in WMS**: Sessions appear in the web dashboard within 1 second

### Session Management Features
- **Live Dashboard**: Automatic refresh every second
- **Session Details**: Click any session to view all captured barcodes
- **Device Information**: See which device captured each session
- **Export Data**: Download session data in various formats
- **Reset Data**: Clear all sessions and start fresh

### WMS Interface Overview

#### Main Dashboard
- **Sessions List**: All uploaded sessions with timestamps
- **Device Column**: Shows which Android device uploaded each session
- **Barcode Count**: Number of barcodes in each session
- **Upload Time**: When the session was received

#### Session Details View
- **Barcode Data**: Complete list of all captured barcodes
- **Symbology Types**: What type each barcode is (QR Code, Code 128, etc.)
- **Timestamps**: When each barcode was scanned
- **Export Options**: Download as Excel, CSV, or text file

## 🔧 Managing the WMS

### Starting and Stopping
```bash
# Start the WMS
docker-compose up -d

# Stop the WMS
docker-compose down

# Restart the WMS
docker-compose restart

# View logs
docker-compose logs -f
```

### Data Management
```bash
# View database logs
docker-compose logs db

# Access database directly
docker exec -it webinterface-db-1 mysql -u root -p

# Backup data (optional)
docker exec webinterface-db-1 mysqldump -u root -p barcode_capture > backup.sql
```

### Clearing All Data
To reset the system and clear all sessions:
1. Open the WMS dashboard: http://localhost:8080
2. Click the **"Reset All Data"** button
3. Confirm the reset operation
4. All sessions and barcodes will be deleted

## 🌐 Network Configuration

### Multiple Device Setup
For scanning with multiple Android devices:
1. Ensure all devices are on the same WiFi network
2. Configure each device with the same endpoint URL
3. Each device will appear separately in the WMS with unique hostnames

### Firewall Configuration
If devices cannot connect:
```bash
# Windows - Allow port 8080
netsh advfirewall firewall add rule name="Docker WMS" dir=in action=allow protocol=TCP localport=8080

# Linux - Allow port 8080
sudo ufw allow 8080
```

## 🔍 Troubleshooting

### Common Issues and Solutions

#### "No sessions found" in WMS
**Possible Causes:**
- Android app not configured correctly
- Network connectivity issues
- Docker services not running

**Solutions:**
```bash
# Check Docker services
docker-compose ps

# Check Android app endpoint configuration
# Verify IP address is correct and reachable

# Test endpoint from browser
curl http://YOUR_IP:8080/api/barcodes.php
```

#### Android app shows "Upload Failed"
**Possible Causes:**
- Incorrect endpoint URL
- Network firewall blocking connection
- Docker services not responding

**Solutions:**
```bash
# Check Docker logs
docker-compose logs web

# Verify network connectivity
ping YOUR_COMPUTER_IP

# Test port accessibility
telnet YOUR_COMPUTER_IP 8080
```

#### WMS not accessible from browser
**Possible Causes:**
- Docker containers not running
- Port 8080 already in use
- Firewall blocking access

**Solutions:**
```bash
# Check what's using port 8080
netstat -tulpn | grep :8080

# Stop conflicting services
sudo kill -9 $(lsof -t -i:8080)

# Restart Docker services
docker-compose down && docker-compose up -d
```

### Performance Optimization

#### For High-Volume Scanning
```bash
# Monitor resource usage
docker stats

# Increase database memory (edit docker-compose.yml)
# Add under mysql service:
command: --innodb-buffer-pool-size=1G
```

#### For Multiple Devices
- Ensure stable WiFi network
- Consider using ethernet connection for the host computer
- Monitor network bandwidth if using many devices simultaneously

## 📊 Monitoring and Maintenance

### Health Checks
```bash
# Check all services are healthy
docker-compose ps

# Monitor logs in real-time
docker-compose logs -f

# Check database connectivity
docker exec webinterface-db-1 mysqladmin ping
```

### Regular Maintenance
```bash
# Clean up unused Docker resources
docker system prune

# Update Docker images
docker-compose pull
docker-compose up -d

# Backup database (recommended weekly)
docker exec webinterface-db-1 mysqldump -u root -p barcode_capture > backup_$(date +%Y%m%d).sql
```

## 🎯 Production Considerations

### For Production Use
1. **Use HTTPS**: Configure SSL certificates for secure communication
2. **Strong Passwords**: Change default database passwords
3. **Regular Backups**: Implement automated backup procedures
4. **Monitoring**: Set up monitoring and alerting for the services
5. **Firewall**: Restrict access to necessary IPs only

### Scaling for Enterprise
- Use load balancers for multiple WMS instances
- Implement database clustering for high availability
- Configure container orchestration (Kubernetes) for large deployments
- Set up centralized logging and monitoring

---

**🎉 Congratulations!** You now have a fully functional Web Management System for monitoring barcode scanning activities across multiple Android devices. The system provides real-time visibility into scanning operations and centralized data management for enterprise barcode capture workflows.
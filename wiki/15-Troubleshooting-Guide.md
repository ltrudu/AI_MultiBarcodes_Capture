# 🔧 Troubleshooting Guide - AI MultiBarcode Capture

This comprehensive guide helps you resolve common issues with the AI MultiBarcode Capture system.

## Common Docker Issues

### ❌ "Docker Desktop is not running" Error

**Error Message:**
```
unable to get image 'multibarcode-webinterface': error during connect: Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.51/images/multibarcode-webinterface/json": open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.
```

**Solution:**

1. **Start Docker Desktop:**
   - Search for "Docker Desktop" in Windows Start menu
   - Click to start the application
   - Wait for the Docker whale icon to appear in system tray
   - Look for "Docker Desktop is running" status

2. **Verify Docker is running:**
   ```bash
   docker --version    # Should show version
   docker info         # Should show Docker daemon info
   ```

3. **If Docker Desktop won't start:**
   - Restart your computer
   - Check Windows updates
   - Reinstall Docker Desktop if necessary

### ⚠️ "Version is obsolete" Warning

**Warning Message:**
```
the attribute `version` is obsolete, it will be ignored, please remove it to avoid potential confusion
```

**Solution:** ✅ **Already Fixed** - The docker-compose.yml has been updated to remove the obsolete version field.

### 🔌 Port Already in Use

**Error Message:**
```
Error starting userland proxy: listen tcp4 0.0.0.0:3500: bind: address already in use
```

**Solution:**
1. **Find what's using the port:**
   ```bash
   # Windows
   netstat -ano | findstr :3500

   # Linux/macOS
   netstat -tulpn | grep :3500
   ```

2. **Stop the conflicting service or change ports in .env file:**
   ```bash
   # Edit .env file to use different port
   WEB_PORT=3501  # Change from 3500 to 3501
   ```

### 💾 Database Connection Issues

**Symptoms:** Web page loads but shows database errors

**Solution:**
1. **Wait for database initialization** (can take 1-2 minutes on first run)
2. **Check unified container logs:**
   ```bash
   docker logs multibarcode-webinterface
   ```
3. **Restart services:**
   ```bash
   docker-compose down && docker-compose up -d
   ```

### 🌐 Web Service Not Responding

**Solution:**
1. **Check unified container status:**
   ```bash
   docker ps -f name=multibarcode-webinterface
   ```

2. **View container logs:**
   ```bash
   docker logs multibarcode-webinterface
   ```

3. **Rebuild container:**
   ```bash
   docker-compose down
   ./start-services.sh  # Windows: start-services.bat
   ```

## Step-by-Step Startup Process

### 🚀 Method 1: Easy Start (Recommended)
```bash
cd WebInterface

# Windows
start-services.bat

# Linux/macOS
./start-services.sh
```

### 🔧 Method 2: Manual Steps
```bash
cd WebInterface

# 1. Ensure Docker Desktop is running
docker info

# 2. Build and start unified container
docker build -t multibarcode-webinterface:latest .
docker-compose up -d

# 3. Wait for initialization
sleep 30

# 4. Check status
docker ps -f name=multibarcode-webinterface

# 5. View logs if needed
docker logs multibarcode-webinterface -f
```

### 🛑 How to Stop
```bash
# Stop container but keep data
docker-compose down

# Stop container and remove all data
docker-compose down -v
```

## Verification Steps

### ✅ Check Container Is Running
```bash
# Container should show "Up"
docker ps -f name=multibarcode-webinterface

# Expected output:
# CONTAINER ID   IMAGE                        STATUS
# abc123def456   multibarcode-webinterface    Up
```

### ✅ Test Web Interfaces
- **WMS Dashboard:** http://localhost:3500 ← Should show AI MultiBarcode Capture WMS interface
- **phpMyAdmin:** http://localhost:3500/phpmyadmin ← Available if enabled in .env (EXPOSE_PHPMYADMIN=true)
- **API Test:** http://localhost:3500/api/barcodes.php ← Should return JSON

### ✅ Test Android Connection
In your Android app:
1. Set Processing Mode to "HTTP(s) Post"
2. Set Endpoint to: `http://YOUR_PC_IP:3500/api/barcodes.php`
3. Capture some barcodes
4. Check WMS dashboard for received data

## Log Files and Debugging

### 📊 View Container Logs
```bash
# View all logs from unified container
docker logs multibarcode-webinterface -f

# View last 100 lines
docker logs multibarcode-webinterface --tail 100
```

### 📊 View Specific Service Logs Inside Container
```bash
# Access container shell
docker exec -it multibarcode-webinterface /bin/bash

# View Apache logs
tail -f /var/log/apache2/access.log
tail -f /var/log/apache2/error.log

# View MySQL logs
tail -f /var/log/mysql/error.log

# View PHP logs
tail -f /var/log/php8.2-fpm.log
```

### 🔍 Debug API Issues
```bash
# Test API endpoint directly
curl -X GET http://localhost:3500/api/barcodes.php

# Test POST with sample data
curl -X POST http://localhost:3500/api/barcodes.php \
  -H "Content-Type: application/json" \
  -d '{"session_timestamp":"2024-01-01T12:00:00.000Z","device_info":"Test Device","barcodes":[{"value":"TEST123","symbology":9,"quantity":1,"timestamp":"2024-01-01T12:00:00.000Z"}]}'
```

## Android App Issues

### ❌ "Upload Failed" Error

**Possible Causes:**
- Incorrect endpoint URL
- Network connectivity issues
- Firewall blocking port 3500
- Docker container not running

**Solutions:**
```bash
# 1. Check container is running
docker ps -f name=multibarcode-webinterface

# 2. Test endpoint accessibility
curl http://YOUR_IP:3500/api/barcodes.php

# 3. Check firewall (Windows)
netsh advfirewall firewall add rule name="AI MultiBarcode WMS" dir=in action=allow protocol=TCP localport=3500

# 4. Check firewall (Linux)
sudo ufw allow 3500

# 5. Test from Android device network
ping YOUR_PC_IP  # Replace with your computer's IP
```

### ❌ "No sessions found" in Web Interface

**Solutions:**
1. **Verify Android app uploaded successfully**
2. **Check API endpoint in Android app settings:**
   - Should be: `http://YOUR_PC_IP:3500/api/barcodes.php`
   - Ensure Authentication is disabled
3. **Check container logs for errors:**
   ```bash
   docker logs multibarcode-webinterface --tail 50
   ```

### ❌ Android App Can't Find IP Address

**Solution:** Find your computer's IP address:
```bash
# Windows
ipconfig

# Linux/macOS
ip addr show
# or
ifconfig

# Look for IPv4 address (usually 192.168.x.x or 10.x.x.x)
```

## Performance Issues

### 🐌 Slow Web Interface

**Solutions:**
1. **Increase container resources:**
   ```yaml
   # Add to docker-compose.yml
   services:
     multibarcode-webinterface:
       deploy:
         resources:
           limits:
             memory: 2G
             cpus: '1.0'
   ```

2. **Clear browser cache and refresh page**

3. **Check for large session datasets and archive old data**

### 🐌 Slow Barcode Upload

**Solutions:**
1. **Check network latency:**
   ```bash
   ping YOUR_PC_IP
   ```

2. **Reduce session size in Android app**

3. **Check Docker container resources:**
   ```bash
   docker stats multibarcode-webinterface
   ```

## Common Questions

### ❓ Can I change the ports?

Yes! Edit the `.env` file:
```bash
# Change WMS port from 3500 to something else
WEB_PORT=8080

# Enable optional services
EXPOSE_PHPMYADMIN=true  # Enables phpMyAdmin access
EXPOSE_MYSQL=true       # Enables direct MySQL access
```

### ❓ How do I reset everything?

```bash
# Stop container and remove all data
docker-compose down -v

# Remove container image (forces rebuild)
docker rmi multibarcode-webinterface:latest

# Fresh start
./start-services.sh  # Windows: start-services.bat
```

### ❓ How do I backup the database?

```bash
# Create backup
docker exec multibarcode-webinterface mysqldump -u root -proot_password barcode_wms > backup.sql

# Restore backup (after fresh installation)
docker exec -i multibarcode-webinterface mysql -u root -proot_password barcode_wms < backup.sql
```

### ❓ How do I access from other devices on the network?

Replace `localhost` with your computer's IP address:
- Find your IP: `ipconfig` (Windows) or `ip addr` (Linux)
- Use: `http://YOUR_IP:3500` instead of `http://localhost:3500`
- Ensure firewall allows port 3500

### ❓ Can I use HTTPS instead of HTTP?

For production use, implement HTTPS by:
1. **Using a reverse proxy like nginx**
2. **Obtaining SSL certificates**
3. **Configuring Docker to use certificates**

## Getting Help

If you're still having issues:

1. **Check this troubleshooting guide first**
2. **Run the startup script with diagnostics:**
   ```bash
   ./start-services.sh  # Includes built-in diagnostics
   ```
3. **Collect logs:**
   ```bash
   docker logs multibarcode-webinterface > wms-logs.txt
   ```
4. **Check Docker Desktop status in system tray**
5. **Verify your network/firewall isn't blocking port 3500**
6. **Test with a simple curl command:**
   ```bash
   curl http://localhost:3500/api/barcodes.php
   ```

## Advanced Troubleshooting

### 🔧 Container Won't Start

```bash
# Check Docker daemon
docker info

# View detailed container startup logs
docker-compose up --no-detach

# Check for port conflicts
netstat -tulpn | grep 3500

# Force rebuild
docker-compose build --no-cache
```

### 🔧 Database Issues Inside Container

```bash
# Access container shell
docker exec -it multibarcode-webinterface /bin/bash

# Check MySQL status
service mysql status

# Restart MySQL inside container
service mysql restart

# Check database tables
mysql -u root -proot_password barcode_wms -e "SHOW TABLES;"
```

### 🔧 Web Server Issues Inside Container

```bash
# Access container shell
docker exec -it multibarcode-webinterface /bin/bash

# Check Apache status
service apache2 status

# Restart Apache inside container
service apache2 restart

# Check PHP configuration
php -m | grep mysql  # Should show mysql extension
```

---

**💡 Pro Tip:** Most issues can be resolved by stopping the container (`docker-compose down`) and restarting with the startup script (`./start-services.sh` or `start-services.bat`). This ensures all services start in the correct order with proper initialization.
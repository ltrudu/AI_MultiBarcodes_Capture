# 🔧 Troubleshooting Guide - Barcode WMS

## Common Docker Issues

### ❌ "Docker Desktop is not running" Error

**Error Message:**
```
unable to get image 'webinterface-web': error during connect: Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.51/images/webinterface-web/json": open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.
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
Error starting userland proxy: listen tcp4 0.0.0.0:8080: bind: address already in use
```

**Solution:**
1. **Find what's using the port:**
   ```bash
   netstat -ano | findstr :8080
   ```

2. **Stop the conflicting service or change ports in docker-compose.yml:**
   ```yaml
   ports:
     - "8081:80"  # Change 8080 to 8081
   ```

### 💾 Database Connection Issues

**Symptoms:** Web page loads but shows database errors

**Solution:**
1. **Wait for database initialization** (can take 1-2 minutes on first run)
2. **Check database logs:**
   ```bash
   docker-compose logs db
   ```
3. **Restart services:**
   ```bash
   docker-compose restart
   ```

### 🌐 Web Service Not Responding

**Solution:**
1. **Check service status:**
   ```bash
   docker-compose ps
   ```

2. **View web service logs:**
   ```bash
   docker-compose logs web
   ```

3. **Rebuild containers:**
   ```bash
   docker-compose down
   docker-compose up -d --build
   ```

## Step-by-Step Startup Process

### 🚀 Method 1: Easy Start (Recommended)
```bash
cd WebInterface
./start.sh
```

### 🔧 Method 2: Manual Steps
```bash
cd WebInterface

# 1. Ensure Docker Desktop is running
docker info

# 2. Start services
docker-compose up -d

# 3. Wait for initialization
sleep 30

# 4. Check status
docker-compose ps

# 5. View logs if needed
docker-compose logs -f
```

### 🛑 How to Stop
```bash
# Stop services but keep data
docker-compose down

# Stop services and remove all data
docker-compose down -v
```

## Verification Steps

### ✅ Check Services Are Running
```bash
# All services should show "Up"
docker-compose ps

# Expected output:
# webinterface-web-1         Up    0.0.0.0:8080->80/tcp
# webinterface-db-1          Up    0.0.0.0:3306->3306/tcp
# webinterface-phpmyadmin-1  Up    0.0.0.0:8081->80/tcp
```

### ✅ Test Web Interfaces
- **WMS Dashboard:** http://localhost:8080 ← Should show Barcode WMS interface
- **phpMyAdmin:** http://localhost:8081 ← Should show database login
- **API Test:** http://localhost:8080/api/barcodes.php ← Should return JSON

### ✅ Test Android Connection
In your Android app:
1. Set Processing Mode to "HTTPS Post"
2. Set Endpoint to: `http://YOUR_PC_IP:8080/api/barcodes.php`
3. Capture some barcodes
4. Check WMS dashboard for received data

## Log Files and Debugging

### 📊 View All Logs
```bash
docker-compose logs -f
```

### 📊 View Specific Service Logs
```bash
docker-compose logs -f web        # Web server logs
docker-compose logs -f db         # Database logs
docker-compose logs -f phpmyadmin # phpMyAdmin logs
```

### 🔍 Debug API Issues
```bash
# Test API endpoint directly
curl -X GET http://localhost:8080/api/barcodes.php

# Test POST with sample data
curl -X POST http://localhost:8080/api/barcodes.php \
  -H "Content-Type: application/json" \
  -d '{"session_timestamp":"2024-01-01T12:00:00.000Z","barcodes":[{"value":"TEST123","symbology":9,"quantity":1,"timestamp":"2024-01-01T12:00:00.000Z"}]}'
```

## Common Questions

### ❓ Can I change the ports?
Yes! Edit `docker-compose.yml` and change the port mappings:
```yaml
ports:
  - "8080:80"  # Change 8080 to your preferred port
```

### ❓ How do I reset everything?
```bash
docker-compose down -v  # Removes all data
docker-compose up -d    # Fresh start
```

### ❓ How do I backup the database?
```bash
docker-compose exec db mysqldump -u root -proot_password barcode_wms > backup.sql
```

### ❓ How do I access from other devices?
Replace `localhost` with your computer's IP address:
- Find your IP: `ipconfig` (Windows) or `ip addr` (Linux)
- Use: `http://YOUR_IP:8080` instead of `http://localhost:8080`

## Getting Help

If you're still having issues:

1. **Check this troubleshooting guide first**
2. **Run the diagnostic script:**
   ```bash
   ./start.sh  # It includes built-in diagnostics
   ```
3. **Collect logs:**
   ```bash
   docker-compose logs > wms-logs.txt
   ```
4. **Check Docker Desktop status in system tray**
5. **Verify your network/firewall isn't blocking the ports**
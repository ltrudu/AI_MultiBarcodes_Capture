# Installation Guide - AI MultiBarcode Capture

This guide provides simple, step-by-step instructions to get the AI MultiBarcode Capture system running quickly.

## 📋 Prerequisites

Before starting, ensure you have:
- **Docker Desktop** installed and running
- **Git** for cloning the repository
- **Android Studio** (for building the Android app)
- **Android device** with USB debugging enabled

### Quick Verification
```bash
# Verify Docker installation
docker --version
docker-compose --version

# Check available ports (should return nothing)
netstat -an | grep :8080
```

## 🚀 Quick Installation

### Step 1: Clone Repository
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture
```

### Step 2: Start Docker Web Management System
```bash
cd WebInterface
docker-compose up -d
```

This starts all required services:
- **Web Interface** (Port 8080) - Main dashboard
- **MySQL Database** (Port 3306) - Data storage
- **phpMyAdmin** (Port 8081) - Database admin

### Step 3: Verify Installation
```bash
# Check all containers are running
docker-compose ps

# Expected output:
# NAME                          IMAGE               STATUS
# webinterface-web-1           webinterface-web    Up
# webinterface-db-1            mysql:8.0          Up
# webinterface-phpmyadmin-1    phpmyadmin:latest  Up
```

### Step 4: Test Web Interface
Open your browser and navigate to:
- **Main Dashboard**: http://localhost:8080
- **Database Admin**: http://localhost:8081

You should see the WMS interface with an empty session list.

### Step 5: Build Android App
```bash
# Return to project root
cd ..

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install AI_MultiBarcodes_Capture/build/outputs/apk/debug/AI_MultiBarcodes_Capture-debug.apk
```

### Step 6: Configure Android App
1. Find your computer's IP address:
   ```bash
   # Windows
   ipconfig

   # Linux/macOS
   ip addr show
   ```

2. Open the **AI MultiBarcode Capture** app on your device
3. Tap **Settings** → **Processing Mode** → **HTTP(s) Post**
4. Enter endpoint: `http://YOUR_IP:8080/api/barcodes.php`
5. Ensure **Authentication** is disabled
6. Tap **Save**

## ✅ Verification Test

1. **Scan Barcodes**: Use the Android app to scan some barcodes
2. **Upload Session**: Tap the upload button in the app
3. **Check WMS**: Open http://localhost:8080 in your browser
4. **Verify Data**: You should see your session appear within 1 second

## 🔧 Basic Management

### Start/Stop the System
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f
```

### Clear All Data
1. Open http://localhost:8080
2. Click **"Reset All Data"** button
3. Confirm to clear all sessions

## 🚨 Troubleshooting

### Common Issues

**"No sessions found" in web interface:**
- Check that Docker containers are running: `docker-compose ps`
- Verify Android app uploaded successfully
- Check endpoint URL is correct

**Android app shows "Upload Failed":**
- Verify IP address is correct and reachable
- Check firewall isn't blocking port 8080
- Test endpoint: `curl http://YOUR_IP:8080/api/barcodes.php`

**Can't access web interface:**
- Check if port 8080 is already in use: `netstat -tulpn | grep :8080`
- Restart Docker services: `docker-compose restart`

## 🎯 Next Steps

After successful installation:
- **[Android App Configuration](07-Android-App-Configuration.md)** - Detailed device setup
- **[Docker WMS Deployment](10-Docker-WMS-Deployment.md)** - Complete WMS usage guide
- **[Troubleshooting Guide](15-Troubleshooting-Guide.md)** - Detailed problem solving

---

**🎉 Congratulations!** You now have a fully functional AI MultiBarcode Capture system with real-time web monitoring capabilities.
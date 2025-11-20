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
netstat -an | grep :3500
```

## 🚀 Quick Installation

### Step 1: Clone Repository
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture
```

### Step 2: Start Unified Container
```bash
cd WebInterface

# Windows
start-services.bat

# Linux/macOS
./start-services.sh
```

This builds and starts the unified multibarcode-webinterface container containing:
- **Apache + PHP Web Server** - Serves the main dashboard and API
- **MySQL Database** - Internal database for session storage
- **phpMyAdmin** - Database administration (optional)

All services run in a single, unified Docker container for simplicity.

### Step 3: Verify Installation
```bash
# Check the unified container is running
docker ps

# Expected output:
# CONTAINER ID   IMAGE                        STATUS
# abc123def456   multibarcode-webinterface    Up
```

### Step 4: Test Web Interface
Open your browser and navigate to:
- **Main Dashboard**: http://localhost:3500

You should see the WMS interface with an empty session list.

*Note: phpMyAdmin is available at /phpmyadmin if enabled in .env file (EXPOSE_PHPMYADMIN=true).*

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
4. Enter endpoint: `http://YOUR_IP:3500/api/barcodes.php`
5. Ensure **Authentication** is disabled
6. Tap **Save**

## ✅ Verification Test

1. **Scan Barcodes**: Use the Android app to scan some barcodes
2. **Upload Session**: Tap the upload button in the app
3. **Check WMS**: Open http://localhost:3500 in your browser
4. **Verify Data**: You should see your session appear within 1 second

## 🔧 Basic Management

### Start/Stop the System
```bash
# Start the unified container
./start-services.sh    # Linux/macOS
start-services.bat     # Windows

# Stop the container
docker-compose down

# View logs
docker logs multibarcode-webinterface
```

### Clear All Data
1. Open http://localhost:3500
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
- Check firewall isn't blocking port 3500
- Test endpoint: `curl http://YOUR_IP:3500/api/barcodes.php`

**Can't access web interface:**
- Check if port 3500 is already in use: `netstat -tulpn | grep :3500`
- Restart Docker services: `docker-compose restart`

## 🎯 Next Steps

After successful installation:
- **[Android App Configuration](07-Android-App-Configuration.md)** - Detailed device setup
- **[Docker WMS Deployment](10-Docker-WMS-Deployment.md)** - Complete WMS usage guide
- **[Troubleshooting Guide](15-Troubleshooting-Guide.md)** - Detailed problem solving

---

**🎉 Congratulations!** You now have a fully functional AI MultiBarcode Capture system with real-time web monitoring capabilities.
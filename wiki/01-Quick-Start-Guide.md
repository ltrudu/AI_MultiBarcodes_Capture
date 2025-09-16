# Quick Start Guide - AI MultiBarcode Capture

Get the AI MultiBarcode Capture system running in 15 minutes with this streamlined setup guide.

## 🎯 What You'll Achieve

By the end of this guide, you'll have:
- ✅ A running web management system accessible via browser
- ✅ A configured Android barcode scanner app
- ✅ Real-time data synchronization between device and web interface
- ✅ Complete barcode capture and management workflow

## 📋 Prerequisites Checklist

Before starting, ensure you have:

- [ ] **Windows/Linux/macOS** computer with Docker support
- [ ] **Docker Desktop** installed and running
- [ ] **Android device** with camera (Zebra device recommended)
- [ ] **Network connectivity** between computer and Android device
- [ ] **Git** for cloning the repository

## 🚀 Step 1: Clone and Setup

### 1.1 Clone the Repository
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture
```

### 1.2 Verify Docker Installation
```bash
docker --version
docker-compose --version
```

Expected output:
```
Docker version 24.0.x
Docker Compose version v2.x.x
```

## 🐳 Step 2: Start the Docker Environment

### 2.1 Navigate to Web Interface Directory
```bash
cd WebInterface
```

### 2.2 Start All Services
```bash
docker-compose up -d
```

This command starts:
- **Apache Web Server** (Port 8080)
- **MySQL Database** (Port 3306)
- **phpMyAdmin** (Port 8081)

### 2.3 Verify Services Are Running
```bash
docker-compose ps
```

Expected output:
```
NAME                    IMAGE               STATUS
webinterface-db-1       mysql:8.0          Up
webinterface-web-1      webinterface-web    Up
webinterface-phpmyadmin-1 phpmyadmin:latest Up
```

### 2.4 Test Web Interface
Open your browser and navigate to:
- **Main WMS Interface**: http://localhost:8080
- **Database Admin**: http://localhost:8081

You should see the Zebra-branded WMS interface with an empty session list.

## 📱 Step 3: Build the Android App

### 3.1 Open Project in Android Studio
1. Launch **Android Studio**
2. Open the project directory: `AI_MultiBarcode_Capture`
3. Wait for Gradle sync to complete

### 3.2 Build Debug APK
Using Android Studio:
1. Go to **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Wait for build completion

Or using command line:
```bash
# From project root directory
./gradlew assembleDebug
```

### 3.3 Install on Android Device
1. Enable **Developer Options** and **USB Debugging** on your device
2. Connect device via USB
3. Install the APK:
   ```bash
   adb install AI_MultiBarcodes_Capture/build/outputs/apk/debug/AI_MultiBarcodes_Capture-debug.apk
   ```

## 🔧 Step 4: Configure Android App

### 4.1 Find Your Computer's IP Address

**On Windows:**
```bash
ipconfig
```
Look for your IPv4 address (e.g., 192.168.1.100)

**On Linux/macOS:**
```bash
ip addr show
# or
ifconfig
```

### 4.2 Configure HTTP Endpoint in App
1. Launch **AI MultiBarcode Capture** app on your device
2. Tap **Settings** (gear icon)
3. Select **Processing Mode** → **HTTP(s) Post**
4. Enter **HTTP(s) Endpoint**:
   ```
   http://YOUR_COMPUTER_IP:8080/api/barcodes.php
   ```
   Example: `http://192.168.1.100:8080/api/barcodes.php`
5. Ensure **Authentication** is disabled (unchecked)
6. Tap **Save**

## 📸 Step 5: Test End-to-End Workflow

### 5.1 Start Barcode Scanning
1. In the app, tap **Start Scanning**
2. Grant camera permissions if prompted
3. Point camera at barcodes/QR codes
4. Watch as barcodes are detected and added to the list

### 5.2 Upload Data to WMS
1. After scanning several barcodes, tap the **Upload** button
2. Confirm the upload action
3. Wait for "Upload Successful" message

### 5.3 Verify in Web Interface
1. Open your browser to: http://localhost:8080
2. You should see your captured session appear within 1 second
3. Click on the session to view detailed barcode data
4. Verify device hostname appears in the Device column

## 🎉 Success Verification

You've successfully set up the system if you can:

1. **See sessions** in the web interface immediately after upload
2. **View device hostname** (e.g., "Samsung_Galaxy_S24_Android14") in the Device column
3. **Access session details** showing all captured barcodes with timestamps
4. **Real-time updates** - new uploads appear within 1 second in the web interface

## 🔄 Testing Real-Time Features

### Test 1: Real-Time Session Updates
1. Keep the web interface open
2. Scan and upload from multiple devices simultaneously
3. Watch sessions appear in real-time on the web dashboard

### Test 2: Session Management
1. Click **Reset All Data** button in web interface
2. Confirm the reset operation
3. Verify all data is cleared and counters reset to zero

### Test 3: Device Identification
1. Upload from different Android devices
2. Verify each device shows a unique hostname in the Device column
3. Confirm you can distinguish between different scanning devices

## 🛠️ Quick Troubleshooting

### Common Issues and Solutions

**❌ Web interface shows "No sessions"**
- Check if Docker services are running: `docker-compose ps`
- Verify upload was successful in Android app
- Check browser network tab for API errors

**❌ Android app shows "Upload Failed"**
- Verify IP address is correct and reachable
- Ensure port 8080 is accessible from the device
- Check Docker logs: `docker-compose logs web`

**❌ Can't connect to database**
- Restart Docker services: `docker-compose down && docker-compose up -d`
- Check if port 3306 is already in use
- Verify MySQL container is healthy: `docker-compose logs db`

**❌ Android app won't build**
- Ensure you have Android SDK 35 installed
- Check that Zebra AI Vision SDK dependencies are available
- Verify Java 1.8 compatibility

## 📚 Next Steps

Now that you have a working demonstration:

1. **Explore Advanced Features**: Check out device hostname tracking, session management, and real-time monitoring
2. **Customize the Setup**: Modify the web interface, add authentication, or integrate with existing systems
3. **Scale the Deployment**: Learn about production deployment with HTTPS, load balancing, and security
4. **Develop Custom Integrations**: Use the REST API to integrate with your existing warehouse management systems

Continue with the detailed guides:
- [Docker Setup Guide](04-Docker-Setup-Guide.md) for production deployment
- [Web Service Development](05-Web-Service-Development.md) for custom API development
- [Android App Configuration](07-Android-App-Configuration.md) for advanced device settings

---

**🎯 You now have a fully functional AI MultiBarcode Capture system!** The combination of real-time scanning, instant data synchronization, and comprehensive web management provides a powerful foundation for enterprise barcode management solutions.
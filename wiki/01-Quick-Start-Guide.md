# Quick Start Guide - AI MultiBarcode Capture

Get the AI MultiBarcode Capture system running in 10 minutes with this streamlined XAMPP setup guide.

## 🎯 What You'll Achieve

By the end of this guide, you'll have:
- ✅ A running web management system with secure HTTPS
- ✅ A configured Android barcode scanner app
- ✅ Trusted SSL certificates installed on Windows and Android
- ✅ Real-time data synchronization between device and web interface
- ✅ Complete barcode capture and management workflow

## 📋 Prerequisites Checklist

Before starting, ensure you have:

- [ ] **Windows 10/11** computer (64-bit)
- [ ] **Android device** with camera (Zebra device recommended)
- [ ] **Network connectivity** between computer and Android device
- [ ] **Git** for cloning the repository
- [ ] **2GB free disk space** for XAMPP installation

## 🚀 Step 1: Clone the Repository

```bash
git clone https://github.com/ltrudu/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture
```

## ⚡ Step 2: Quick XAMPP Installation (3 Steps)

### 2.1 Install XAMPP
Navigate to the `XAMPP_Full_Install` folder and run the automated installation:

**For C: drive (recommended):**
```batch
cd XAMPP_Full_Install
install_Xampp_to_C.bat
```

**For D: drive (if C: has limited space):**
```batch
cd XAMPP_Full_Install
install_Xampp_to_D.bat
```

This automatically extracts XAMPP to `C:\xampp` or `D:\xampp`.

### 2.2 Start the Server
```batch
cd ..\WebInterface
xampp_start_server.bat
```

The server will:
- Update all website files automatically
- Start Apache and MySQL
- Configure the database
- Update network IP for QR codes
- Display access URLs

### 2.3 Test Web Interface
Open your browser and navigate to:
- **HTTPS (Secure)**: https://localhost:3543
- **HTTP (Fallback)**: http://localhost:3500

You should see the Zebra-branded WMS interface with an empty session list.

**Note**: You'll see a certificate warning - this is expected. We'll fix this in Step 3.

## 🔐 Step 3: Install SSL Certificates (Enable Secure HTTPS)

### 3.1 Install Certificate on Windows

1. **Open Web Interface**: Navigate to https://localhost:3543 (accept the security warning for now)
2. **Click Settings (⚙️)** in the top navigation bar
3. **Scroll to SSL Certificates section**
4. **Click "📥 Download Windows Certificate"** - saves as `wms-ca-certificate.crt`
5. **Install the certificate**:
   - Right-click on the downloaded `wms-ca-certificate.crt` file
   - Select **"Install Certificate..."**
   - Choose **"Current User"** → **"Next"**
   - Select **"Place all certificates in the following store"**
   - Click **"Browse..."** → Select **"Trusted Root Certification Authorities"**
   - Click **"OK"** → **"Next"** → **"Finish"**
   - Click **"Yes"** on the security warning
6. **Restart your browser** and reload https://localhost:3543
7. **Verify**: You should now see a 🔒 secure connection without warnings!

### 3.2 Install Certificate on Android

1. **Download Certificate to Android**:
   - Open your Android browser
   - Navigate to `http://YOUR_COMPUTER_IP:3500` (use HTTP initially)
   - Go to **Settings (⚙️)** → **SSL Certificates section**
   - Click **"📥 Download Android Certificate"** - saves as `android-ca-certificate.pem`

2. **Install on Android Device**:
   - Go to **Settings** → **Security** → **Encryption & credentials**
   - Tap **"Install from storage"** or **"Install certificates"**
   - Navigate to Downloads and select `android-ca-certificate.pem`
   - When prompted, choose **"CA certificate"**
   - Name it **"WMS CA"**
   - Confirm installation (may show security warning - tap **"Install anyway"**)

3. **Verify Installation**:
   - Go to **Settings** → **Security** → **Trusted credentials** → **User** tab
   - Look for your **"WMS CA"** certificate

## 📱 Step 4: Download and Install Android App

### 4.1 Download the APK

Choose the version that best suits your needs:

**Option A: Latest Stable Version (Recommended)**
- Visit: https://github.com/ZebraDevs/AI-Java-MultiBarcodesCapture/releases
- Download the latest `AI_MultiBarcodes_Capture-release.apk`
- This is the official stable release from Zebra

**Option B: Latest Built Version (Development)**
- Visit: https://github.com/ltrudu/AI_MultiBarcodes_Capture/releases
- Download the latest `AI_MultiBarcodes_Capture-debug.apk` or `AI_MultiBarcodes_Capture-release.apk`
- This may include the latest features but could be a development version

### 4.2 Install on Android Device

Choose one of the following installation methods:

#### Method 1: Direct Installation from Android (Simplest)

1. **Transfer APK to device** (via USB, email, cloud storage, or direct download)
2. **Enable Unknown Sources**:
   - Go to **Settings** → **Security** (or **Apps & notifications**)
   - Enable **"Install unknown apps"** or **"Unknown sources"**
   - Select your file manager or browser and allow installations
3. **Install APK**:
   - Open the downloaded APK file using your file manager
   - Tap **"Install"**
   - Grant necessary permissions
   - Tap **"Open"** when installation completes

#### Method 2: Using ADB (Android Debug Bridge)

1. **Enable USB Debugging** on your device:
   - Go to **Settings** → **About phone**
   - Tap **"Build number"** 7 times to enable Developer Options
   - Go to **Settings** → **Developer options**
   - Enable **"USB debugging"**
2. **Connect device via USB** to your computer
3. **Install using ADB**:
   ```bash
   adb install path/to/AI_MultiBarcodes_Capture.apk
   ```

#### Method 3: Using Zebra StageNow (Zebra Devices)

1. **Create StageNow Profile**:
   - Open **StageNow** on your computer
   - Create a new profile with **"File Manager"** setting
   - Add the APK file to deploy
   - Generate staging barcode
2. **Deploy to Device**:
   - Scan the staging barcode with your Zebra device
   - StageNow will automatically install the APK

#### Method 4: Using EMM (Enterprise Mobility Management)

1. **Upload APK** to your EMM console (e.g., SOTI MobiControl, VMware Workspace ONE, Microsoft Intune)
2. **Create deployment policy** targeting your devices
3. **Push APK** to devices remotely
4. **Installation occurs automatically** based on EMM policy

## 🔧 Step 5: Configure Android App

### 5.1 Using QR Code Configuration (Recommended - Fastest Method)

This is the easiest and fastest way to configure your Android app!

1. **Open WMS Web Interface** on your computer:
   - Navigate to **https://localhost:3543** (or **https://YOUR_COMPUTER_IP:3543**)

2. **Access Settings**:
   - Click the **⚙️ Settings** button in the top navigation bar

3. **Open Endpoint Configuration**:
   - In the Settings modal, find the **"Endpoint Configuration"** section
   - Click **"Show QR Code"** or **"Generate QR Code"** button
   - A QR code will appear containing the complete HTTPS endpoint URL

4. **Scan QR Code with Android App**:
   - Launch **AI MultiBarcode Capture** app on your Android device
   - Open the Menu (icon at the top left of the Welcome Screen of the Application)
   - Tap on **Settings**
   - Select **Processing Mode** → **HTTP(s) Post**
   - Point your device camera at the QR code displayed on the web interface
   - Use the scan button of your device to **scan the QR Code**
   - The endpoint will be **automatically configured**!

5. **Verify Configuration**:
   - Check that **Processing Mode** is set to **"HTTP(s) Post"**
   - Verify the **HTTP(s) Endpoint** shows: `https://YOUR_IP:3543/api/barcodes.php`
   - Tap **Validate**

**That's it!** No manual typing required - the QR code includes the correct IP address and port automatically.

---

### 5.2 Manual Configuration (Alternative Method)

If you prefer to manually configure the endpoint or the QR code method isn't available:

1. **Find Your Computer's IP Address**:

   The IP was already detected during server startup. Check the output from `xampp_start_server.bat` or run:
   ```batch
   cd WebInterface
   xampp_update_network_IP.bat
   ```
   Look for your IPv4 address (e.g., 192.168.1.188)

2. **Configure HTTPS Endpoint in App**:
   - Launch **AI MultiBarcode Capture** app on your device
   - Open the Menu (icon at the top left of the Welcome Screen of the Application)
   - Tap on **Settings**
   - Select **Processing Mode** → **HTTP(s) Post**
   - Enter **HTTP(s) Endpoint** using **HTTPS**:
     ```
     https://YOUR_COMPUTER_IP:3543/api/barcodes.php
     ```
     Example: `https://192.168.1.188:3543/api/barcodes.php`
   - Tap **Validate**

## 📸 Step 6: Test End-to-End Workflow

### 6.1 Start Barcode Scanning
1. In the app, tap **Start Scanning**
2. Grant camera permissions if prompted
3. Point camera at barcodes/QR codes
4. Watch as barcodes are detected and added to the list

### 6.2 Upload Data to WMS
1. After scanning several barcodes, tap the **Upload** button
2. Confirm the upload action
3. Wait for "Upload Successful" message

### 6.3 Verify in Web Interface
1. Open your browser to: **https://localhost:3543** (now with 🔒 secure connection!)
2. You should see your captured session appear within 1 second
3. Click on the session to view detailed barcode data
4. Verify device hostname appears in the Device column

## 🎉 Success Verification

You've successfully set up the system if you can:

1. **See secure HTTPS** connection (🔒) in your browser without warnings
2. **See sessions** in the web interface immediately after upload
3. **View device hostname** (e.g., "Samsung_Galaxy_S24_Android14") in the Device column
4. **Access session details** showing all captured barcodes with timestamps
5. **Real-time updates** - new uploads appear within 1 second in the web interface

## 🔄 Testing Real-Time Features

### Test 1: Secure HTTPS Communication
1. Open browser and verify **https://localhost:3543** shows 🔒 secure connection
2. Check certificate details in browser (should show "WMS Root CA")
3. Upload from Android app - verify successful HTTPS connection

### Test 2: Real-Time Session Updates
1. Keep the web interface open
2. Scan and upload from multiple devices simultaneously
3. Watch sessions appear in real-time on the web dashboard

### Test 3: Session Management
1. Click **Reset All Data** button in web interface
2. Confirm the reset operation
3. Verify all data is cleared and counters reset to zero

### Test 4: Device Identification
1. Upload from different Android devices
2. Verify each device shows a unique hostname in the Device column
3. Confirm you can distinguish between different scanning devices

## 🛠️ Quick Troubleshooting

### Common Issues and Solutions

**❌ Certificate warnings in browser**
- Ensure you installed the certificate in "Trusted Root Certification Authorities"
- Restart your browser after installing the certificate
- Check if certificate is visible in Certificate Manager (`certmgr.msc`)

**❌ Android SSL errors**
- Verify the CA certificate is installed on Android
- Check **Settings** → **Security** → **Trusted credentials** → **User** tab
- Reinstall certificate if not found
- Use HTTP endpoint temporarily for testing: `http://YOUR_IP:3500/api/barcodes.php`

**❌ Web interface shows "No sessions"**
- Check if XAMPP services are running: `tasklist | findstr httpd`
- Verify upload was successful in Android app
- Check browser network tab for API errors
- Ensure database is running: `tasklist | findstr mysqld`

**❌ Android app shows "Upload Failed"**
- Verify IP address is correct and reachable
- Ensure port 3543 (HTTPS) or 3500 (HTTP) is accessible from the device
- Check Windows Firewall allows Apache on these ports
- Verify both devices are on the same WiFi network
- Test connection from Android browser first

**❌ Can't connect to database**
- Restart XAMPP services:
  ```batch
  cd WebInterface
  xampp_stop_server.bat
  xampp_start_server.bat
  ```
- Check MySQL error log: `type C:\xampp\mysql\data\*.err`
- Verify database exists: Navigate to http://localhost:3500/phpmyadmin

**❌ XAMPP services won't start**
- Port conflicts: Check if ports 3500, 3543, or 3306 are in use
- Run: `netstat -ano | findstr "3500 3543 3306"`
- Stop conflicting services (IIS, Skype, other web servers)
- Check Apache error log: `type C:\xampp\apache\logs\error.log`

**❌ Android app won't build**
- Ensure you have Android SDK 35 installed
- Check that Zebra AI Vision SDK dependencies are available
- Verify Java 1.8 compatibility
- Clean and rebuild: `./gradlew clean assembleDebug`

## 📚 Next Steps

Now that you have a working demonstration with secure HTTPS:

1. **Explore Advanced Features**: Check out device hostname tracking, session management, and real-time monitoring
2. **Understand Certificates**: Learn more about SSL/TLS and certificate management
3. **Customize the Setup**: Modify the web interface, add authentication, or integrate with existing systems
4. **Develop Custom Integrations**: Use the REST API to integrate with your existing warehouse management systems
5. **Network Management**: Learn how to handle IP changes when switching networks

Continue with the detailed guides:
- [Installation Guide (XAMPP)](04-Installation-Guide-XAMPP.md) - Complete XAMPP setup and management
- [Generating HTTPS Certificates](13-Generating-HTTPS-Certificates.md) - Advanced certificate generation
- [Installing Server Certificates](14-Installing-Server-Certificates.md) - Detailed certificate installation
- [Understanding Certificates For Beginners](17-Understanding-Certificates-For-Beginners.md) - SSL/TLS basics
- [Android App Configuration](07-Android-App-Configuration.md) - Advanced device settings
- [API Documentation](06-API-Documentation.md) - REST API reference

---

**🎯 You now have a fully functional AI MultiBarcode Capture system with secure HTTPS!** The combination of real-time scanning, encrypted data transmission, instant synchronization, and comprehensive web management provides a powerful foundation for enterprise barcode management solutions.

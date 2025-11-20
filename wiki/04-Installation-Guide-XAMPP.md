# Installation Guide (XAMPP) - AI MultiBarcode Capture
## No Admin Rights Required

This comprehensive guide provides detailed instructions for setting up the AI MultiBarcode Capture system using XAMPP instead of Docker. This method is ideal for environments where Docker cannot be used due to administrative restrictions or organizational policies.

## 🎯 Why XAMPP Instead of Docker?

XAMPP offers several advantages in restricted environments:
- ✅ **No Admin Rights Required** - Can run in portable mode without system installation
- ✅ **No Docker Desktop** - Works without virtualization or containers
- ✅ **Lightweight** - Uses less system resources than Docker
- ✅ **Direct Access** - Easy access to Apache, MySQL, and PHP configurations
- ✅ **Windows Native** - Runs natively on Windows without WSL2

## 📋 Prerequisites

### Required Software
1. **XAMPP** (v8.0 or higher)
   - Download from: https://www.apachefriends.org/
   - Portable version recommended for no-admin environments
   - Includes: Apache 2.4.x, MySQL 8.x, PHP 8.x

2. **Git** (for cloning repository)
   - Download from: https://git-scm.com/
   - Portable version available

3. **Android Studio** (for building the Android app)
   - Download from: https://developer.android.com/studio
   - Required to build the barcode scanner APK

4. **Android Device** with:
   - Camera support
   - USB debugging enabled
   - Android 11 (API 30) or higher

### System Requirements
- **OS**: Windows 10/11 (64-bit)
- **RAM**: 4GB minimum, 8GB recommended
- **Storage**: 2GB free space for XAMPP + 1GB for project
- **Ports**: 3500 (HTTP), 3543 (HTTPS) must be available
- **Network**: Local network connectivity for Android device

### Quick Verification
```bash
# Check if ports are available (should return nothing)
netstat -an | findstr "3500"
netstat -an | findstr "3543"

# Verify Git installation
git --version
```

---

## 🚀 Quick Installation (3 Steps - Recommended)

This is the fastest and easiest way to get started. No manual configuration needed!

### Step 1: Clone the Repository

```batch
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture
```

### Step 2: Install XAMPP

Navigate to the `XAMPP_Full_Install` folder and run the automated installation script:

**For C: drive (recommended):**
```batch
cd XAMPP_Full_Install
install_Xampp_to_C.bat
```

**For D: drive (if C: drive has limited space):**
```batch
cd XAMPP_Full_Install
install_Xampp_to_D.bat
```

The script will automatically:
- Join split archives if needed
- Extract XAMPP to `C:\xampp` or `D:\xampp`
- Configure paths
- Verify installation

### Step 3: Start the Server

```batch
cd AI_MultiBarcode_Capture\WebInterface
xampp_start_server.bat
```

That's it! The server will:
- Update all website files automatically
- Start Apache and MySQL
- Configure the database
- Update network IP for QR codes
- Display access URLs

**Access the web interface at:** https://localhost:3543 or http://localhost:3500

---

## 📋 XAMPP Management Scripts

The `WebInterface` folder contains several batch scripts for managing your XAMPP installation:

### `xampp_start_server.bat` - Start Server with Auto-Update

**Purpose:** Starts the XAMPP server with automatic file updates

**Usage:**
```batch
cd WebInterface
xampp_start_server.bat
```

**What it does:**

#### STEP 1: Updating XAMPP Installation
- Automatically calls `xampp_update_server.bat`
- Detects XAMPP at `C:\xampp` or `D:\xampp`
- Backs up existing htdocs folder
- Copies latest website files from `src/` to XAMPP htdocs
- Applies XAMPP-specific path fixes
- Copies SSL certificates
- Creates Apache HTTP configuration (port 3500)
- Creates Apache HTTPS/SSL configuration (port 3543)
- Updates `httpd.conf` with custom configurations
- Changes Apache port from 80 to 3500
- Enables required modules (mod_rewrite, mod_ssl, mod_headers, mod_expires)
- Updates PHP database configuration

#### STEP 2: Starting XAMPP Services
- Fixes configuration paths for D: drive (if needed)
- Starts MySQL server
- Creates `barcode_wms` database
- Creates database user `wms_user` with password `wms_password`
- Grants all privileges
- Imports database schema (3 tables, 2 views)
- Verifies database installation
- Starts Apache web server
- Verifies all services running

#### STEP 3: Network IP Configuration
- Calls `xampp_update_network_IP.bat`
- Detects local network IP (192.168.x.x or 10.x.x.x)
- Detects external/public IP
- Creates `config/ip-config.json` with current IPs
- Enables QR code generation in web interface
- Displays all access URLs

**When to use:** Every time you want to start the server or after pulling new code changes

---

### `xampp_stop_server.bat` - Stop All Services

**Purpose:** Cleanly stops Apache and MySQL services

**Usage:**
```batch
cd WebInterface
xampp_stop_server.bat
```

**What it does:**
- Stops Apache web server gracefully
- Stops MySQL database server
- Closes all XAMPP-related processes
- Displays confirmation messages

**When to use:**
- Before system shutdown/restart
- When updating XAMPP itself
- When switching between Docker and XAMPP
- To free up ports 3500/3543

---

### `xampp_update_server.bat` - Update Files Only

**Purpose:** Updates website files without restarting services

**Usage:**
```batch
cd WebInterface
xampp_update_server.bat
```

**What it does:**
- Detects XAMPP installation (C:\xampp or D:\xampp)
- Backs up existing htdocs folder
- Copies latest files from `src/` to XAMPP htdocs
- Applies XAMPP-specific path fixes (server-info.php)
- Updates Apache configurations
- Updates SSL certificates
- Updates PHP database configuration
- Does **NOT** restart services

**When to use:**
- Updating PHP/HTML/CSS/JavaScript files only
- Testing new code changes
- When you want minimal downtime
- After pulling code updates

**Note:** PHP file changes take effect immediately. Apache config changes require server restart.

---

### `xampp_update_network_IP.bat` - Update Network Configuration

**Purpose:** Updates IP addresses when changing networks (WiFi, mobile hotspot, etc.)

**Usage:**
```batch
cd WebInterface
xampp_update_network_IP.bat
```

**What it does:**
- Detects XAMPP installation (C:\xampp or D:\xampp)
- Detects current local network IP (192.168.x.x, 10.x.x.x)
- Attempts to detect external/public IP via PowerShell
- Creates/updates `config/ip-config.json` with:
  ```json
  {
    "local_ip": "192.168.1.188",
    "external_ip": "109.221.226.173",
    "last_updated": "2025-11-20T16:34:13Z",
    "detection_method": "xampp_batch_update"
  }
  ```
- Updates web interface endpoint configuration
- Refreshes QR codes with new IPs
- **No service restart needed** - changes take effect immediately

**When to use:**
- After connecting to a different WiFi network
- When switching from WiFi to mobile hotspot
- When Android app can't connect (wrong IP)
- After moving to a new location/network

**Expected Output:**
```
[OK] Local IP detected: 192.168.1.188
[OK] External IP detected: 109.221.226.173
[OK] IP configuration updated successfully
```

---

## 🔄 Common Workflows

### Daily Development Workflow

```batch
# Start working
cd WebInterface
xampp_start_server.bat

# ... do your development work ...

# Stop when done
xampp_stop_server.bat
```

### After Pulling Code Updates

```batch
# Pull latest changes
git pull origin main

# Stop services
cd WebInterface
xampp_stop_server.bat

# Restart with automatic update
xampp_start_server.bat
```

### When Network Changes

```batch
# Just update IPs (no restart needed)
cd WebInterface
xampp_update_network_IP.bat

# Open web interface and use QR code to update Android app
# http://localhost:3500 → Settings → Endpoint Configuration → QR Code
```

### Quick File Update (No Downtime)

```batch
# Update files while services running
cd WebInterface
xampp_update_server.bat

# PHP changes work immediately
# For Apache config changes, restart:
xampp_stop_server.bat
xampp_start_server.bat
```

---

## 📁 XAMPP Installation Structure

After running `install_Xampp_to_C.bat` or `install_Xampp_to_D.bat`, your XAMPP installation will have this structure:

```
C:\xampp\                    OR      D:\xampp\
├── apache\                          ├── apache\
│   ├── bin\                         │   ├── bin\
│   │   ├── httpd.exe                │   │   ├── httpd.exe
│   │   └── ...                      │   │   └── ...
│   ├── conf\                        │   ├── conf\
│   │   ├── httpd.conf               │   │   ├── httpd.conf
│   │   ├── extra\                   │   │   ├── extra\
│   │   │   ├── httpd-multibarcode.conf    │   │   ├── httpd-multibarcode.conf
│   │   │   └── httpd-multibarcode-ssl.conf│   │   └── httpd-multibarcode-ssl.conf
│   │   └── ssl\                     │   │   └── ssl\
│   │       ├── server.crt           │   │       ├── server.crt
│   │       ├── server.key           │   │       ├── server.key
│   │       └── ca.crt               │   │       └── ca.crt
│   ├── logs\                        │   ├── logs\
│   │   ├── error.log                │   │   ├── error.log
│   │   ├── access.log               │   │   ├── access.log
│   │   ├── multibarcode_error.log   │   │   ├── multibarcode_error.log
│   │   └── multibarcode_access.log  │   │   └── multibarcode_access.log
│   └── modules\                     │   └── modules\
├── mysql\                           ├── mysql\
│   ├── bin\                         │   ├── bin\
│   │   ├── mysql.exe                │   │   ├── mysql.exe
│   │   ├── mysqld.exe               │   │   ├── mysqld.exe
│   │   ├── mysqldump.exe            │   │   ├── mysqldump.exe
│   │   └── ...                      │   │   └── ...
│   ├── data\                        │   ├── data\
│   │   └── barcode_wms\             │   │   └── barcode_wms\
│   └── ...                          │   └── ...
├── php\                             ├── php\
│   ├── php.exe                      │   ├── php.exe
│   ├── php.ini                      │   ├── php.ini
│   └── ...                          │   └── ...
├── phpMyAdmin\                      ├── phpMyAdmin\
├── htdocs\                          ├── htdocs\
│   ├── api\                         │   ├── api\
│   │   ├── barcodes.php             │   │   ├── barcodes.php
│   │   ├── server-info.php          │   │   ├── server-info.php
│   │   └── ...                      │   │   └── ...
│   ├── config\                      │   ├── config\
│   │   ├── database.php             │   │   ├── database.php
│   │   └── ip-config.json           │   │   └── ip-config.json
│   ├── index.html                   │   ├── index.html
│   └── ...                          │   └── ...
├── xampp-control.exe                ├── xampp-control.exe
└── setup_xampp.bat                  └── setup_xampp.bat
```

### Key Configuration Files

- **`apache/conf/httpd.conf`** - Main Apache configuration (port 3500, modules)
- **`apache/conf/extra/httpd-multibarcode.conf`** - HTTP virtual host configuration
- **`apache/conf/extra/httpd-multibarcode-ssl.conf`** - HTTPS virtual host configuration
- **`apache/conf/ssl/`** - SSL certificates for HTTPS
- **`htdocs/config/database.php`** - Database connection settings
- **`htdocs/config/ip-config.json`** - Network IP configuration for QR codes
- **`mysql/bin/my.ini`** - MySQL configuration

---

## ✅ Verification and Testing

### Step 1: Verify Services

```batch
# Check if Apache is running
tasklist /FI "IMAGENAME eq httpd.exe"

# Check if MySQL is running
tasklist /FI "IMAGENAME eq mysqld.exe"
```

### Step 2: Verify Web Interface

1. **Open browser** and go to http://localhost:3500 or https://localhost:3543
2. **Check dashboard** - should display "AI MultiBarcode Capture WMS"
3. **Click Settings (⚙️)** in top-right corner
4. **Click "Endpoint Configuration"** - should show your local network IP
5. **Click "QR Code"** button - should display QR code with endpoint URL

### Step 3: Verify Database

1. **Open phpMyAdmin** at http://localhost:3500/phpmyadmin or https://localhost:3543/phpmyadmin
2. **Login** with username: `root` (no password)
3. **Check database** `barcode_wms` exists in sidebar
4. **Verify tables**:
   - `symbologies`
   - `capture_sessions`
   - `barcodes`
5. **Verify views**:
   - `session_statistics`
   - `barcode_details`

### Step 4: Configure Android App

Use the QR code from the web interface:

1. Open AI MultiBarcode Capture app on Android device
2. Go to **Settings**
3. Tap **"Scan QR Code"**
4. Scan the QR code displayed in web interface
5. Endpoint is automatically configured!

Alternatively, manually enter:
- **HTTP Endpoint**: `http://YOUR_IP:3500/api/barcodes.php`
- **HTTPS Endpoint**: `https://YOUR_IP:3543/api/barcodes.php`

### Step 5: Test End-to-End

1. **Scan some barcodes** with Android app
2. **Tap upload button** in app
3. **Check web interface** at http://localhost:3500  or https://localhost:3543
4. **Session should appear** within 1 second
5. **Click session** to view captured barcodes

---

## 🔧 Troubleshooting

### Problem: "XAMPP not found at C:\xampp or D:\xampp"

**Cause**: XAMPP not installed or installed in different location

**Solution**:
1. Check if XAMPP exists:
   ```batch
   dir C:\xampp\apache\bin\httpd.exe
   dir D:\xampp\apache\bin\httpd.exe
   ```
2. If not found, run `install_Xampp_to_C.bat` or `install_Xampp_to_D.bat`
3. If installed elsewhere, scripts only support C: or D: drive

### Problem: "Port 3500 already in use"

**Cause**: Another application is using port 3500

**Solution**:
1. Find what's using the port:
   ```batch
   netstat -ano | findstr :3500
   ```
2. Note the PID (last column)
3. Check which process:
   ```batch
   tasklist /FI "PID eq YOUR_PID"
   ```
4. Stop that process or choose different port

### Problem: "Apache won't start"

**Cause**: Configuration error or port conflict

**Solution**:
1. Check Apache error log:
   ```batch
   type C:\xampp\apache\logs\error.log
   ```
2. Common issues:
   - Port 80 or 443 already in use (IIS, Skype)
   - Syntax error in httpd.conf
   - Missing SSL certificates for HTTPS

3. Test configuration:
   ```batch
   C:\xampp\apache\bin\httpd.exe -t
   ```

### Problem: "MySQL won't start"

**Cause**: Port 3306 in use or data corruption

**Solution**:
1. Check MySQL error log:
   ```batch
   type C:\xampp\mysql\data\*.err
   ```
2. Check if port 3306 is available:
   ```batch
   netstat -ano | findstr :3306
   ```
3. Stop any conflicting MySQL instances

### Problem: "Database connection failed"

**Cause**: Incorrect credentials or MySQL not running

**Solution**:
1. Verify MySQL is running:
   ```batch
   tasklist /FI "IMAGENAME eq mysqld.exe"
   ```
2. Test database connection:
   ```batch
   C:\xampp\mysql\bin\mysql.exe -u wms_user -pwms_password barcode_wms -e "SELECT 1;"
   ```
3. If fails, recreate user:
   ```batch
   C:\xampp\mysql\bin\mysql.exe -u root -e "DROP USER IF EXISTS 'wms_user'@'localhost';"
   C:\xampp\mysql\bin\mysql.exe -u root -e "CREATE USER 'wms_user'@'localhost' IDENTIFIED BY 'wms_password';"
   C:\xampp\mysql\bin\mysql.exe -u root -e "GRANT ALL PRIVILEGES ON barcode_wms.* TO 'wms_user'@'localhost';"
   C:\xampp\mysql\bin\mysql.exe -u root -e "FLUSH PRIVILEGES;"
   ```

### Problem: "QR codes don't show correct IP"

**Cause**: Network IP not updated

**Solution**:
```batch
cd WebInterface
xampp_update_network_IP.bat
```
Refresh web interface (Ctrl+F5)

### Problem: "Android app can't connect"

**Cause**: Wrong IP, firewall, or network issue

**Solution**:
1. Verify both devices on same WiFi network
2. Check Windows Firewall:
   - Allow port 3500 for Apache
   - Allow port 3543 for HTTPS
3. Test from Android browser first:
   - Open `http://YOUR_IP:3500` in Chrome
   - Should show web interface
4. Use QR code to configure app endpoint
5. Check Android app logs for errors

---

## 🔄 Updating the System

### Updating Website Files

When pulling new code from Git:

```batch
# Pull latest changes
cd D:\Dev\LTGithub\AI_MultiBarcode_Capture
git pull origin main

# Stop XAMPP services (if running)
cd WebInterface
xampp_stop_server.bat

# Restart with automatic update
xampp_start_server.bat
```

The `xampp_start_server.bat` script automatically:
- Calls `xampp_update_server.bat` to update all files from src/
- Backs up existing htdocs
- Copies new website files
- Applies XAMPP-specific configurations
- Updates Apache and PHP configurations
- Preserves vendor directory (if using Composer)
- Restarts services with updated files

### Updating Files Without Restarting Services

If you want to update website files without restarting the entire XAMPP stack:

```batch
cd WebInterface
xampp_update_server.bat
```

**What This Script Does:**
- ✅ Detects XAMPP installation (C:\xampp or D:\xampp)
- ✅ Backs up existing htdocs folder
- ✅ Copies latest files from `src/` to XAMPP htdocs
- ✅ Applies XAMPP-specific path fixes
- ✅ Updates Apache configurations
- ✅ Updates SSL certificates
- ✅ Updates PHP database configuration
- ✅ Does NOT restart services - changes take effect immediately for PHP files

**When to Use:**
- Updating website files only (PHP, HTML, CSS, JavaScript)
- Quick updates without service interruption
- Testing new code changes

**Note:** Apache configuration changes still require an Apache restart to take effect. For those changes, use `xampp_start_server.bat` instead.

### Updating Database Schema

If database schema changes:

```batch
# Backup current database
C:\xampp\mysql\bin\mysqldump.exe -u root barcode_wms > backup.sql

# Import updated schema
C:\xampp\mysql\bin\mysql.exe -u root barcode_wms < database\init.sql
```

---

## 📚 Additional Resources

- **[Managing IP Changes](11-Managing-IP-Changes.md)** - Detailed guide for network changes
- **[Android App Configuration](07-Android-App-Configuration.md)** - Configure the mobile app
- **[API Documentation](06-API-Documentation.md)** - API endpoints and usage
- **[Troubleshooting Guide](15-Troubleshooting-Guide.md)** - Comprehensive troubleshooting
- **[Generating HTTPS Certificates](13-Generating-HTTPS-Certificates.md)** - SSL certificate setup
- **[Understanding Certificates](17-Understanding-Certificates-For-Beginners.md)** - Certificate basics

---

## 🎉 Success!

You now have a fully functional AI MultiBarcode Capture system running on XAMPP!

**Next Steps:**
1. Configure your Android app using the QR code
2. Scan some barcodes
3. View sessions in the web interface
4. Explore the API documentation
5. Customize for your needs

**Need Help?**
- Check the [Troubleshooting Guide](15-Troubleshooting-Guide.md)
- Review the [API Documentation](06-API-Documentation.md)
- Check the GitHub issues page

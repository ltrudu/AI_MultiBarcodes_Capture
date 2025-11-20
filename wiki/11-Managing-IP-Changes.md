# Managing IP Changes

When you connect to a new network (different WiFi, mobile hotspot, or office network), your computer's IP address changes. This means the endpoint URLs in the AI MultiBarcode Capture app need to be updated to maintain connectivity with the web management system.

## 🔧 Quick Solution

The system includes automatic IP update scripts for both Docker and XAMPP deployments.

### For XAMPP Users (Windows)

Run the following script in the `WebInterface` directory:

```batch
xampp_update_network_IP.bat
```

This script:
- ✅ Detects your current local network IP
- ✅ Attempts to detect your external/public IP
- ✅ Updates the configuration file used by the web interface
- ✅ Enables QR code generation with new endpoints
- ✅ **No need to restart XAMPP services**

### For Docker Users

#### Windows Users

Run the following script in the `WebInterface` directory:

```batch
update-network-ip.bat
```

#### Linux/macOS Users

Run the following script in the `WebInterface` directory:

```bash
./update-network-ip.sh
```

## 📋 What These Scripts Do

### XAMPP Script (`xampp_update_network_IP.bat`)

1. **Detect New IP**: Automatically detect your current local and external IP addresses
2. **Update Configuration**: Create/update `config/ip-config.json` in XAMPP htdocs
3. **Preserve Data**: All your barcode sessions and data remain intact
4. **Update Endpoints**: The web interface will immediately show the correct new endpoint URLs
5. **No Downtime**: No need to restart Apache or MySQL services

### Docker Scripts (`update-network-ip.bat` / `.sh`)

1. **Detect New IP**: Automatically detect your current network IP address
2. **Update Container**: Restart the Docker container with the new IP configuration
3. **Preserve Data**: All your barcode sessions and data remain intact (data persists in Docker volume)
4. **Update Endpoints**: The web interface will show the correct new endpoint URLs

## 🎯 When to Use

Use these scripts whenever you:

- Connect to a different WiFi network
- Switch from WiFi to mobile hotspot
- Move between office locations
- Experience connectivity issues with the Android app

## ⚡ Step-by-Step Process

### For XAMPP (Windows):

1. Open Command Prompt or PowerShell
2. Navigate to the WebInterface directory:
   ```
   cd path\to\AI_MultiBarcode_Capture\WebInterface
   ```
3. Run the XAMPP IP update script:
   ```
   xampp_update_network_IP.bat
   ```
4. Wait for the script to complete (typically 5-10 seconds)
5. **No restart needed** - changes take effect immediately!

**Expected Output:**
```
=========================================================
AI MultiBarcode Capture - Network IP Update
=========================================================

[INFO] XAMPP detected at C:\xampp
[OK] Local IP detected: 192.168.1.188
[INFO] Detecting external IP address...
[OK] External IP detected: 109.221.226.173
[INFO] Writing IP configuration to C:\xampp\htdocs\config\ip-config.json
[OK] IP configuration updated successfully

=========================================================
Network IP Configuration Updated
=========================================================

Local IP:    192.168.1.188
External IP: 109.221.226.173
Config File: C:\xampp\htdocs\config\ip-config.json

You can now configure your Android app with:
- HTTP Endpoint:  http://192.168.1.188:3500/api/barcodes.php
- HTTPS Endpoint: https://192.168.1.188:3543/api/barcodes.php
```

### For Docker (Windows):

1. Open Command Prompt or PowerShell
2. Navigate to the WebInterface directory:
   ```
   cd path\to\AI_MultiBarcode_Capture\WebInterface
   ```
3. Run the Docker update script:
   ```
   update-network-ip.bat
   ```
4. Wait for the script to complete (typically 30-60 seconds)

### For Docker (Linux/macOS):

1. Open Terminal
2. Navigate to the WebInterface directory:
   ```
   cd path/to/AI_MultiBarcode_Capture/WebInterface
   ```
3. Run the update script:
   ```
   ./update-network-ip.sh
   ```
4. Wait for the script to complete (typically 30-60 seconds)

## 📱 Updating Your Android App

After running the IP update script, you have two options to update your Android app:

### Option A: Using QR Codes (Easiest!)

1. **Open the web interface** at `http://localhost:3500`
2. **Click the Settings (⚙️) button** in the top-right corner
3. **Click "🔗 Endpoint Configuration"**
4. **Click the "📱 QR Code" button** next to the Local Network Endpoint
5. **Scan the QR code** from your Android app's Settings page
6. The endpoint is automatically updated with the new IP address!

### Option B: Manual Copy/Paste

1. **Open the web interface** at `http://localhost:3500`
2. **Click the Settings (⚙️) button** in the top-right corner
3. **Click "🔗 Endpoint Configuration"**
4. **Copy the new Local Network Endpoint** (it will show your new IP address)
5. **Open the AI MultiBarcode Capture app** on your Android device
6. **Go to Settings → Server Configuration**
7. **Paste the new endpoint URL**
8. **Test the connection** to verify it works

## 🔍 Verifying the Update

After running the script, you can verify it worked by:

1. **Check the script output** - it should show "Network IP updated to: [YOUR_NEW_IP]"
2. **Visit the web interface** at `http://localhost:3500`
3. **Check the endpoint configuration** - it should show your new IP address
4. **Test Android connectivity** by scanning a barcode

## 🛠️ Technical Details

### XAMPP Script Technical Details

The `xampp_update_network_IP.bat` script performs these operations:

1. **XAMPP Detection**: Automatically detects XAMPP at `C:\xampp` or `D:\xampp`
2. **Local IP Detection**: Uses `ipconfig` to detect current network IP (192.168.x.x or 10.x.x.x ranges)
3. **External IP Detection**: Uses PowerShell to query `ipinfo.io/ip` for public IP (with 5-second timeout)
4. **Configuration Update**: Creates/updates `config/ip-config.json` in XAMPP htdocs:
   ```json
   {
     "local_ip": "192.168.1.188",
     "external_ip": "109.221.226.173",
     "last_updated": "2025-11-20T16:34:13Z",
     "detection_method": "xampp_batch_update"
   }
   ```
5. **No Service Restart**: Changes take effect immediately without restarting Apache or MySQL

### Docker Script Technical Details

The Docker scripts perform these operations:

1. **IP Detection**: Use system commands (`ipconfig` on Windows, `hostname -I` on Linux) to detect the current network IP
2. **Container Restart**: Stop and remove the current Docker container
3. **Reconfiguration**: Start a new container with the updated `HOST_IP` environment variable
4. **Data Preservation**: Reuse the same MySQL data volume to preserve all sessions and barcodes

## ⚠️ Important Notes

### For XAMPP Users

- ✅ **No Data Loss**: Your barcode sessions and data are preserved during the update
- ✅ **Zero Downtime**: No need to restart Apache or MySQL - changes are instant
- ✅ **Automatic Detection**: Detects local IP (192.168.x.x, 10.x.x.x) and external IP automatically
- ✅ **Fallback Handling**: If external IP detection fails, shows "Unable to detect"
- ✅ **No User Interaction**: Script runs automatically without prompts
- ✅ **QR Code Support**: Web interface immediately shows updated QR codes with new IPs

### For Docker Users

- ✅ **No Data Loss**: Your barcode sessions and data are preserved (stored in Docker volume)
- ⚠️ **Brief Downtime**: Web interface unavailable for 30-60 seconds during container restart
- ✅ **Automatic Detection**: Detects common private IP ranges (192.168.x.x, 10.x.x.x, 172.x.x.x)
- ✅ **No User Interaction**: Scripts run completely automatically without prompts

## 🔧 Troubleshooting

### XAMPP: Script Won't Run
- **Permissions**: Ensure you have read/write access to the XAMPP installation folder
- **XAMPP Not Found**: Script checks for C:\xampp or D:\xampp - if installed elsewhere, manually edit the script
- **PowerShell Blocked**: If external IP detection fails, check PowerShell execution policy

### XAMPP: IP Configuration Not Working
- **Check config file exists**: Look for `C:\xampp\htdocs\config\ip-config.json` or `D:\xampp\htdocs\config\ip-config.json`
- **Verify file contents**: Open the JSON file and check if IPs are correct
- **Clear browser cache**: Press Ctrl+F5 to hard refresh the web interface
- **Check server-info.php**: Visit `http://localhost:3500/api/server-info.php` to see detected IPs

### XAMPP: External IP Shows "Unable to detect"
- **Normal behavior**: This happens if you're behind a firewall or don't have internet access
- **Not a problem**: Local network endpoint still works fine for same-network Android devices
- **Manual override**: You can manually edit the `ip-config.json` file if needed

### Docker: Script Won't Run
- **Windows**: Ensure you're running as Administrator if you get permission errors
- **Linux/macOS**: Make sure the script is executable with `chmod +x update-network-ip.sh`

### Docker: Container Won't Start
- **Check Docker**: Ensure Docker Desktop is running
- **Port Conflicts**: Make sure port 3500 isn't being used by another application
- **Check Logs**: Run `docker logs multibarcode-webinterface` to see error messages

### Common: IP Not Detected
- **XAMPP**: Script will fall back to `127.0.0.1` if it can't detect your IP
- **Docker**: Same fallback behavior
- **Manual check**: Use `ipconfig` (Windows) or `ip addr` (Linux) to verify your actual IP
- **Network connection**: Ensure you're connected to a network with a valid IP address

### Common: Android App Still Can't Connect
- **Double-check the endpoint URL** in the Android app settings
- **Verify both devices are on the same network**
- **Check firewall settings** that might block connections on port 3500/3543
- **Try the QR code**: Use the QR code feature to avoid typos
- **Test connectivity**: Try accessing `http://YOUR_IP:3500` from the Android device browser first

## 📚 Related Documentation

- **[XAMPP Installation Guide](04-Installation-Guide-XAMPP.md)** - Initial XAMPP setup and deployment
- **[Docker WMS Deployment](10-Docker-WMS-Deployment.md)** - Initial Docker setup and deployment
- **[Android App Configuration](07-Android-App-Configuration.md)** - Configuring the mobile app
- **[Troubleshooting Guide](15-Troubleshooting-Guide.md)** - General troubleshooting help

---

**💡 Tip**: Consider bookmarking this page or keeping the script location handy, as network changes are common when working with mobile devices in different locations.
# Managing IP Changes

When you connect to a new network (different WiFi, mobile hotspot, or office network), your computer's IP address changes. This means the endpoint URLs in the AI MultiBarcode Capture app need to be updated to maintain connectivity with the web management system.

## 🔧 Quick Solution

The system includes automatic IP update scripts that detect your new network IP and update the Docker container configuration.

### Windows Users

Run the following script in the `WebInterface` directory:

```batch
update-network-ip.bat
```

### Linux/macOS Users

Run the following script in the `WebInterface` directory:

```bash
./update-network-ip.sh
```

## 📋 What These Scripts Do

1. **Detect New IP**: Automatically detect your current network IP address
2. **Update Container**: Restart the Docker container with the new IP configuration
3. **Preserve Data**: All your barcode sessions and data remain intact
4. **Update Endpoints**: The web interface will show the correct new endpoint URLs

## 🎯 When to Use

Use these scripts whenever you:

- Connect to a different WiFi network
- Switch from WiFi to mobile hotspot
- Move between office locations
- Experience connectivity issues with the Android app

## ⚡ Step-by-Step Process

### For Windows:

1. Open Command Prompt or PowerShell
2. Navigate to the WebInterface directory:
   ```
   cd path\to\AI_MultiBarcode_Capture\WebInterface
   ```
3. Run the update script:
   ```
   update-network-ip.bat
   ```
4. Wait for the script to complete (typically 30-60 seconds)

### For Linux/macOS:

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

After running the IP update script:

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

The scripts perform these operations:

1. **IP Detection**: Use system commands (`ipconfig` on Windows, `hostname -I` on Linux) to detect the current network IP
2. **Container Restart**: Stop and remove the current Docker container
3. **Reconfiguration**: Start a new container with the updated `HOST_IP` environment variable
4. **Data Preservation**: Reuse the same MySQL data volume to preserve all sessions and barcodes

## ⚠️ Important Notes

- **No Data Loss**: Your barcode sessions and data are preserved during the update
- **Brief Downtime**: The web interface will be unavailable for 30-60 seconds during the container restart
- **Automatic Detection**: The scripts automatically detect common private IP ranges (192.168.x.x, 10.x.x.x, 172.x.x.x)
- **No User Interaction**: The scripts run completely automatically without prompting for input

## 🔧 Troubleshooting

### Script Won't Run
- **Windows**: Ensure you're running as Administrator if you get permission errors
- **Linux/macOS**: Make sure the script is executable with `chmod +x update-network-ip.sh`

### Container Won't Start
- **Check Docker**: Ensure Docker Desktop is running
- **Port Conflicts**: Make sure port 3500 isn't being used by another application
- **Check Logs**: Run `docker logs multibarcode-webinterface` to see error messages

### IP Not Detected
- The script will fall back to `127.0.0.1` if it can't detect your IP
- You can manually check your IP with `ipconfig` (Windows) or `ip addr` (Linux)
- Ensure you're connected to a network with a valid IP address

### Android App Still Can't Connect
- **Double-check the endpoint URL** in the Android app settings
- **Verify both devices are on the same network**
- **Check firewall settings** that might block connections
- **Try the internet endpoint** if local network endpoint doesn't work

## 📚 Related Documentation

- **[Docker WMS Deployment](10-Docker-WMS-Deployment.md)** - Initial setup and deployment
- **[Android App Configuration](07-Android-App-Configuration.md)** - Configuring the mobile app
- **[Troubleshooting Guide](15-Troubleshooting-Guide.md)** - General troubleshooting help

---

**💡 Tip**: Consider bookmarking this page or keeping the script location handy, as network changes are common when working with mobile devices in different locations.
# Android App Configuration - AI MultiBarcode Capture

This comprehensive guide covers all aspects of configuring the AI MultiBarcode Capture Android application for optimal performance in various deployment scenarios.

## 📱 Application Overview

The AI MultiBarcode Capture app provides dual-mode operation:
- **File-based Mode**: Stores barcode data locally for offline operation
- **HTTP(s) Post Mode**: Real-time data transmission to web management system

### 🚀 Quick Configuration Features
- **📱 QR Code Endpoint Setup**: Automatically configure HTTP endpoint by scanning a QR code from your WMS
- **⚙️ Manual Configuration**: Traditional manual endpoint and authentication setup
- **🔧 Advanced Settings**: Camera resolution, processing modes, and barcode symbologies

## ⚙️ Settings Configuration

### Access Settings
1. Launch the AI MultiBarcode Capture app
2. Tap the **Settings** (gear) icon in the top-right corner
3. Configure options based on your deployment requirements

### Processing Mode Configuration

#### File-based Processing
```
Settings → Processing Mode → File-based Processing
```

**Use Cases:**
- Offline environments without network connectivity
- High-security environments where network transmission is restricted
- Batch processing workflows
- Remote locations with intermittent connectivity

**Configuration:**
- No additional setup required
- Data stored in device internal storage
- Export functionality available via "Browser" button
- Files can be transferred manually via USB or shared wirelessly

#### HTTP(s) Post Processing
```
Settings → Processing Mode → HTTP(s) Post
```

**Required Configuration:**
- **HTTP(s) Endpoint**: Complete URL to your web service
- **Authentication**: Enable/disable based on server requirements
- **Network Security**: Configure SSL/TLS settings

## 🌐 Network Configuration

### HTTP Endpoint Setup

#### Development Environment
```
HTTP(s) Endpoint: http://192.168.1.100:3500/api/barcodes.php
Authentication: Disabled
```

#### Production Environment
```
HTTP(s) Endpoint: https://barcode-api.company.com/api/barcodes.php
Authentication: Enabled (if configured on server)
SSL Certificate Validation: Enabled
```

### QR Code Endpoint Configuration

The Android app supports automatic endpoint configuration via QR code scanning, providing a convenient way to connect the mobile app to your WMS (Web Management System).

#### How to Use QR Code Configuration

**Step 1: Generate QR Code from WMS**
1. Open your WMS web interface in a browser
2. Navigate to the **Endpoint** or **Configuration** section
3. Click on the **QR Code** button or icon
4. The system will display a QR code containing the endpoint URL

**Step 2: Scan QR Code with Android App**
1. Launch the AI MultiBarcode Capture app on your Android device
2. Tap the **Settings** (gear) icon to open app settings
3. Use the Zebra Imager to scan the QR Code
4. A toast message will confirm: "Endpoint updated from QR code"
5. The **HTTP(s) Endpoint** field will be automatically populated

#### QR Code Format
The QR code must contain data in the following format:
```
AIMultiBarcodeEndpoint:http://192.168.1.100:3500/api/barcodes.php
```
or
```
AIMultiBarcodeEndpoint:https://barcode-api.company.com/api/barcodes.php
```

#### Benefits of QR Code Configuration
- **Zero-typing**: Eliminates manual URL entry errors
- **Quick Setup**: Instant connection to WMS
- **Accuracy**: Prevents typos in complex URLs
- **User-friendly**: No technical knowledge required
- **Scalable**: Easy deployment across multiple devices

#### Security Considerations
- QR codes should only be displayed on trusted networks
- Consider using HTTPS endpoints for production environments
- Validate the endpoint URL before saving settings
- Monitor for unauthorized QR code scanning attempts

### Network Discovery Methods

#### Method 1: Find Computer IP Address

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

#### Method 2: Docker Host Discovery
```bash
# Find Docker network gateway
docker network inspect webinterface_default

# Use gateway IP as endpoint
http://172.18.0.1:3500/api/barcodes.php
```

#### Method 3: Android Emulator
For Android emulator development:
```
HTTP(s) Endpoint: http://10.0.2.2:3500/api/barcodes.php
```
Note: `10.0.2.2` is the special IP that Android emulator uses to access the host machine.

### Network Security Configuration

#### HTTP Cleartext Traffic (Development)
For development environments using HTTP (not HTTPS), the app includes network security configuration to allow cleartext traffic:

```xml
<!-- Automatically configured in network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">localhost</domain>
        <domain includeSubdomains="false">127.0.0.1</domain>
        <domain includeSubdomains="false">192.168.1.188</domain>
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

#### HTTPS Configuration (Production)
For production deployments:
- Use valid SSL certificates
- Enable certificate validation
- Configure proper domain names
- Test SSL connectivity before deployment

## 🔧 Advanced Configuration

### Camera Settings

#### Resolution Configuration
The app uses optimized camera settings:
- **Aspect Ratio**: 16:9 for optimal AI processing
- **Resolution**: 1920x1080 (Full HD)
- **Frame Rate**: 30 FPS for smooth operation


### Session Management

#### Session File Naming
```
Format: Session_YYYYMMDD_HHMMSS.txt
Example: Session_20240315_143022.txt
```

#### Session Data Structure
```json
{
  "session_start": "2024-03-15T14:30:22.123Z",
  "device_info": "Samsung_Galaxy_S24_Android14",
  "barcodes": [
    {
      "data": "1234567890123",
      "symbology": "EAN13",
      "timestamp": "2024-03-15T14:30:25.456Z",
      "position": {"x": 0.5, "y": 0.3}
    }
  ]
}
```

## 🔐 Security Configuration

### Permissions Management

#### Required Permissions
- **CAMERA**: Core barcode scanning functionality
- **MANAGE_EXTERNAL_STORAGE**: File operations and session storage
- **INTERNET**: Network communication for HTTP mode
- **EMDK Permission**: For Zebra device integration

#### Permission Request Flow
1. App startup checks for permissions
2. Displays permission request dialogs
3. Provides explanatory text for each permission
4. Graceful handling of denied permissions


## 🔧 Troubleshooting Common Issues

### Connection Issues

#### "Upload Failed" Toast Message
**Diagnosis:**
1. Check network connectivity
2. Verify endpoint URL format
3. Test server accessibility
4. Review server logs

**Solutions:**
```bash
# Test endpoint accessibility
curl -I http://192.168.1.100:3500/api/barcodes.php

# Check network from device
ping 192.168.1.100

# Verify Docker services
docker-compose ps
```

#### "Cleartext HTTP Traffic Not Permitted"
**Cause:** Android security policy blocking HTTP connections

**Solution:** Use HTTPS or configure network security (development only):
```xml
<!-- For development environments -->
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">192.168.1.100</domain>
</domain-config>
```

### Performance Issues

#### Slow Barcode Detection
**Possible Causes:**
- Insufficient device processing power
- Poor lighting conditions
- Camera focus issues
- High CPU usage from other apps

**Solutions:**
- Reduce detection parameters
- Improve lighting environment
- Close background applications
- Use higher-end device for demanding environments

#### Memory Issues
**Symptoms:**
- App crashes during scanning
- Out of memory errors
- Slow performance over time

**Solutions:**
- Restart app periodically
- Clear cache and data
- Reduce session size
- Enable automatic upload to reduce local storage

### QR Code Configuration Issues

#### QR Code Not Scanning
**Symptoms:**
- App doesn't respond to QR code
- No toast message appears
- Endpoint field remains unchanged

**Solutions:**
1. **Check QR Code Format**: Ensure the QR code contains data starting with `AIMultiBarcodeEndpoint:`
2. **Improve Lighting**: Ensure adequate lighting when scanning
3. **Camera Focus**: Allow camera to focus properly on the QR code
4. **Distance**: Hold device at appropriate distance (6-12 inches)
5. **Screen Brightness**: Increase WMS display brightness if scanning from screen

#### Incorrect Endpoint Populated
**Possible Causes:**
- QR code contains wrong URL
- WMS generating incorrect endpoint
- Network configuration changed

**Solutions:**
1. Verify the WMS endpoint configuration
2. Regenerate QR code from WMS
3. Manually verify endpoint URL
4. Test endpoint connectivity with curl/browser

#### QR Code Security Concerns
**Best Practices:**
- Only scan QR codes from trusted sources
- Verify the populated endpoint before saving
- Use HTTPS endpoints in production
- Monitor endpoint access logs for unauthorized attempts

### Network Configuration Issues

#### Cannot Connect to Server
**Checklist:**
- [ ] Server is running and accessible
- [ ] Port 3500 is open and not blocked by firewall
- [ ] Device is on the same network as server
- [ ] Endpoint URL is correctly formatted
- [ ] DNS resolution is working (if using domain names)


#### LogCat Monitoring
```bash
# Monitor app-specific logs
adb logcat | grep "ai_multibarcodes_capture"

# Monitor network-related logs
adb logcat | grep -E "(HTTP|Network|Connection)"

# Monitor performance logs
adb logcat | grep -E "(Performance|Memory|Camera)"
```

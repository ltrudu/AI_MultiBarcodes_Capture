# Android App Configuration - AI MultiBarcode Capture

This comprehensive guide covers all aspects of configuring the AI MultiBarcode Capture Android application for optimal performance in various deployment scenarios.

## 📱 Application Overview

The AI MultiBarcode Capture app provides dual-mode operation:
- **File-based Mode**: Stores barcode data locally for offline operation
- **HTTP(s) Post Mode**: Real-time data transmission to web management system

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

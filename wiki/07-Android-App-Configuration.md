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
HTTP(s) Endpoint: http://192.168.1.100:8080/api/barcodes.php
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
http://172.18.0.1:8080/api/barcodes.php
```

#### Method 3: Android Emulator
For Android emulator development:
```
HTTP(s) Endpoint: http://10.0.2.2:8080/api/barcodes.php
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

#### Orientation Handling
- **Auto-rotation**: Supported for portrait and landscape modes
- **Overlay Adaptation**: Barcode overlays automatically adjust to orientation
- **UI Rotation**: Interface elements rotate with device orientation

### AI Vision SDK Configuration

#### Detection Parameters
```kotlin
// Configured in BarcodeTracker.kt
val config = BarcodeTrackerConfig().apply {
    maxBarcodes = 10              // Simultaneous barcode detection
    confidenceThreshold = 0.7     // Minimum confidence score
    trackingEnabled = true        // Enable barcode tracking
    duplicateFilter = true        // Filter duplicate detections
}
```

#### Performance Tuning
- **Multi-threading**: 3 camera threads, 3 task execution threads
- **Memory Management**: Automatic cleanup of camera resources
- **Buffer Management**: Optimized image buffer handling

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

### Authentication Configuration

#### Endpoint Authentication
When server requires authentication:
```
Settings → HTTP(s) Post → Authentication → Enable
```

**Supported Authentication Methods:**
- API Key authentication (header-based)
- Basic HTTP authentication
- Custom token-based authentication

#### Implementation Example
```java
// Add authentication headers
if (authenticationEnabled) {
    urlConnection.setRequestProperty("Authorization", "Bearer " + apiToken);
    urlConnection.setRequestProperty("X-API-Key", apiKey);
}
```

## 🎯 Deployment Scenarios

### Scenario 1: Small Office (1-5 Devices)

**Network Setup:**
- Single WiFi network
- Desktop computer running Docker
- Direct IP configuration

**Configuration:**
```
Processing Mode: HTTP(s) Post
Endpoint: http://192.168.1.100:8080/api/barcodes.php
Authentication: Disabled
SSL: Not required for internal network
```

### Scenario 2: Warehouse Environment (10-50 Devices)

**Network Setup:**
- Enterprise WiFi with multiple access points
- Dedicated server with load balancer
- VLAN segmentation for security

**Configuration:**
```
Processing Mode: HTTP(s) Post
Endpoint: https://barcode-internal.company.com/api/barcodes.php
Authentication: Enabled
SSL: Required with internal CA certificate
```

### Scenario 3: Multi-Site Enterprise (50+ Devices)

**Network Setup:**
- Cloud-hosted infrastructure
- CDN for global distribution
- Multi-region deployment

**Configuration:**
```
Processing Mode: HTTP(s) Post
Endpoint: https://barcode-api.company.com/api/v1/barcodes
Authentication: OAuth 2.0 or API key
SSL: Required with public certificate
Rate Limiting: Configured server-side
```

### Scenario 4: Offline/Disconnected Operation

**Network Setup:**
- No network connectivity
- Periodic data synchronization

**Configuration:**
```
Processing Mode: File-based Processing
Export Format: JSON or Excel
Sync Method: Manual transfer via USB/WiFi Direct
Backup Strategy: Automatic local backup
```

## 📱 Device-Specific Configuration

### Zebra Devices

#### TC21/TC26 Series
```
Recommended Settings:
- Camera Resolution: Full HD (1920x1080)
- Processing Mode: HTTP(s) Post (for real-time operation)
- Session Management: Auto-upload enabled
- Power Management: Optimized for scanning workflows
```

#### MC33 Series
```
Optimized Settings:
- Rugged Environment Mode: Enabled
- Extended Battery Life: Configured
- Barcode Engine: Optimized for warehouse scanning
- Network: WiFi with enterprise security
```

#### L10 Rugged Tablet
```
Tablet-Specific Settings:
- Large Screen Mode: Optimized UI scaling
- Multi-Window Support: Enabled for productivity
- Docking Station Integration: Configured
- External Scanner Support: Enabled
```

### Generic Android Devices

#### High-End Smartphones (Samsung Galaxy, Google Pixel)
```
Performance Settings:
- Camera2 API: Enabled for advanced features
- Hardware Acceleration: Enabled
- Background Processing: Optimized
- Memory Management: Aggressive cleanup
```

#### Budget Android Devices
```
Compatibility Settings:
- Reduced Resolution: 1280x720 for performance
- Frame Rate: 24 FPS to reduce processing load
- Memory Optimization: Enabled
- Background Limits: Strict power management
```

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
curl -I http://192.168.1.100:8080/api/barcodes.php

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
- [ ] Port 8080 is open and not blocked by firewall
- [ ] Device is on the same network as server
- [ ] Endpoint URL is correctly formatted
- [ ] DNS resolution is working (if using domain names)

#### SSL/TLS Certificate Issues
**For Production Deployments:**
- Ensure certificates are valid and not expired
- Verify certificate chain is complete
- Check that certificate matches domain name
- Test with multiple clients to isolate device-specific issues

## 📊 Performance Monitoring

### Built-in Diagnostics

#### Connection Testing
The app includes built-in connectivity testing:
1. Go to Settings → HTTP(s) Post
2. Tap "Test Connection" button
3. Review connection status and response time

#### Performance Metrics
- Average detection time per barcode
- Upload success rate
- Session completion time
- Memory usage statistics

### Logging Configuration

#### Enable Debug Logging
```java
// Add to application configuration
public static final boolean DEBUG_MODE = BuildConfig.DEBUG;

if (DEBUG_MODE) {
    Log.d(TAG, "Debug information: " + details);
}
```

#### LogCat Monitoring
```bash
# Monitor app-specific logs
adb logcat | grep "ai_multibarcodes_capture"

# Monitor network-related logs
adb logcat | grep -E "(HTTP|Network|Connection)"

# Monitor performance logs
adb logcat | grep -E "(Performance|Memory|Camera)"
```

## 🔄 Configuration Management

### Bulk Configuration Deployment

#### Enterprise Configuration File
```json
{
  "deployment_config": {
    "processing_mode": "http_post",
    "endpoint_url": "https://barcode-api.company.com/api/barcodes.php",
    "authentication": {
      "enabled": true,
      "type": "api_key",
      "api_key": "your-api-key-here"
    },
    "network": {
      "ssl_validation": true,
      "timeout_seconds": 30,
      "retry_attempts": 3
    },
    "camera": {
      "resolution": "1920x1080",
      "frame_rate": 30
    }
  }
}
```

#### Configuration Deployment Methods
1. **MDM (Mobile Device Management)**: Push configuration via enterprise MDM solution
2. **QR Code Configuration**: Encode settings in QR code for quick setup
3. **NFC Configuration**: Use NFC tags for rapid device configuration
4. **Manual Configuration**: Step-by-step setup for small deployments

---

**Next Steps**: After configuring the Android app, proceed to [HTTP Integration](09-HTTP-Integration.md) for detailed communication setup or [Build & Deployment](08-Android-Build-Deployment.md) for creating and distributing the app.
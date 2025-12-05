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

### Capture Trigger Mode Configuration

The Capture Trigger Mode setting allows you to customize when barcode capture occurs during the scan button press cycle.

#### Available Options

**On Scan Press (Default)**
```
Settings → Capture Trigger Mode → On Scan Press
```

**Behavior:** Captures barcodes immediately when the scan button is pressed down.

**Use Cases:**
- Rapid scanning workflows requiring immediate feedback
- High-volume scanning environments
- Applications where speed is prioritized over precision
- Quick batch processing scenarios

**On Scan Release**
```
Settings → Capture Trigger Mode → On Scan Release
```

**Behavior:** Captures barcodes when the scan button is released (on key up).

**Use Cases:**
- Scenarios requiring precise aiming before capture confirmation
- Applications where users need time to steady their aim
- Workflows where deliberate capture timing is important
- Quality control processes requiring careful positioning

#### Configuration Notes

- **Default Setting**: On Scan Press is the default mode for maximum responsiveness
- **Persistent Setting**: Your selection is saved and automatically restored when the app resumes
- **Managed Configuration**: Enterprise administrators can remotely configure this setting via EMM/MDM systems
- **Real-Time Updates**: Configuration changes through managed configuration are applied immediately without app restart
- **Hardware Button Support**: Works with both physical scan buttons (R1 button and dedicated scan button)

#### Enterprise Deployment

For enterprise managed configuration:
```xml
<!-- In app_restrictions.xml -->
<restriction
    android:key="capture_trigger_mode"
    android:title="Capture Trigger Mode"
    android:restrictionType="choice"
    android:defaultValue="press"
    android:entries="@array/capture_trigger_mode_names"
    android:entryValues="@array/capture_trigger_mode_values" />
```

Available values:
- `press` - Capture on scan button press
- `release` - Capture on scan button release

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
- **Prefix**: Configure the filename prefix for exported files (default: "MySession_")
- **File Type**: Choose export format from available options:
  - **Text File (.txt)**: Plain text format for simple data export
  - **CSV File (.csv)**: Comma-separated values for spreadsheet compatibility
  - **Excel File (.xlsx)**: Microsoft Excel format for advanced data analysis
- Data stored in device internal storage
- Export functionality available via "Browser" button
- Files can be transferred manually via USB or shared wirelessly

**File Naming Convention:**
```
Format: [Prefix]_YYYYMMDD_HHMMSS.[extension]
Examples:
- MySession_20240315_143022.txt
- Inventory_20240315_143022.csv
- Warehouse_20240315_143022.xlsx
```

#### HTTP(s) Post Processing
```
Settings → Processing Mode → HTTP(s) Post
```

**Required Configuration:**
- **HTTP(s) Endpoint**: Complete URL to your web service
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
SSL Certificate Validation: Enabled
```

### QR Code Endpoint Configuration

The Android app supports automatic endpoint configuration via QR code scanning, providing a convenient way to connect the mobile app to your WMS (Web Management System).

#### How to Use QR Code Configuration

**Step 1: Generate QR Code from WMS**
1. Open the WMS simulator web interface in a browser
2. Open the Settings (gear icon on the top right of the screen)
2. Navigate to the **Endpoint Configuration**
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
    <!-- Allow cleartext traffic globally for development/testing -->
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```

#### HTTPS Configuration (Production)
For production deployments:
- Use valid SSL certificates
- Enable certificate validation
- Configure proper domain names
- Test SSL connectivity before deployment

## 🔍 Regular Expression Filtering

The AI MultiBarcode Capture app includes a powerful filtering feature that allows you to capture only barcodes that match specific patterns using regular expressions. This feature helps focus on relevant data and eliminates unwanted barcode captures.

### How It Works

When **Regular Expression Filtering** is enabled, the application will:
- Scan and detect all barcodes as usual
- Apply the regex pattern to each barcode's data
- **Only capture barcodes that match the pattern**
- Ignore barcodes that don't match the filter

### Configuration Steps

1. **Access Settings**
   ```
   Settings → Filtering → Enable Filtering (checkbox)
   ```

2. **Enable Filtering**
   - Check the "Enable Filtering" checkbox
   - This activates the filtering system

3. **Set Regular Expression Pattern**
   - Enter your regex pattern in the "Regular Expression" text field
   - The text field is only enabled when filtering is activated

### Common Regular Expression Examples

**📚 [Complete Regex Pattern Collection](16-Common-Regex-Expressions.md)** - Comprehensive guide with hundreds of regex patterns for web URLs, device identifiers, government IDs, product codes, and industry standards.

#### Numeric Barcodes Only
```regex
^[0-9]+$
```
**Use Case**: Only capture barcodes containing numbers (UPC, EAN, etc.)

#### Product Codes with Specific Format
```regex
^PRD[0-9]{6}$
```
**Use Case**: Capture product codes starting with "PRD" followed by 6 digits

#### URL/Web Addresses
```regex
^https://[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(/.*)?$
```
**Use Case**: Only capture HTTPS URLs from QR codes

#### Serial Numbers
```regex
^SN[A-Z0-9]{8}$
```
**Use Case**: Capture serial numbers starting with "SN" followed by 8 alphanumeric characters

#### Batch/Lot Numbers
```regex
^LOT[0-9]{4}-[0-9]{2}$
```
**Use Case**: Capture lot numbers in format "LOT1234-56"

#### Alphanumeric Codes
```regex
^[A-Z]{2}[0-9]{4}[A-Z]{2}$
```
**Use Case**: Capture codes like "AB1234CD"

### Configuration Notes

- **Case Sensitivity**: Regular expressions are case-sensitive by default
- **Error Handling**: Invalid regex patterns are logged but won't crash the app
- **Performance**: Complex regex patterns may slightly impact scanning speed
- **Fallback Behavior**: If regex is empty, all barcodes are allowed through
- **Real-time Filtering**: Filtering is applied during live scanning

### Best Practices

1. **Test Patterns**: Verify your regex with sample data before deployment
2. **Keep It Simple**: Use the simplest pattern that meets your needs
3. **Document Patterns**: Record the purpose of complex regex patterns
4. **Performance Considerations**: Avoid overly complex patterns for high-volume scanning
5. **Backup Strategy**: Always have a way to disable filtering if needed

### Troubleshooting

#### Pattern Not Working
- Verify regex syntax using online regex testers
- Check for special characters that need escaping
- Ensure pattern matches the exact barcode format

#### Performance Issues
- Simplify complex regex patterns
- Consider using character classes instead of long alternations
- Test with actual scanning volume

#### No Barcodes Captured
- Check if filtering is enabled when not intended
- Verify pattern matches expected barcode format
- Temporarily disable filtering to test barcode detection

## 📊 Symbologies Configuration

The AI MultiBarcode Capture app supports a comprehensive range of barcode symbologies that can be individually enabled or disabled based on your specific requirements. The application supports **46 different barcode symbologies** organized into several categories.

### Symbology Categories

#### EAN/UPC Codes (Consumer Products)
These are commonly used for retail and consumer products.

#### 2D Matrix Codes (High Density)
Two-dimensional codes that can store large amounts of data in a compact space.

#### Linear 1D Codes (Traditional Barcodes)
Traditional single-dimension barcodes commonly used in various industries.

#### Postal Codes (Mail Services)
Specialized codes used by postal services worldwide.

#### GS1 Standards (Supply Chain)
Industry-standard codes for global supply chain management.

#### Other Specialized Codes
Additional symbologies for specific industry applications.

### Complete Symbology Reference

| Symbology | Default Setting | Category |
|-----------|----------------|----------|
| EAN 8 | ✅ Enabled | EAN/UPC |
| EAN 13 | ✅ Enabled | EAN/UPC |
| UPC A | ✅ Enabled | EAN/UPC |
| UPC E | ✅ Enabled | EAN/UPC |
| AZTEC | ✅ Enabled | 2D Matrix |
| CODABAR | ✅ Enabled | Linear 1D |
| CODE128 | ✅ Enabled | Linear 1D |
| CODE39 | ✅ Enabled | Linear 1D |
| I2OF5 | ❌ Disabled | Linear 1D |
| GS1 DATABAR | ✅ Enabled | GS1 Standards |
| DATAMATRIX | ✅ Enabled | 2D Matrix |
| GS1 DATABAR EXPANDED | ✅ Enabled | GS1 Standards |
| MAILMARK | ✅ Enabled | Postal |
| MAXICODE | ✅ Enabled | 2D Matrix |
| PDF417 | ✅ Enabled | 2D Matrix |
| QRCODE | ✅ Enabled | 2D Matrix |
| DOTCODE | ❌ Disabled | 2D Matrix |
| GRID MATRIX | ❌ Disabled | 2D Matrix |
| GS1 DATAMATRIX | ❌ Disabled | GS1 Standards |
| GS1 QRCODE | ❌ Disabled | GS1 Standards |
| MICROQR | ❌ Disabled | 2D Matrix |
| MICROPDF | ❌ Disabled | 2D Matrix |
| USPOSTNET | ❌ Disabled | Postal |
| USPLANET | ❌ Disabled | Postal |
| UK POSTAL | ❌ Disabled | Postal |
| JAPANESE POSTAL | ❌ Disabled | Postal |
| AUSTRALIAN POSTAL | ❌ Disabled | Postal |
| CANADIAN POSTAL | ❌ Disabled | Postal |
| DUTCH POSTAL | ❌ Disabled | Postal |
| US4STATE | ❌ Disabled | Postal |
| US4STATE FICS | ❌ Disabled | Postal |
| MSI | ❌ Disabled | Linear 1D |
| CODE93 | ❌ Disabled | Linear 1D |
| TRIOPTIC39 | ❌ Disabled | Linear 1D |
| D2OF5 | ❌ Disabled | Linear 1D |
| CHINESE 2OF5 | ❌ Disabled | Linear 1D |
| KOREAN 3OF5 | ❌ Disabled | Linear 1D |
| CODE11 | ❌ Disabled | Linear 1D |
| TLC39 | ❌ Disabled | Linear 1D |
| HANXIN | ❌ Disabled | 2D Matrix |
| MATRIX 2OF5 | ❌ Disabled | Linear 1D |
| UPCE1 | ❌ Disabled | EAN/UPC |
| GS1 DATABAR LIM | ❌ Disabled | GS1 Standards |
| FINNISH POSTAL 4S | ❌ Disabled | Postal |
| COMPOSITE AB | ❌ Disabled | Composite |
| COMPOSITE C | ❌ Disabled | Composite |

### Configuration Notes

- **Default Enabled (16 symbologies)**: The most commonly used barcode types across retail, logistics, and general applications
- **Default Disabled (30 symbologies)**: Specialized codes for specific industries or less common applications
- **Performance Impact**: Enabling fewer symbologies can improve detection speed and accuracy
- **Regional Considerations**: Some postal codes are specific to certain countries/regions

### Best Practices

1. **Enable only needed symbologies** for optimal performance
2. **Retail environments**: Keep EAN/UPC codes enabled
3. **Logistics/Supply Chain**: Enable GS1 standards and common 2D codes
4. **Postal applications**: Enable relevant postal codes for your region
5. **Manufacturing**: Consider enabling Code 128, Code 39, and Data Matrix

## 🔧 Advanced Configuration

### Model Input Size

**Description:** Model input size is the resolution your image is resized to before AI analysis. Smaller sizes are faster and use less memory, while larger sizes can help detect smaller or more distant barcodes—but also uses more processing power and memory. Choose the input size to balance speed and accuracy for your needs.

**Available Options:**
- **Small (640x640)**: Fastest processing, lowest memory usage, suitable for large or nearby barcodes
- **Medium (1280x1280)**: Balanced performance and accuracy for general use cases
- **Large (1600x1600)**: Best accuracy for small or distant barcodes, higher processing power required

**Note:** Model Input Size can be customized in increments of 32 using the SDK. The options above represent standard sizes.

### Camera Resolution

**Description:** Camera resolution is the number of pixels in your photo (e.g., 1MP = 1280x720). Higher resolution captures more detail for small or distant text but uses more power and memory. Benefits are limited if the model input size is low.

**Available Options:**
- **1MP (1280 x 720)**: Large or close-up barcodes - Lower power consumption
- **2MP (1920 x 1080)**: General barcodes - Recommended for most use cases
- **4MP (2688 x 1512)**: Dense, faint, or small barcodes - Higher detail capture
- **8MP (3840 x 2160)**: Tiny, distant, or low-contrast barcodes - Maximum detail

### Inference (Processor) Type

**Description:** This setting chooses which chip in your device runs AI tasks, affecting speed and battery life. Not all devices have a DSP.

**Available Options:**
- **DSP (Digital Signal Processor)**: Best Choice - Optimal performance and power efficiency
- **GPU (Graphics Processing Unit)**: For trial use if DSP not available - Good performance
- **CPU (Central Processing Unit)**: For trial use if DSP and GPU are not available - Fallback option

### Performance Recommendations

#### Optimal Settings for Different Scenarios

**High Performance (Close-range, good lighting):**
- Model Input Size: Small (640x640)
- Camera Resolution: 1MP or 2MP
- Inference Type: DSP

**Balanced Performance (General use):**
- Model Input Size: Medium (1280x1280)
- Camera Resolution: 2MP
- Inference Type: DSP

**Maximum Accuracy (Small/distant barcodes):**
- Model Input Size: Large (1600x1600)
- Camera Resolution: 4MP or 8MP
- Inference Type: DSP

**Battery Conservation:**
- Model Input Size: Small (640x640)
- Camera Resolution: 1MP
- Inference Type: DSP (if available)

## ⚡ Optimizations

The Optimizations section provides settings for performance monitoring and debugging control.

### Display Analysis Per Second

**Description:** Shows a real-time overlay on the camera preview displaying performance metrics for barcode analysis.

**Location:**
```
Settings → Advanced → Optimizations → Display Analysis Per Second
```

**Display Format:**
```
12 APS
45 ms
```

Where:
- **APS (Analysis Per Second)**: Number of complete barcode analyses performed per second
- **ms (Milliseconds)**: Average processing time per analysis (rolling average over 20 samples)

**Use Cases:**
- Performance tuning and optimization
- Comparing different device capabilities
- Troubleshooting slow scanning performance
- Validating configuration changes impact

**Configuration Notes:**
- **Default Setting**: Disabled (off)
- **Overlay Position**: Top-center of camera preview
- **Visual Style**: Semi-transparent background with white outline
- **Rolling Average**: Processing time calculated as average over last 20 analyses for stability

**Managed Configuration:**
```xml
<!-- In advanced_settings bundle -->
<restriction
    android:key="display_analysis_per_second"
    android:title="Display Analysis Per Second"
    android:restrictionType="bool"
    android:defaultValue="false" />
```

### Logging Enabled

**Description:** Controls application logging output. When disabled, only critical error reporting (feedback reporting) continues to function.

**Location:**
```
Settings → Advanced → Optimizations → Logging Enabled
```

**Behavior:**
- **Enabled**: All log levels (verbose, debug, info, warning, error) output to logcat
- **Disabled**: No logging output except for feedback/error reporting to EMM systems

**Use Cases:**
- **Production Deployments**: Disable logging to reduce overhead and prevent sensitive data exposure
- **Development/Debugging**: Enable logging for troubleshooting
- **Performance Optimization**: Disable logging to minimize processing overhead
- **Security Compliance**: Disable logging to prevent potential data leakage in logs

**Configuration Notes:**
- **Default Setting**: Disabled (off) for optimal production performance
- **Feedback Reporting**: Error reporting to EMM/MDM systems continues regardless of this setting
- **Immediate Effect**: Changes apply immediately without app restart
- **Persistent Setting**: Selection saved and restored across app sessions

**Managed Configuration:**
```xml
<!-- In advanced_settings bundle -->
<restriction
    android:key="logging_enabled"
    android:title="Logging Enabled"
    android:restrictionType="bool"
    android:defaultValue="false" />
```

**LogCat Filtering (when enabled):**
```bash
# Monitor all app logs
adb logcat | grep "AIMBCCapture"

# Monitor specific log levels
adb logcat *:V | grep "AIMBCCapture"  # Verbose and above
adb logcat *:D | grep "AIMBCCapture"  # Debug and above
adb logcat *:I | grep "AIMBCCapture"  # Info and above
```

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

# Managed Configuration Implementation

This document describes the managed configuration (app restrictions) implementation for AI MultiBarcode Capture application. The managed configuration allows EMM/MDM administrators to remotely configure the app settings.

## Overview

The app supports managed configuration through Android's Application Restrictions framework. When deployed in an enterprise environment, administrators can configure app settings centrally through their EMM/MDM solution.

## Configuration Structure

The managed configuration is organized into the following sections:

### 1. Application Settings
- **Prefix** (`prefix`): Default prefix for exported files (string)
- **Extension** (`extension`): Default file type for exports (choice: .txt, .csv, .xlsx)
- **Language** (`language`): Default language for the application interface (dropdown with all supported languages)

### 2. Barcode Symbologies (Nested Bundle)
All barcode symbologies are organized in a nested bundle called `barcode_symbologies`. This allows administrators to see them as a collapsed group when editing the configuration, improving the management experience.

#### Supported Symbologies:
- **Postal Barcodes**: Australian Postal, Canadian Postal, Dutch Postal, Finnish Postal 4S, Japanese Postal, UK Postal, US Planet, US Postnet, US 4State, US 4State FICS, Mailmark
- **2D Matrix Codes**: Aztec, Data Matrix, QR Code, Micro QR, MaxiCode, PDF417, Micro PDF, DotCode, Grid Matrix, Han Xin
- **Linear 1D Barcodes**: Code 39, Code 128, Code 93, Code 11, Codabar, MSI
- **EAN/UPC Codes**: EAN-8, EAN-13, UPC-A, UPC-E, UPC-E1
- **GS1 DataBar**: GS1 DataBar, GS1 DataBar Expanded, GS1 DataBar Limited, GS1 DataMatrix, GS1 QR Code
- **2 of 5 Variants**: Chinese 2 of 5, Discrete 2 of 5, Interleaved 2 of 5, Matrix 2 of 5, Korean 3 of 5
- **Composite Codes**: Composite A/B, Composite C
- **Other Codes**: TLC39, Trioptic 39

## Implementation Details

### Files Created/Modified:

1. **`app_restrictions.xml`** - Defines the managed configuration schema
2. **`ManagedConfigurationReceiver.java`** - Handles configuration changes and updates SharedPreferences
3. **`AndroidManifest.xml`** - Registers the BroadcastReceiver and configuration metadata
4. **`MainApplication.java`** - Applies managed configuration on app startup

### How It Works:

1. **Configuration Reception**: The `ManagedConfigurationReceiver` listens for `ACTION_APPLICATION_RESTRICTIONS_CHANGED` broadcasts
2. **Automatic Processing**: When a new managed configuration is received, it automatically updates the app's SharedPreferences
3. **Startup Application**: On app startup, `MainApplication` applies any existing managed configuration
4. **Settings Integration**: The existing `SettingsActivity` continues to work normally, reading from the same SharedPreferences

### Key Features:

- **Nested Bundle Structure**: All barcode symbologies are grouped in a collapsible bundle for better UX
- **Automatic Sync**: Configuration changes are immediately applied to SharedPreferences
- **Logging**: Comprehensive logging for debugging and monitoring
- **Error Handling**: Robust error handling with fallback to default values
- **Startup Sync**: Ensures configuration is applied even if the app wasn't running when changes were made

## EMM/MDM Configuration

### For Administrators:

1. **Deploy the APK** to your managed devices
2. **Configure App Restrictions** through your EMM/MDM console:
   - Set the file prefix (e.g., "Company_Scan_")
   - Choose default export format (.txt, .csv, or .xlsx)
   - Select default application language from the dropdown (supports 67 languages plus system default)
   - Expand the "Barcode Symbologies" section to configure which barcode types are enabled
3. **Push Configuration** to devices
4. **Verify Application**: The app will automatically apply the new settings

### Configuration Example:

```json
{
  "prefix": "CompanyScans_",
  "extension": ".xlsx",
  "language": "fr",
  "barcode_symbologies": {
    "CODE39": true,
    "CODE128": true,
    "QRCODE": true,
    "DATAMATRIX": true,
    "EAN_13": true,
    "UPC_A": true,
    "PDF417": false,
    "AZTEC": false
  }
}
```

## Testing

### Manual Testing:
1. Use ADB to set restrictions: `adb shell am start-activity -e restrictionsPackage com.zebra.ai_multibarcodes_capture`
2. Use a test EMM solution
3. Deploy through Android Enterprise

### Verification:
- Check app logs for "ManagedConfigReceiver" entries
- Verify SharedPreferences are updated
- Confirm SettingsActivity reflects the managed configuration

## Technical Notes

- **Backwards Compatibility**: Apps without managed configuration continue to work with default settings
- **User Override**: Managed configuration takes precedence over user settings
- **Performance**: Configuration updates are applied asynchronously to avoid blocking the UI
- **Security**: No sensitive data is exposed through the configuration interface

## Troubleshooting

### Common Issues:

1. **Configuration Not Applied**:
   - Check EMM/MDM logs for deployment status
   - Verify app is in work profile (if using work profiles)
   - Check device logs: `adb logcat | grep ManagedConfig`

2. **Settings Not Updating**:
   - Verify the restriction keys match exactly
   - Check bundle structure for barcode symbologies
   - Ensure the app has been restarted after configuration changes

3. **Default Values**:
   - If no managed configuration is present, the app uses built-in defaults
   - Default values match the original app behavior

## Support

For technical support with managed configuration deployment, please refer to:
- Your EMM/MDM vendor documentation
- Android Enterprise documentation
- App-specific logs (filter: "ManagedConfigReceiver")
# AI MultiBarcode Capture Application

[![License](https://img.shields.io/badge/License-Zebra%20Development%20Tool-blue)](https://github.com/ZebraDevs/AISuite_Android_Samples/blob/main/Zebra%20Development%20Tool%20License.pdf) [![Platform](https://img.shields.io/badge/Platform-Android-green)](https://developer.android.com/) [![Language](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com/) [![Version](https://img.shields.io/badge/Version-1.36-brightgreen)](CHANGELOG.md) [![API](https://img.shields.io/badge/API-35%2B-yellow)](https://developer.android.com/about/versions/15) [![SDK](https://img.shields.io/badge/Zebra%20AI%20Vision%20SDK-3.1.4-blue)](https://developer.zebra.com/)

[![Apache](https://img.shields.io/badge/Apache-2.4-red)](https://httpd.apache.org/) [![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/) [![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/) [![XAMPP](https://img.shields.io/badge/XAMPP-8.2-orange?logo=xampp&logoColor=white)](https://www.apachefriends.org/) [![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML) [![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript) [![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)

A comprehensive Android enterprise application demonstrating Zebra AI Vision SDK capabilities for simultaneous multi-barcode detection, tracking, and session-based data management with enterprise deployment features.

**📚 [Complete Setup & Deployment Documentation](wiki/README.md)** - Comprehensive guides for quick start, Docker deployment, API integration, and enterprise configuration.

**Please note that this is a Work In Progress.**

Report any issues using the Issues manager of the original repository:
https://github.com/ltrudu/AI_MutliBarcodes_Capture

## 📅 What's New

### Version 1.36 - ⚡ **Performance & Android 15+ Compatibility**

**High-performance native image processing with NDK/JNI and Android 15+ 16KB page size support.**

#### ⚡ **Native NDK Performance Optimization:**

• **JNI-Based Image Cropping**: Native C++ implementation for capture zone image processing
  - **High-Performance YUV to RGB Conversion**: Optimized native code using integer math with fixed-point arithmetic
  - **Direct Bitmap Writing**: Native code writes directly to Android Bitmap using `AndroidBitmap_lockPixels()` for zero-copy operation
  - **Automatic Fallback**: Java implementation fallback when native library is unavailable
  - **BT.601 Color Space**: Proper YUV to RGB conversion using standard broadcast coefficients

• **Performance Benefits**:
  - Significantly faster capture zone cropping compared to Java implementation
  - Reduced CPU usage during barcode scanning with capture zone enabled
  - Lower memory allocation overhead through direct buffer processing
  - Optimized for real-time barcode detection workflows

#### 📱 **Android 15+ 16KB Page Size Support:**

• **Future-Proof Compatibility**: Native library built with 16KB page size alignment
  - **`-Wl,-z,max-page-size=16384`**: Linker flag ensures compatibility with Android 15+ devices using 16KB memory pages
  - **Seamless Operation**: Works on both traditional 4KB and new 16KB page size devices
  - **No Runtime Configuration Required**: Compatibility is built into the native library

#### 💡 **Benefits:**

• **Faster Capture Zone Processing**: Native code provides significant speedup for capture zone image cropping
• **Android 15+ Ready**: Application is prepared for upcoming Android devices with 16KB page sizes
• **Battery Efficiency**: Reduced CPU usage extends battery life during extended scanning sessions


---

### Version 1.35 - 🎨 **Theme System & Custom Typography**

**Comprehensive visual customization with dual theme support and Zebra brand fonts for both Android app and web interface.**

#### 🎨 **Android App Theme System:**

• **Dual Theme Support**: Choose between Legacy (classic blue) and Modern (Zebra green/dark) visual themes
  - **Legacy Theme**: Traditional blue color scheme with light backgrounds
  - **Modern Theme**: Contemporary dark design featuring Zebra's latest brand colors
  - **Settings Integration**: Easy theme selection in app Settings with instant preview
  - **Persistent Preferences**: Theme choice saved and automatically restored on app launch

• **Custom Zebra Typography**: Professional brand fonts integrated across the application
  - **Smart Font Application**: Fonts automatically applied when using Modern theme
  - **Intelligent Script Detection**: Custom fonts applied only for Latin-based languages (English, German, French, Spanish, Italian, Portuguese, Dutch, and 30+ more)
  - **Native Font Preservation**: Non-Latin languages (Arabic, Russian, Chinese, Japanese, Korean, Hebrew, Greek, etc.) use optimized native system fonts for best readability

• **Enhanced Android 15 Support**:
  - Full compatibility with Android 15 (API 35) edge-to-edge display
  - Improved system bar rendering with proper window insets handling
  - Optimized status bar and navigation bar color management across all Android versions (11-15)

#### 🌐 **Web Interface Theme Consistency:**

• **Modern Theme Availability**: The Modern theme introduced in v1.34 for the web interface is now complemented by matching Android app themes
  - **Unified Design Language**: Consistent visual experience across both Android app and web management interface
  - **Brand Alignment**: Both platforms now feature Zebra's modern brand identity with the Modern theme
  - **Flexible User Choice**: Users can independently select preferred themes for app and web interface

#### 💡 **Benefits:**

• **Professional Branding**: Zebra brand identity consistently applied across all platforms
• **Enhanced Readability**: Theme-appropriate typography optimized for each language script
• **User Customization**: Choose themes that match personal preferences or corporate standards
• **Global Accessibility**: Smart font selection ensures optimal readability for international users
• **Modern User Experience**: Contemporary design language aligned with Material Design 3 principles
• **Cross-Platform Consistency**: Unified theme system across Android app and web management interface

---

#### 📚 **Wiki Updates:**

• **📚 [Complete Regex Pattern Library](wiki/16-Common-Regex-Expressions.md)**: Comprehensive collection of 300+ regex patterns covering web URLs, device identifiers, government IDs, license plates, postal codes, phone numbers, and industry standards

• **🔐 [Understanding Certificates for Beginners](wiki/17-Understanding-Certificates-For-Beginners.md)**: Complete beginner's guide explaining certificate creation, platform-specific requirements, and how to create self-signed certificates that Windows and Android recognize as legitimate Certificate Authority certificates

• **🖥️ [XAMPP Installation Guide](wiki/04-Installation-Guide-XAMPP.md)**: NEW comprehensive guide for Docker-free deployment
  - **Method 0: XAMPP Quick Install** - Fastest setup via pre-configured 7z archive
  - Automated installation scripts
  - Manual step-by-step configuration
  - Supports C:\xampp or D:\xampp with auto-detection
  - Detailed troubleshooting section


## 📋 [View Complete Changelog](CHANGELOG.md) for previous versions and detailed release history.

## 📖 Quick Start Links

| Guide | Description |
|-------|-------------|
| **[15-Minute Quick Start](wiki/01-Quick-Start-Guide.md)** | Get the system running in 15 minutes |
| **[Docker Installation](wiki/03-Installation-Guide-Docker.md)** | Docker-based deployment (recommended) |
| **[XAMPP Installation](wiki/04-Installation-Guide-XAMPP.md)** | Docker-free deployment with XAMPP |
| **[Android App Configuration](wiki/07-Android-App-Configuration.md)** | Configure the mobile barcode scanner |
| **[Docker WMS Setup](wiki/10-Docker-WMS-Deployment.md)** | Deploy and use the web management system |
| **[📚 All Documentation](wiki/README.md)** | Complete documentation index |

## 🚀 Key Features

### **🔍 Advanced Barcode Detection**
- **Multi-Barcode Simultaneous Tracking**: Detect and track multiple barcodes in real-time
- **50+ Barcode Symbologies Support**: Including QR Code, Data Matrix, Code 128, UPC/EAN, PDF417, Aztec, and many more
- **AI-Powered Recognition**: Leverages Zebra AI Vision SDK v3.1.4 with barcode localizer model v5.0.1
- **Real-time Entity Tracking**: Visual overlay with bounding boxes and decoded values
- **Configurable Detection Settings**: Customizable symbology enabling/disabling

### **📊 Session Management**
- **Session-Based Workflow**: Organize barcode captures into manageable sessions
- **Multiple Export Formats**: 
  - Text files (.txt)
  - CSV files (.csv) 
  - Excel files (.xlsx) with Apache POI integration
- **File Browser Integration**: Navigate and manage session files with swipe gestures
- **Last Session Persistence**: Resume previous sessions seamlessly
- **Session Viewer**: Review captured data with edit and delete capabilities

### **✏️ Data Management**
- **In-App Editing**: Edit barcode data, quantities, and metadata
- **Swipe Gestures**: Left swipe to reveal edit and delete options
- **Quantity Management**: Track quantities with increment/decrement controls
- **Data Validation**: Form validation and error handling

### **🏢 Enterprise Features**
- **Complete Web Management System**: Real-time web interface for centralized barcode session monitoring and management
- **🌐 Advanced Multilingual Support**: Enterprise-grade translation system supporting 76+ Android languages with dynamic discovery
- **🚀 Ultra-Fast Translation Pipeline**: Speed-optimized translation engine with 10-20x performance improvements, batch processing, and parallel execution
- **🔄 Automated Translation Workflow**: AI-powered translation automation with Google Translate API integration and quality validation
- **🌍 Dynamic Language Discovery**: Automatic detection and population of available language files in the web interface
- **Flexible Deployment Options**: Production-ready deployment via Docker (containerized) or XAMPP (standalone) with Apache, MySQL, and phpMyAdmin
- **Enterprise REST API**: Comprehensive backend with session management, device tracking, and data export
- **Real-Time Data Synchronization**: Live dashboard updates with 1-second refresh intervals
- **Multi-Device Support**: Device hostname tracking and identification for enterprise environments
- **Managed Configuration Support**: Full EMM/MDM integration for enterprise deployment
- **Real-time Policy Updates**: Configuration changes applied without app restart
- **Nested Configuration Structure**: Organized settings with collapsible barcode symbology groups
- **Administrator Notifications**: Toast notifications for policy updates
- **Dynamic Registration**: Android 8.0+ compatible managed configuration system

### **🚨 Advanced Error Reporting**
- **Dual-Channel Feedback System**: 
  - EMM feedback channels for enterprise devices
  - Email fallback for comprehensive coverage
- **Automatic Error Reporting**: LogUtils.e() calls trigger detailed reports
- **Comprehensive Reports**: Include device info, app version, stack traces, and timestamps
- **Enterprise Integration**: Direct integration with EMM systems for error visibility

## 📱 Screenshots

### Android Application

<img width="216" height="432" alt="Entry Choice Screen" src="https://github.com/user-attachments/assets/c531d739-63c0-4e59-b8c8-7fc3b3899122" />
<img width="216" height="432" alt="Entry Choice Screen Menu" src="https://github.com/user-attachments/assets/1d02443f-b6c3-4dfe-8097-65951b6bf51f" />
<img width="216" height="432" alt="Camera Preview with Detection" src="https://github.com/user-attachments/assets/2314b57b-f47a-4add-9d24-2da1c2488bee" />
<img width="216" height="432" alt="Captured Barcodes View" src="https://github.com/user-attachments/assets/1602cd78-0f2f-4344-9f36-364268a3b0df" />
<img width="216" height="432" alt="Session Management Folders" src="https://github.com/user-attachments/assets/69b7ec1a-f087-48e1-809e-55a9ab34ef47" />
<img width="216" height="432" alt="Session Management File With Selection" src="https://github.com/user-attachments/assets/eeafe15b-3762-458c-bdb8-ecdf9dcaa7f5" />
<img width="216" height="432" alt="Session Management Menu" src="https://github.com/user-attachments/assets/5f70cade-5a57-44b9-a7e9-60c06f742e7a" />
<img width="216" height="432" alt="Settings Configuration" src="https://github.com/user-attachments/assets/52fd72f1-6213-44de-9c87-d65254301d2c" />
<img width="216" height="432" alt="Settings File Processing" src="https://github.com/user-attachments/assets/75682a75-b4b1-4f4b-965c-b77ab6c074d7" />
<img width="216" height="432" alt="Settings HTTP(s) Post" src="https://github.com/user-attachments/assets/c4d0aff3-e5db-4649-9f5f-86e2b9ff7fc7" />
<img width="216" height="432" alt="Settings Filter HTTPS URL With Regular Expression" src="https://github.com/user-attachments/assets/30f809d7-25b6-4a0e-ac63-1c200618ac35" />
<img width="216" height="432" alt="Session Data Editor" src="https://github.com/user-attachments/assets/c4e2279f-fb38-4211-974a-1f821e2e6307" />
<img width="216" height="432" alt="Session Data Editor Row Swipe Left" src="https://github.com/user-attachments/assets/ac8e3dfa-e95f-4fd7-af73-ef4767eeb0d1" />
<img width="216" height="432" alt="Barcode Quantity Editor" src="https://github.com/user-attachments/assets/030c9577-0a36-47f7-840b-685d1bf0301f" />

### WMS Barcode Web Server

<img width="616" height="470" alt="Barcode WMS WebServer" src="https://github.com/user-attachments/assets/753d4e9d-31f0-46fc-8f03-58a37e260796" />
<div/>
<img width="616" height="521" alt="Barcode WMS WebServer Session Detail" src="https://github.com/user-attachments/assets/f51adeca-ed9e-4531-9184-99e668174d4b" />
<div/>
<img width="270" height="283" alt="Barcode WMS WebServer Settings" src="https://github.com/user-attachments/assets/95e1360a-7171-42c1-9cc6-69cde79f2714" />
<img width="271" height="283" alt="Barcode WMS WebServer Settings Certificates" src="https://github.com/user-attachments/assets/51849efb-e487-4178-b141-0714962164c6" />
<img width="271" height="282" alt="Barcode WMS WebServer Settings Endpoints" src="https://github.com/user-attachments/assets/dadd22ea-e9af-4ef3-80a0-d5b92ddf7575" />
<img width="227" height="284" alt="Barcode WMS WebServer Settings Endpoint QRCode" src="https://github.com/user-attachments/assets/75e997a9-e5c7-49a5-a106-02c9f2151d92" />


## 🏗️ Architecture

### **Core Activities**
- **SplashActivity**: Application launcher with branding and permissions
- **EntryChoiceActivity**: Session management and AI Vision SDK initialization
- **CameraXLivePreviewActivity**: Real-time camera preview with barcode detection
- **CapturedBarcodesActivity**: Review and manage captured barcode data
- **SessionViewerActivity**: Browse and edit existing session data
- **BrowserActivity**: File management with swipe gesture support
- **SettingsActivity**: Configuration interface for app preferences
- **BarcodeDataEditorActivity**: In-app data editing with form validation

### **Key Systems**
- **BarcodeTracker**: Core barcode detection using Zebra AI Vision SDK
- **EntityTrackerAnalyzer**: Real-time multi-barcode tracking and analysis
- **ManagedConfigurationReceiver**: Enterprise policy management
- **LogUtils**: Centralized logging with automatic error reporting
- **FileUtil**: Session file management and export capabilities

## 🔧 Technical Specifications

### **Requirements**
- **Android 14+ (API 34)**: Minimum supported version
- **Target SDK 35**: Android 14+ optimization
- **Java 1.8**: Language compatibility

### **Dependencies**
- **Zebra AI Vision SDK**: v3.0.5 - Core barcode detection engine
- **Barcode Localizer Model**: v5.0.1 - AI model for barcode localization
- **CameraX**: v1.4.2 - Modern camera functionality
- **Apache POI**: v5.2.3 - Excel export capabilities
- **Gson**: v2.13.1 - JSON serialization
- **Material Design Components**: v1.12.0 - UI framework
- **Critical Permission Helper**: 0.8.1 - Automatically grant critical permissions (Camera, Manage All Files) [CriticalPermissionHelper Repository](https://github.com/ltrudu/CriticalPermissionsHelper)
- **DataWedge Intent Wrapper**: 14.10 - Simplifies the setup of DataWedge for barcode scanning in the Settings Activity [DataWedgeIntentWrapper Repository](https://github.com/ltrudu/DataWedgeIntentWrapper)

### **Build Configuration**
- **Android Gradle Plugin**: 8.11.0
- **Gradle Wrapper**: 8.9

## 📋 Configuration

### **Managed Configuration Options**
- **File Prefix**: Default prefix for exported session files
- **Export Format**: Choose between TXT, CSV, or XLSX
- **Barcode Symbologies**: Enable/disable specific barcode types
- **Real-time Updates**: Configuration changes applied without restart
  
- **More information on Managed Configuration here**: [Documentation](https://github.com/ZebraDevs/AI_MutliBarcodes_Capture/blob/master/MANAGED_CONFIGURATION.md)

### **Supported Barcode Types**
**2D Codes**: QR Code, Data Matrix, Aztec, PDF417, MaxiCode, and more  
**1D Codes**: Code 128, Code 39, UPC/EAN, GS1 DataBar, and more  
**Postal Codes**: US Planet, UK Postal, Canadian Postal, and more  
**Specialty Codes**: GS1 DataMatrix, Composite codes, DotCode, and more

## 🔍 Usage Examples

### **Basic Barcode Scanning (File Mode)**
1. Launch app and grant camera permissions
2. Create new session or load existing one
3. Point camera at barcodes for automatic detection
4. Tap "Capture" to save detected barcodes
5. Review and edit captured data
6. Swipe left a captured barcode data row to access to delete button or edit button
7. Use edit button to change quantity
8. Use the merge button to merge quantities if there were previously scanned data in the session
9. Export to desired format (TXT/CSV/XLSX)

### **Enterprise Web Management (HTTP Mode)**
1. Configure Docker environment and start web services
2. Set Android app to HTTP(s) Post mode with endpoint URL
3. Scan barcodes and upload sessions to web management system
4. Monitor real-time scanning activity via web dashboard
5. Manage sessions, view device information, and export data
6. Track multiple devices simultaneously with hostname identification

### **Enterprise Deployment**
1. Configure barcode symbologies via EMM console
2. Deploy app with managed configuration
3. App receives real-time policy updates
4. Error reports sent via EMM feedback channels
5. Monitor app usage through enterprise dashboards

## 🔗 Additional Resources

**📚 Complete Documentation Wiki:**
[Comprehensive Setup and Deployment Guides](wiki/README.md) - 15+ detailed guides covering everything from quick start to enterprise deployment

**Zebra AI Vision SDK Documentation:**
https://techdocs.zebra.com/ai-datacapture/latest/about/

**More Android AI Samples:**
https://github.com/ZebraDevs/AISuite_Android_Samples

## 📞 Support

*Please be aware that this library / application / sample is provided as a community project without any guarantee of support*

For technical questions and community support:
- GitHub Issues: Report bugs and feature requests in the original repository: [link](https://github.com/ltrudu/AI_MutliBarcodes_Capture)
- Zebra Developer Portal: Technical documentation and resources
- Community Forums: Connect with other developers

## License

All content under this repository's root folder is subject to the [Development Tool License Agreement](https://github.com/ZebraDevs/AISuite_Android_Samples/blob/main/Zebra%20Development%20Tool%20License.pdf). By accessing, using, or distributing any part of this content, you agree to comply with the terms of the Development Tool License Agreement.

# AI MultiBarcode Capture Application

[![License](https://img.shields.io/badge/License-Zebra%20Development%20Tool-blue)](https://github.com/ZebraDevs/AISuite_Android_Samples/blob/main/Zebra%20Development%20Tool%20License.pdf) [![Platform](https://img.shields.io/badge/Platform-Android-green)](https://developer.android.com/) [![Language](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com/) [![Version](https://img.shields.io/badge/Version-1.24-brightgreen)](CHANGELOG.md) [![API](https://img.shields.io/badge/API-34%2B-yellow)](https://developer.android.com/about/versions/14) [![SDK](https://img.shields.io/badge/Zebra%20AI%20Vision%20SDK-3.0.2-blue)](https://developer.zebra.com/)

[![Apache](https://img.shields.io/badge/Apache-2.4-red)](https://httpd.apache.org/) [![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/) [![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/) [![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML) [![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript) [![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)

A comprehensive Android enterprise application demonstrating Zebra AI Vision SDK capabilities for simultaneous multi-barcode detection, tracking, and session-based data management with enterprise deployment features.

**📚 [Complete Setup & Deployment Documentation](wiki/README.md)** - Comprehensive guides for quick start, Docker deployment, API integration, and enterprise configuration.

**Please note that this is a Work In Progress.**

Report any issues using the Issues manager of the original repository:
https://github.com/ltrudu/AI_MutliBarcodes_Capture

## 📅 What's New

### **Version 1.24** 🚀
**Enterprise QR Code Configuration & Enhanced Docker Deployment**

Revolutionary **QR Code endpoint configuration** capabilities with major Docker infrastructure improvements:

• **📱 QR Code Endpoint Configuration**: Automatically configure HTTP endpoints by scanning QR codes from the Web Management System - eliminates manual URL entry and deployment complexity

• **Zero-Typing Setup**: WMS generates QR codes for instant mobile configuration with Zebra Imager integration

• **🐳 Unified Docker Container**: Single container deployment containing Apache+PHP web server, MySQL database, and phpMyAdmin in one unified container

• **Simplified Deployment**: Enhanced deployment scripts for Windows (`start-services.bat`) and Linux/macOS (`start-services.sh`) with automated container management

• **🌐 Enhanced WMS Interface**: Built-in QR code generator, improved responsive design, and better real-time session monitoring

• **📖 Comprehensive Documentation**: Complete QR code configuration guide with troubleshooting, security guidelines, and deployment procedures

**Key Technical Features:**
• **Automatic Endpoint Detection**: Android app processes QR codes with `AIMultiBarcodeEndpoint:` prefix for instant configuration

• **DataWedge Integration**: Built-in QR code scanning via DataWedge profile in settings with toast confirmation

• **Container Architecture**: Multi-service container with supervisord management and persistent data storage

• **Security & Validation**: Input validation, endpoint verification, and secure configuration handling

**Enterprise Benefits:**
• **Simplified IT Deployment**: Zero configuration errors with QR code scanning

• **Rapid Device Setup**: Multiple devices configured in seconds

• **Scalable Infrastructure**: Single container management reduces operational overhead

• **Enhanced Monitoring**: Better visibility into device connections and configurations

The QR Code configuration system transforms enterprise deployment from complex manual setup to simple one-scan configuration, while the unified Docker container architecture simplifies deployment and maintenance for IT teams.

📋 **[View Complete Changelog](CHANGELOG.md)** for previous versions and detailed release history.

## 📖 Quick Start Links

| Guide | Description |
|-------|-------------|
| **[15-Minute Quick Start](wiki/01-Quick-Start-Guide.md)** | Get the system running in 15 minutes |
| **[Installation Guide](wiki/03-Installation-Guide.md)** | Complete system installation |
| **[Android App Configuration](wiki/07-Android-App-Configuration.md)** | Configure the mobile barcode scanner |
| **[Docker WMS Setup](wiki/10-Docker-WMS-Deployment.md)** | Deploy and use the web management system |
| **[📚 All Documentation](wiki/README.md)** | Complete documentation index |

## 🚀 Key Features

### **🔍 Advanced Barcode Detection**
- **Multi-Barcode Simultaneous Tracking**: Detect and track multiple barcodes in real-time
- **50+ Barcode Symbologies Support**: Including QR Code, Data Matrix, Code 128, UPC/EAN, PDF417, Aztec, and many more
- **AI-Powered Recognition**: Leverages Zebra AI Vision SDK v3.0.2 with barcode localizer model v5.0.1
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
- **Docker Infrastructure**: Production-ready containerized deployment with Apache, MySQL, and phpMyAdmin
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

<img width="216" height="432" alt="Entry Choice Screen" src="https://github.com/user-attachments/assets/c531d739-63c0-4e59-b8c8-7fc3b3899122" />
<img width="216" height="432" alt="Entry Choice Screen Menu" src="https://github.com/user-attachments/assets/1d02443f-b6c3-4dfe-8097-65951b6bf51f" />
<img width="216" height="432" alt="Camera Preview with Detection" src="https://github.com/user-attachments/assets/bfa7b4dc-6595-4991-8ea5-494ce4a88aa7" />
<img width="216" height="432" alt="Captured Barcodes View" src="https://github.com/user-attachments/assets/1602cd78-0f2f-4344-9f36-364268a3b0df" />
<img width="216" height="432" alt="Session Management Folders" src="https://github.com/user-attachments/assets/69b7ec1a-f087-48e1-809e-55a9ab34ef47" />
<img width="216" height="432" alt="Session Management File With Selection" src="https://github.com/user-attachments/assets/eeafe15b-3762-458c-bdb8-ecdf9dcaa7f5" />
<img width="216" height="432" alt="Session Management Menu" src="https://github.com/user-attachments/assets/5f70cade-5a57-44b9-a7e9-60c06f742e7a" />
<img width="216" height="432" alt="Settings Configuration" src="https://github.com/user-attachments/assets/83f092b9-41be-4346-a29b-a3931aa70707" />
<img width="216" height="432" alt="Session Data Editor" src="https://github.com/user-attachments/assets/c4e2279f-fb38-4211-974a-1f821e2e6307" />
<img width="216" height="432" alt="Session Data Editor Row Swipe Left" src="https://github.com/user-attachments/assets/ac8e3dfa-e95f-4fd7-af73-ef4767eeb0d1" />
<img width="216" height="432" alt="Barcode Quantity Editor" src="https://github.com/user-attachments/assets/030c9577-0a36-47f7-840b-685d1bf0301f" />

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
- **Zebra AI Vision SDK**: v3.0.2 - Core barcode detection engine
- **Barcode Localizer Model**: v5.0.1 - AI model for barcode localization
- **CameraX**: v1.4.2 - Modern camera functionality
- **Apache POI**: v5.2.3 - Excel export capabilities
- **Gson**: v2.13.1 - JSON serialization
- **Material Design Components**: v1.12.0 - UI framework
- **Critical Permission Helper**: 0.8.1 - Automatically grant critical permissions (Camera, Manage All Files) [CriticalPermissionHelper Repository](https://github.com/ltrudu/CriticalPermissionsHelper)

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

# AI MultiBarcode Capture Application

[![License](https://img.shields.io/badge/License-Zebra%20Development%20Tool-blue)](https://github.com/ZebraDevs/AISuite_Android_Samples/blob/main/Zebra%20Development%20Tool%20License.pdf) [![Platform](https://img.shields.io/badge/Platform-Android-green)](https://developer.android.com/) [![Language](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com/) [![Version](https://img.shields.io/badge/Version-1.26-brightgreen)](CHANGELOG.md) [![API](https://img.shields.io/badge/API-34%2B-yellow)](https://developer.android.com/about/versions/14) [![SDK](https://img.shields.io/badge/Zebra%20AI%20Vision%20SDK-3.0.2-blue)](https://developer.zebra.com/)

[![Apache](https://img.shields.io/badge/Apache-2.4-red)](https://httpd.apache.org/) [![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/) [![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/) [![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML) [![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript) [![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)

A comprehensive Android enterprise application demonstrating Zebra AI Vision SDK capabilities for simultaneous multi-barcode detection, tracking, and session-based data management with enterprise deployment features.

**📚 [Complete Setup & Deployment Documentation](wiki/README.md)** - Comprehensive guides for quick start, Docker deployment, API integration, and enterprise configuration.

**Please note that this is a Work In Progress.**

Report any issues using the Issues manager of the original repository:
https://github.com/ltrudu/AI_MutliBarcodes_Capture

## 📅 What's New

### **Server Update** 🔄
**Network IP Update Scripts**

Added automatic IP update scripts to handle network changes when connecting to new WiFi networks or different locations.

• **New Scripts**: `update-network-ip.bat` (Windows) and `update-network-ip.sh` (Linux/macOS) automatically detect and update IP configuration

• **Antivirus-Safe**: Scripts use only standard system commands to avoid security software conflicts

• **Data Preservation**: Docker container restart maintains all session and barcode data

• **Updated Documentation**: Added "[Managing IP Changes](wiki/11-Managing-IP-Changes.md)" guide to the wiki

**🔄 WebServer Update Scripts:**
• **Live Updates**: `update-webserver.bat` (Windows) and `update-webserver.sh` (Linux/macOS) update website files without rebuilding containers

• **Smart Container Management**: Automatically handles container status checking, starting stopped containers when needed

• **Complete File Sync**: Updates all website files, API endpoints, configurations, and language translations in running containers

• **Zero Data Loss**: Updates preserve all database data and user sessions while applying latest code changes

• **Development Workflow**: Streamlined git-to-deployment process for efficient development and maintenance

• **Comprehensive Guide**: Complete documentation available in "[Updating the Server](wiki/12-Updating-Server.md)" wiki page

### **Version 1.26** 🚀
**Enhanced Enterprise Managed Configuration**

Complete **managed configuration synchronization** with comprehensive **HTTP/HTTPS endpoint management** for enterprise deployment.

### **Version 1.25** 🚀
**Enterprise Export System & Enhanced Web Management**

Major **data export capabilities** and **advanced web interface enhancements** with significant infrastructure improvements:

• **📊 Complete Export System**: Full data export functionality supporting TXT, CSV, and native XLSX formats - replicating Android app export capabilities in the web interface

• **📈 Real XLSX Generation**: Native Excel file creation using custom SimpleXLSXWriter library with proper OpenXML format - no more CSV-to-Excel conversion

• **🎯 Enhanced User Experience**: Improved barcode processing workflow with visual feedback and optimized interaction design

• **🌐 Advanced Translation Updates**: Updated translation files across 76+ languages with new export-related terminology and UI improvements

• **🔧 Smart IP Resolution**: Automatic host IP detection for Docker containers, eliminating manual IP configuration for Android connectivity

• **🛡️ Simplified Security**: Global cleartext HTTP traffic permission for development environments, removing IP-specific network restrictions

• **⚡ Performance Optimizations**: Enhanced startup scripts with automatic IP detection and improved container lifecycle management

• **🐳 Docker Infrastructure**: Enhanced container management with automatic host IP detection and improved service reliability

**Key Technical Features:**
• **Multi-Format Export API**: RESTful API supporting TXT, CSV, and XLSX exports with session-based data filtering

• **Native XLSX Writer**: Custom lightweight XLSX generation without external dependencies using PHP ZipArchive

• **Intelligent IP Detection**: Multi-method host IP detection prioritizing 192.168.x.x networks with Docker container filtering

• **Enhanced UX Workflows**: Improved barcode marking system with better visual feedback and user interaction patterns

• **Cross-Browser Compatibility**: Consistent UI rendering across Chrome, Edge, and other browsers with enhanced CSS styling

• **Automated Network Configuration**: Smart network security configuration removing manual IP management complexity

**Enterprise Benefits:**
• **Seamless Data Export**: Web-based export system matches Android app functionality for unified data management

• **Simplified Network Setup**: Automatic IP resolution eliminates network configuration complexity

• **Enhanced Productivity**: Improved user workflows reduce time-to-action for barcode processing operations

• **Better Integration**: Unified export system enables seamless data flow between mobile and web platforms

This release significantly enhances enterprise data management capabilities while simplifying deployment and network configuration for IT teams.

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
- **🌐 Advanced Multilingual Support**: Enterprise-grade translation system supporting 76+ Android languages with dynamic discovery
- **🚀 Ultra-Fast Translation Pipeline**: Speed-optimized translation engine with 10-20x performance improvements, batch processing, and parallel execution
- **🔄 Automated Translation Workflow**: AI-powered translation automation with Google Translate API integration and quality validation
- **🌍 Dynamic Language Discovery**: Automatic detection and population of available language files in the web interface
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

# Changelog

All notable changes to the AI MultiBarcode Capture Application are documented in this file.

## Version 1.27
**🔓 Simplified Android Authentication**

Streamlined Android application HTTP post configuration by removing authentication complexity for improved demonstration and development experience.

### Major Changes:
• **Authentication Removal**: Completely removed authentication checkbox, username, and password fields from Android app HTTP post settings
• **Simplified Configuration**: Android app now requires only endpoint URL configuration for HTTP/HTTPS communication
• **UI Cleanup**: Removed authentication-related UI components from settings layout (activity_setup.xml)
• **Code Simplification**: Eliminated KeystoreHelper dependencies and authentication logic throughout the application

This release focuses on simplifying the user experience for demonstration and development scenarios while preserving all essential application functionality.

### **Server Update** 🔄
**Network IP Update Scripts**

Added automatic IP update scripts to handle network changes when connecting to new WiFi networks or different locations.

• **New Scripts**: `update-network-ip.bat` (Windows) and `update-network-ip.sh` (Linux/macOS) automatically detect and update IP configuration

• **Antivirus-Safe**: Scripts use only standard system commands to avoid security software conflicts

• **Data Preservation**: Docker container restart maintains all session and barcode data

• **Updated Documentation**: Added "[Managing IP Changes](wiki/11-Managing-IP-Changes.md)" guide to the wiki

**WebServer Update Scripts:**
• **Live Updates**: `update-webserver.bat` (Windows) and `update-webserver.sh` (Linux/macOS) update website files without rebuilding containers

• **Smart Container Management**: Automatically handles container status checking, starting stopped containers when needed

• **Complete File Sync**: Updates all website files, API endpoints, configurations, and language translations in running containers

• **Zero Data Loss**: Updates preserve all database data and user sessions while applying latest code changes

• **Development Workflow**: Streamlined git-to-deployment process for efficient development and maintenance

• **Comprehensive Guide**: Complete documentation available in "[Updating the Server](wiki/12-Updating-Server.md)" wiki page

**HTTPS Certificate Management & Download Features:**
• **🔐 Automatic SSL Certificate Generation**: Self-signed CA and server certificates automatically generated with `create-certificates.bat` and `create-certificates.sh` scripts

• **📥 Web-Based Certificate Downloads**: Download CA certificates directly from the web interface settings for easy Windows and Android installation

• **🌐 Secure HTTPS Support**: Full HTTPS implementation with Apache SSL on port 3543, providing encrypted communication for enterprise environments

• **📱 Android System Certificate Support**: Generated Android system certificates (`.pem` format) for device-wide SSL trust without app-embedded certificates

• **🪟 Windows Certificate Integration**: Windows-compatible CA certificates for browser trust and enterprise certificate management

• **📖 Interactive Installation Guide**: Built-in modal popup with step-by-step certificate installation instructions for Windows and Android platforms

• **🔄 Certificate Deployment Pipeline**: Automated certificate copying to web-accessible directories during container startup and updates

• **🛡️ Enterprise Security**: Complete PKI infrastructure with certificate chain validation for secure enterprise communications


## Version 1.26
**📋 Enhanced Enterprise Managed Configuration**

Complete managed configuration synchronization with comprehensive HTTP/HTTPS endpoint management for enterprise deployment.

## Version 1.25
**📊 Enterprise Export System & Enhanced Web Management**

Major data export capabilities and advanced web interface enhancements with significant infrastructure improvements developed in the Improve_WebInterface branch:

### Major New Features:
• **📊 Complete Export System**: Full data export functionality supporting TXT, CSV, and native XLSX formats - replicating Android app export capabilities in the web interface with session-based filtering
• **📈 Real XLSX Generation**: Native Excel file creation using custom SimpleXLSXWriter library with proper OpenXML format - eliminating CSV-to-Excel conversion dependencies
• **🎯 Enhanced User Experience**: Improved barcode processing workflow with visual feedback, optimized interaction design, and streamlined user operations
• **🌐 Advanced Translation Updates**: Updated translation files across 76+ languages with new export-related terminology, UI improvements, and enhanced multilingual support
• **🔧 Smart IP Resolution**: Automatic host IP detection for Docker containers, eliminating manual IP configuration complexity for Android connectivity
• **🛡️ Simplified Security Configuration**: Global cleartext HTTP traffic permission for development environments, removing IP-specific network restrictions and configuration overhead

### Technical Enhancements:
• **Multi-Format Export API**: RESTful API supporting TXT, CSV, and XLSX exports with session-based data filtering and comprehensive error handling
• **Native XLSX Writer**: Custom lightweight XLSX generation without external dependencies using PHP ZipArchive with proper OpenXML structure
• **Intelligent IP Detection**: Multi-method host IP detection prioritizing 192.168.x.x networks with Docker container filtering and automatic fallback mechanisms
• **Enhanced UX Workflows**: Improved barcode marking system with better visual feedback, user interaction patterns, and processing state management
• **Cross-Browser Compatibility**: Consistent UI rendering across Chrome, Edge, and other browsers with enhanced CSS styling and responsive design
• **Automated Network Configuration**: Smart network security configuration removing manual IP management complexity for Android app connectivity

### Export System Features:
• **Session-Based Export**: Export functionality with multi-session selection and batch processing capabilities
• **Format Consistency**: Export formats match Android app output exactly (TXT with headers, CSV with semicolon delimiters, XLSX with proper Excel formatting)
• **Real-time Export**: Direct download functionality with proper MIME types and browser compatibility
• **Export API Debugging**: Comprehensive debug system with detailed error reporting and troubleshooting capabilities
• **Composer Integration**: Enhanced Docker configuration with Composer support for PHP dependencies and library management

### Infrastructure Improvements:
• **Enhanced Startup Scripts**: Automatic IP detection in start-services.bat (Windows) and start-services.sh (Linux/macOS) with intelligent network interface detection
• **Docker Container Management**: Improved container lifecycle with automatic host IP environment variable passing and service reliability
• **Network Security Enhancement**: Global cleartext traffic permission in network_security_config.xml eliminating specific IP address maintenance
• **Database Schema Optimization**: Corrected table references and field mappings for improved data consistency and export accuracy

### Android App Enhancements:
• **Simplified Network Configuration**: Global cleartext HTTP permission eliminating the need for specific IP address maintenance in network security configuration
• **Enhanced Connectivity**: Improved connection reliability with automatic network configuration handling for various deployment scenarios
• **Version Update**: Application version updated to 1.25 reflecting the enhanced export and connectivity capabilities

### Enterprise Benefits:
• **Unified Data Management**: Web-based export system matches Android app functionality providing seamless data flow between mobile and web platforms
• **Simplified Deployment**: Automatic IP resolution and network configuration eliminates IT configuration complexity and reduces deployment errors
• **Enhanced Productivity**: Improved user workflows and export capabilities reduce time-to-action for barcode processing operations and data management
• **Better Integration**: Complete export system enables seamless enterprise data integration with existing business systems and workflows
• **Reduced Maintenance**: Automated network configuration and intelligent IP detection minimize ongoing IT maintenance requirements

This release significantly enhances enterprise data management capabilities while simplifying deployment and network configuration complexity for IT teams. The complete export system provides web-based functionality matching the Android app's capabilities, enabling unified enterprise data workflows.

## Version 1.24
**🚀 Enterprise QR Code Configuration & Enhanced Docker Deployment**

Revolutionary QR Code endpoint configuration capabilities with major Docker infrastructure improvements developed in the Update_WebInterface branch:

### Major New Features:
• **📱 QR Code Endpoint Configuration System**: Automatic HTTP endpoint configuration by scanning QR codes from the Web Management System, eliminating manual URL entry and deployment complexity
• **Zero-Typing Setup**: WMS generates QR codes for instant mobile configuration with DataWedge/Zebra Imager integration
• **🐳 Unified Docker Container Architecture**: Single container deployment containing Apache+PHP web server, MySQL database, and phpMyAdmin services
• **Enhanced Deployment Scripts**: Automated deployment with `start-services.bat` (Windows) and `start-services.sh` (Linux/macOS)
• **🌐 Enhanced Web Management System**: Built-in QR code generator, improved responsive design, and better real-time session monitoring
• **📖 Comprehensive QR Code Documentation**: Complete setup guide with troubleshooting, security guidelines, and deployment procedures

### Technical Enhancements:
• **Automatic QR Code Processing**: Android app detects QR codes with `AIMultiBarcodeEndpoint:` prefix for instant endpoint configuration
• **DataWedge Integration**: Built-in QR code scanning via DataWedge profile in SettingsActivity with toast confirmation messages
• **Multi-Service Container**: Supervisord-managed container with Apache, MySQL, and phpMyAdmin services
• **Enhanced Security**: Input validation, endpoint verification, and secure configuration handling
• **Container Orchestration**: Persistent data storage, health monitoring, and simplified management

### Android App Enhancements:
• **Settings QR Code Scanning**: Integrated QR code detection in SettingsActivity for endpoint configuration
• **Automatic Validation**: Real-time endpoint format validation and user feedback
• **String Internationalization**: Added "endpoint_updated_from_qr" message for successful configuration
• **Thread-Safe Updates**: Proper UI thread handling for QR code processing with runOnUiThread()

### Web Management System Features:
• **QR Code Generation**: Built-in QR code generator for endpoint configuration sharing
• **Enhanced User Interface**: Improved responsive design for mobile and desktop compatibility
• **Better Session Monitoring**: Enhanced real-time updates and device tracking capabilities
• **Simplified Deployment**: Single container architecture reduces operational complexity

### Docker Infrastructure:
• **Unified Container**: Single multibarcode-webinterface container with all services
• **Automated Scripts**: Platform-specific deployment scripts for easy setup
• **Container Management**: Simplified lifecycle management and maintenance procedures
• **Development & Production**: Flexible configuration for different deployment environments

### Documentation Updates:
• **QR Code Configuration Guide**: Step-by-step instructions in Android App Configuration wiki
• **Troubleshooting Section**: Common QR code scanning issues and solutions
• **Security Guidelines**: Best practices for QR code deployment in enterprise environments
• **Docker Deployment**: Updated procedures for unified container architecture

### Enterprise Benefits:
• **Simplified IT Deployment**: Zero configuration errors with QR code scanning eliminates manual URL mistakes
• **Rapid Device Setup**: Multiple devices can be configured in seconds with QR code scanning
• **Scalable Infrastructure**: Single container management reduces operational overhead and complexity
• **Enhanced Monitoring**: Better visibility into device connections, configurations, and session data
• **User-Friendly Setup**: No technical knowledge required for endpoint configuration

The QR Code configuration system transforms enterprise deployment from complex manual setup to simple one-scan configuration, while the unified Docker container architecture simplifies deployment and maintenance for IT teams. This release establishes AI MultiBarcode Capture as the premier enterprise barcode scanning solution with industry-leading deployment simplicity.

## Version 1.23
**🚀 Enterprise Web Management System with Real-Time Data Synchronization**

Revolutionary enterprise-grade web management system with complete Docker deployment and real-time barcode data synchronization:

### Major New Features:
• **Complete Web Management System (WMS)**: Full-featured web interface for real-time barcode session monitoring and management with live dashboard
• **HTTP(s) Post Integration**: Dual-mode operation enabling Android app to upload data directly to web backend via HTTP/HTTPS endpoints
• **Real-Time Data Synchronization**: Live dashboard with 1-second refresh intervals showing barcode captures as they happen across multiple devices
• **Docker Infrastructure**: Complete containerized deployment stack with Apache web server, MySQL database, and phpMyAdmin administration interface
• **Enterprise REST API**: Comprehensive backend API with session management, barcode processing, data export, and device tracking capabilities
• **Device Hostname Tracking**: Automatic device identification with unique hostname generation (Manufacturer_Model_AndroidVersion) for multi-device environments
• **Comprehensive Documentation**: 15+ detailed wiki guides covering setup, deployment, Docker configuration, API integration, and troubleshooting

### Technical Enhancements:
• **Dual Processing Modes**: Seamless switching between File-based (offline) and HTTP(s) Post (real-time) processing modes
• **Network Security Configuration**: Automatic cleartext HTTP support for development environments with network_security_config.xml
• **Symbology Mapping System**: Accurate barcode type identification and display in web interface with corrected database mappings
• **Complete Database Schema**: Optimized MySQL database with sessions, barcodes, and symbology_types tables with proper foreign key relationships
• **Multi-Format Export**: Web-based export to Excel (.xlsx), CSV (.csv), and text (.txt) formats with batch operations and session management
• **Production-Ready Architecture**: Database optimization, security configuration, performance tuning, and horizontal scalability features

### Web Management System Features:
• **Real-Time Session Monitoring**: Live display of scanning sessions with device information, barcode counts, and timestamps
• **Session Detail Views**: Comprehensive barcode data display with symbology types, timestamps, and metadata
• **Data Reset Functionality**: Complete database reset capability with confirmation dialogs and real-time UI updates
• **Responsive Design**: Modern web interface optimized for desktop and mobile viewing with Zebra branding
• **Auto-Refresh Technology**: Silent background updates preventing UI flickering during real-time data synchronization

### Docker Deployment System:
• **Multi-Container Architecture**: Apache+PHP web server, MySQL 8.0 database, and phpMyAdmin in orchestrated containers
• **Environment Configuration**: Flexible environment variable configuration with development and production profiles
• **Volume Management**: Persistent data storage with backup capabilities and maintenance scripts
• **Network Configuration**: Isolated container networking with proper port mapping and security controls
• **Health Monitoring**: Built-in health checks and logging for all container services

### Android App Enhancements:
• **Settings Mode Selection**: New processing mode setting allowing users to choose between File-based and HTTP(s) Post modes
• **Endpoint Configuration**: HTTP(s) endpoint URL configuration with validation and connection testing
• **Upload Functionality**: Session data upload with JSON payload including device information and barcode arrays
• **Error Handling**: Comprehensive network error handling with user feedback and retry mechanisms
• **Connection Validation**: Built-in connectivity testing for endpoint validation and network troubleshooting

### API and Database Features:
• **RESTful API Design**: Well-structured endpoints for session creation, barcode insertion, data retrieval, and system management
• **Database Optimization**: Indexed tables, optimized queries, and performance tuning for high-volume barcode processing
• **Security Features**: SQL injection protection, input validation, and prepared statement usage throughout the backend
• **Error Logging**: Comprehensive server-side logging with detailed error reporting and debugging information
• **Backup and Maintenance**: Automated backup scripts and database maintenance procedures for production environments

The enterprise web management system transforms the standalone Android app into a complete enterprise solution with centralized monitoring, real-time data synchronization, and comprehensive deployment capabilities for production environments.

## Version 1.22
**Enhanced Session Management with Advanced Folder Operations**

Comprehensive session file management with intelligent UI and folder operations:

• **Folder Long Press Selection**: Implemented long press gesture detection (500ms timeout) to select folders for rename and delete operations with haptic feedback  
• **Context-Sensitive UI Controls**: Smart button visibility system where Select/Share buttons only appear when files are selected (hidden for folders since they cannot be shared or exported)  
• **Dynamic Menu System**: Rename and Delete menu options automatically show/hide based on selection state - only visible when a file or folder is selected  
• **Enhanced File Operations**: Separated file and folder handling with dedicated methods (renameFile/renameFolder, deleteSelectedFileOrFolder) and appropriate user messaging  
• **Intelligent Touch Handling**: Long press cancellation when finger moves >20px prevents accidental folder selection, proper gesture cleanup on ACTION_UP/ACTION_CANCEL  
• **Complete Internationalization**: Added 4 new strings (please_select_file_or_folder, cannot_rename_parent_folder, error_renaming_folder, rename_folder) translated across all 72 supported languages  
• **Improved User Experience**: Unified file/folder management interface with consistent visual selection feedback and context-aware error messaging  

### Technical Enhancements:
• Enhanced FileAdapter with OnSelectionChangeListener interface for real-time UI updates  
• Added long press detection using Handler with configurable timeout and movement cancellation  
• Implemented selection state callbacks to automatically update button and menu visibility  
• Modified visual selection logic to support both files and folders (excluding parent directory)  
• Fixed translation automation script double-escaping issues causing build failures  
• Maintained 100% translation coverage (197 strings × 72 languages = 14,184 translations)  

The enhanced session management system provides intuitive folder operations while maintaining smart UI behavior that adapts to user selection context, ensuring optimal usability for both file and folder management tasks.

## Version 1.21
**Advanced AI Configuration System with Dynamic Settings Management**

Comprehensive AI and camera configuration capabilities with enterprise-grade managed configuration support:

• **Dynamic AI Configuration**: Advanced settings for AI model input size with three performance-optimized presets (640x640 Small, 1280x1280 Medium, 1600x1600 Large) including speed/accuracy guidance  
• **Camera Resolution Control**: Four resolution options (1MP, 2MP, 4MP, 8MP) with specific use case recommendations for different barcode types and distances  
• **Inference Processor Selection**: DSP (Digital Signal Processor), GPU (Graphics Processing Unit), and CPU (Central Processing Unit) options with performance characteristics  
• **Enterprise Managed Configuration**: Full EMM/MDM support with nested "advanced" settings bundle containing all three new configuration options  
• **Real-time Configuration Updates**: Settings changes automatically restart CameraXLivePreviewActivity and reload BarcodeTracker configuration without full app restart  
• **Performance-Aware UI**: Smart descriptions guide users to optimal settings based on scanning requirements (large/close barcodes vs small/distant barcodes)  
• **Constants-Based Architecture**: All hardcoded settings moved to Constants class with consistent preference key naming patterns  
• **Automatic Translation System**: Enhanced translation workflow with comprehensive verification and 72 language support including high-quality translations for major languages  

### Technical Enhancements:
• Removed hardcoded `InferencerOptions.DSP` and `640x640` dimensions from BarcodeTracker  
• Dynamic shared preferences loading with enum-based configuration (EInferenceType.toInferencerOptions(), EModelInputSize.getWidth/getHeight(), ECameraResolution.getWidth/getHeight())  
• ManagedConfigurationReceiver enhanced with nested bundle support and activity restart mechanism  
• Universal translation script with intelligent Android project detection and detailed completion reporting

The advanced configuration system provides granular control over AI performance and camera capabilities, enabling optimal barcode detection for different use cases while supporting enterprise deployment scenarios through comprehensive managed configuration integration.

## Version 1.20
**Comprehensive language selection system with 71+ language support**

Add complete internationalization capabilities with comprehensive language selection and localization:

• Language selection dropdown in settings with native language names and Unicode flag emojis (🇫🇷 Français, 🇪🇸 Español, 🇩🇪 Deutsch, etc.)
• Support for 71+ languages including System Language option with automatic detection  
• Validation-based language switching - changes apply only when settings are confirmed, not immediately  
• Complete string externalization with translations for all supported languages  
• Persistent language preference storage that survives app restarts and device reboots  
• Full application restart mechanism ensuring proper locale switching across all activities  
• Custom dropdown styling with white background and zebra blue outline matching app theme
• Improved settings UI with button reordering (Cancel left, Validate right)
• Comprehensive locale management through LocaleHelper utility with flag emoji mapping
• Enhanced user experience with language changes requiring explicit validation

The language system provides enterprise-grade internationalization support, allowing users to override system language preferences while maintaining consistent UI theming and providing immediate visual feedback through country flag emojis.

## Version 1.18
**Persistent flashlight toggle with automatic state restoration**

Enhance camera functionality with persistent flashlight control that remembers user preferences across app sessions:

• Flashlight toggle icon automatically restores previous on/off state when opening camera view  
• Flashlight setting persists through app restarts, activity navigation, and device orientation changes    
• Improved user experience with consistent lighting preferences across barcode capture workflows  

Users can now toggle the flashlight on during scanning and expect it to remain active when returning to the camera view, providing continuous lighting support for challenging scanning environments without needing to manually re-enable the flashlight each time.

## Version 1.17
**Implement interactive capture zone with barcode filtering**

Add a complete capture zone system that allows users to define a rectangular area for focused barcode scanning:

• Interactive capture zone overlay with drag, resize, and corner anchor controls  
• Visual toggle icon in top-right corner with enabled/disabled states using alpha transparency  
• Real-time barcode filtering - only process barcodes within the capture zone boundaries  
• Persistent settings that save zone position, size, and enabled state across app sessions  
• Comprehensive capture data filtering - only capture barcodes within the defined zone  
• Force portrait mode across entire application for consistent user experience  
• Enhanced logging and error handling for capture zone operations  

The capture zone provides users with precise control over barcode detection, allowing them to focus on specific areas of the camera view while ignoring barcodes outside the defined region. When enabled, both the real-time preview and final capture results only include barcodes that intersect with the capture zone.
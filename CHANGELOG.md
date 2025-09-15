# Changelog

All notable changes to the AI MultiBarcode Capture Application are documented in this file.

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
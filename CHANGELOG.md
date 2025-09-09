# Changelog

All notable changes to the AI MultiBarcode Capture Application are documented in this file.

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
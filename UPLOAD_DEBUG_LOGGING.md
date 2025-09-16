# Upload Debug Logging Implementation

## 🔍 **Comprehensive Logging Added**

I've added extensive logging throughout the upload process to help investigate the error toast issue. The logging covers every step of the upload process.

## 📋 **Log Tags and Levels**

All logs use the tag `"CapturedBarcodes"` with different levels:
- **`Log.d`** - Debug information (general flow)
- **`Log.v`** - Verbose information (detailed data)
- **`Log.w`** - Warnings (potential issues)
- **`Log.e`** - Errors (exceptions and failures)
- **`Log.i`** - Information (success events)

## 🔧 **What's Being Logged**

### **1. uploadData() Method**
```
CapturedBarcodes: uploadData() called
CapturedBarcodes: isHttpsPostMode: true/false
CapturedBarcodes: endpointUri: [URL]
CapturedBarcodes: SessionData created with [X] barcodes
CapturedBarcodes: UI updated to show uploading state
CapturedBarcodes: Starting background upload task
CapturedBarcodes: Updating UI on main thread
CapturedBarcodes: Upload successful/failed, showing [success/failure] toast
```

### **2. convertSessionDataToJson() Method**
```
CapturedBarcodes: convertSessionDataToJson() called
CapturedBarcodes: Processing [X] barcodes
CapturedBarcodes: Processing barcode ID: [ID], value: [VALUE]
CapturedBarcodes: Symbology for [ID]: [TYPE]
CapturedBarcodes: Quantity for [ID]: [COUNT]
CapturedBarcodes: Timestamp for [ID]: [ISO8601]
CapturedBarcodes: Successfully processed [X] barcodes for JSON
CapturedBarcodes: Session timestamp: [ISO8601]
CapturedBarcodes: JSON conversion completed successfully
```

### **3. performHttpPost() Method**
```
CapturedBarcodes: performHttpPost() called
CapturedBarcodes: Endpoint URL: [URL]
CapturedBarcodes: JSON data length: [X] bytes
CapturedBarcodes: URL created successfully
CapturedBarcodes: HTTP connection opened
CapturedBarcodes: Detected HTTPS/HTTP connection
CapturedBarcodes: Setting request method and headers
CapturedBarcodes: Headers set: POST, JSON content-type, timeouts configured
CapturedBarcodes: Sending JSON data to server
CapturedBarcodes: Writing [X] bytes to output stream
CapturedBarcodes: JSON data sent successfully
CapturedBarcodes: Getting response code from server
CapturedBarcodes: Server response code: [CODE]
CapturedBarcodes: Server response message: [MESSAGE]
CapturedBarcodes: Request success: [true/false] (code in 200-299 range)
```

### **4. configureSslForHttps() Method**
```
CapturedBarcodes: configureSslForHttps() called
CapturedBarcodes: Creating trust-all SSL context for self-signed certificates
CapturedBarcodes: Installing SSL context and hostname verifier
CapturedBarcodes: SSL configuration completed successfully
```

### **5. addAuthenticationHeaders() Method**
```
CapturedBarcodes: addAuthenticationHeaders() called
CapturedBarcodes: Authentication enabled: [true/false]
CapturedBarcodes: Loading credentials from keystore
CapturedBarcodes: Username loaded: [empty/present]
CapturedBarcodes: Password loaded: [empty/present]
CapturedBarcodes: Adding Basic Authentication header
CapturedBarcodes: Authorization header added successfully
```

### **6. Error Handling**
```
CapturedBarcodes: Exception during upload: [EXCEPTION_MESSAGE]
CapturedBarcodes: IOException during HTTP request: [IO_EXCEPTION]
CapturedBarcodes: Failed to configure SSL: [SSL_EXCEPTION]
```

## 📱 **How to View Logs**

### **Method 1: Android Studio Logcat**
1. Connect your device or use emulator
2. Open **Android Studio**
3. Go to **View** → **Tool Windows** → **Logcat**
4. Filter by tag: `CapturedBarcodes`
5. Perform the upload in your app
6. Watch the logs in real-time

### **Method 2: Command Line ADB**
```bash
# View all logs from the app
adb logcat -s CapturedBarcodes

# View logs with timestamps
adb logcat -v time -s CapturedBarcodes

# Save logs to file
adb logcat -s CapturedBarcodes > upload_debug.log
```

### **Method 3: Device Log Apps**
- Install a log viewer app on your device
- Filter by "CapturedBarcodes" tag
- Perform upload and review logs

## 🔍 **What to Look For**

### **Common Issues to Investigate:**

#### **1. Network Connection Issues**
Look for:
```
CapturedBarcodes: IOException during HTTP request: [DETAILS]
CapturedBarcodes: Server response code: [NON-200-CODE]
```

#### **2. SSL/Certificate Issues**
Look for:
```
CapturedBarcodes: Failed to configure SSL: [SSL_ERROR]
CapturedBarcodes: Server response code: [SSL_ERROR_CODE]
```

#### **3. Authentication Problems**
Look for:
```
CapturedBarcodes: Authentication enabled but credentials are missing
CapturedBarcodes: Server response code: 401
```

#### **4. Endpoint Configuration Issues**
Look for:
```
CapturedBarcodes: Upload aborted - invalid mode or endpoint
CapturedBarcodes: endpointUri: null
CapturedBarcodes: isHttpsPostMode: false
```

#### **5. JSON Conversion Problems**
Look for:
```
CapturedBarcodes: barcodeValuesMap is null
CapturedBarcodes: Successfully processed 0 barcodes for JSON
```

#### **6. Server Response Issues**
Look for:
```
CapturedBarcodes: Server response code: 404
CapturedBarcodes: Server response code: 500
CapturedBarcodes: Request success: false
```

## 🐛 **Debug Process**

### **Step 1: Reproduce the Issue**
1. Build and install the updated app
2. Configure HTTP(s) Post mode with your endpoint
3. Capture some barcodes
4. Tap the Upload button
5. Note the error toast message

### **Step 2: Collect Logs**
```bash
# Start log collection
adb logcat -v time -s CapturedBarcodes > upload_debug.log

# In another terminal, clear existing logs
adb logcat -c

# Perform the upload in the app
# Watch the logs in real-time or check the file
```

### **Step 3: Analyze the Log Flow**
1. **Check if upload starts**: Look for `uploadData() called`
2. **Verify configuration**: Check `endpointUri` and `isHttpsPostMode`
3. **Check JSON conversion**: Look for `JSON conversion completed successfully`
4. **Verify network request**: Check `HTTP connection opened`
5. **Check server response**: Look at `Server response code`
6. **Identify the failure point**: Find where the process stops or errors

### **Step 4: Common Solutions**

#### **Network Issues:**
- Check if WMS server is running: `docker-compose ps`
- Verify endpoint URL in app settings
- Test endpoint manually: `curl -X POST [URL]`

#### **SSL Issues:**
- Use HTTP instead of HTTPS for testing
- Check certificate configuration

#### **Server Issues:**
- Check WMS logs: `docker-compose logs -f web`
- Verify API endpoint is accessible

## 📊 **Example Debug Session**

Here's what a successful upload should look like in the logs:

```
2024-01-15 10:30:00.123 D/CapturedBarcodes: uploadData() called
2024-01-15 10:30:00.124 D/CapturedBarcodes: isHttpsPostMode: true
2024-01-15 10:30:00.125 D/CapturedBarcodes: endpointUri: http://192.168.1.188:8080/api/barcodes.php
2024-01-15 10:30:00.126 D/CapturedBarcodes: SessionData created with 3 barcodes
2024-01-15 10:30:00.130 D/CapturedBarcodes: convertSessionDataToJson() called
2024-01-15 10:30:00.135 D/CapturedBarcodes: JSON conversion completed successfully
2024-01-15 10:30:00.140 D/CapturedBarcodes: performHttpPost() called
2024-01-15 10:30:00.145 D/CapturedBarcodes: Using HTTP connection (no SSL needed)
2024-01-15 10:30:00.200 D/CapturedBarcodes: JSON data sent successfully
2024-01-15 10:30:00.250 D/CapturedBarcodes: Server response code: 200
2024-01-15 10:30:00.251 D/CapturedBarcodes: Request success: true (code in 200-299 range)
2024-01-15 10:30:00.255 I/CapturedBarcodes: Upload successful, showing success toast
```

## 🎯 **Next Steps**

1. **Install the updated app** with logging
2. **Reproduce the upload error**
3. **Collect the logs** using one of the methods above
4. **Share the logs** to identify the exact failure point
5. **Apply the appropriate fix** based on the logged error

The comprehensive logging will help pinpoint exactly where the upload process is failing and what error is causing the toast message.
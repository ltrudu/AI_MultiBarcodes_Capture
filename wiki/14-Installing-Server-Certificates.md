# Installing Server Certificates on Android and Web Client

This guide explains how to install SSL/TLS certificates for secure HTTPS connections with the AI MultiBarcode Capture web management system on both Windows and Android devices.

## 📋 Table of Contents

- [Overview](#overview)
- [Method 1: Download from Web Interface](#method-1-download-from-web-interface)
- [Method 2: Install from SSL Folder](#method-2-install-from-ssl-folder)
- [Windows Installation Process](#windows-installation-process)
- [Android Installation Process](#android-installation-process)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

To eliminate browser security warnings and establish trusted HTTPS connections, you need to install the CA (Certificate Authority) certificate on your devices. The AI MultiBarcode Capture system generates self-signed certificates that need to be manually trusted.

### What You'll Achieve:
- **🔒 Secure HTTPS connections** without browser warnings
- **✅ Trusted certificate validation** on all applications
- **🌐 System-wide certificate trust** for seamless connectivity

## 💻 Method 1: Download from Web Interface

### Step 1: Access the Web Management System
1. Open your web browser
2. Navigate to your WMS instance:
   - **HTTP**: `http://localhost:3500` or `http://YOUR-SERVER-IP:3500`
   - **HTTPS**: `https://localhost:3543` or `https://YOUR-SERVER-IP:3543` (may show security warning initially)

### Step 2: Open Settings
1. Click the **⚙️ Settings** button in the top navigation bar
2. The Settings modal will open

### Step 3: Access SSL Certificates Section
1. Scroll down to find the **🔐 SSL Certificates** section
2. You'll see options for:
   - **🪟 Windows Certificate (wms_ca.crt)**
   - **📱 Android Certificate (android_ca_system.pem)**
   - **📖 Installation Instructions**

### Step 4: Download Certificates
#### For Windows:
- Click **"📥 Download Windows Certificate"**
- File will be saved as `wms-ca-certificate.crt`

#### For Android:
- Click **"📥 Download Android Certificate"**
- File will be saved as `android-ca-certificate.pem`

#### View Instructions:
- Click **"📖 View Installation Guide"** for quick reference

## 📁 Method 2: Install from SSL Folder

### Step 1: Access SSL Folder
Navigate to the SSL folder in your AI MultiBarcode Capture installation:
```
AI_MultiBarcode_Capture/WebInterface/ssl/
```

### Step 2: Locate Certificate Files
You'll find these certificate files:
- **`wms_ca.crt`** - Root CA certificate for Windows/browsers
- **`android_ca_system.pem`** - CA certificate for Android
- **`wms_chain.crt`** - Full certificate chain (alternative)

### Step 3: Copy Files
Copy the appropriate certificate file to your target device:
- **For Windows**: Use `wms_ca.crt`
- **For Android**: Use `android_ca_system.pem`

## 🪟 Windows Installation Process

### Method A: Double-Click Installation (Recommended)

#### Step 1: Download/Locate Certificate
- Use either download method above to get `wms_ca.crt`

#### Step 2: Install Certificate
1. **Right-click** on the `wms_ca.crt` file
2. Select **"Install Certificate..."**
3. Choose installation scope:
   - **"Local Machine"** (requires administrator rights, affects all users)
   - **"Current User"** (affects only your user account)
4. Click **"Next"**

#### Step 3: Choose Certificate Store
1. Select **"Place all certificates in the following store"**
2. Click **"Browse..."**
3. Select **"Trusted Root Certification Authorities"** ⚠️ **IMPORTANT**
4. Click **"OK"** → **"Next"** → **"Finish"**

#### Step 4: Security Warning
1. Windows will display a security warning
2. Click **"Yes"** to confirm the installation
3. You should see "The import was successful"

### Method B: Certificate Manager (Advanced)

#### Step 1: Open Certificate Manager
- Press `Win + R`, type `certmgr.msc`, press Enter
- **OR** search for "Manage computer certificates" (requires admin)

#### Step 2: Import Certificate
1. Navigate to: **Trusted Root Certification Authorities** → **Certificates**
2. Right-click in the certificates list → **All Tasks** → **Import...**
3. Follow the Certificate Import Wizard
4. Browse to your `wms_ca.crt` file
5. Complete the import process

### Method C: Command Line (PowerShell)

#### For Administrators:
```powershell
# Run PowerShell as Administrator
Import-Certificate -FilePath "path\to\wms_ca.crt" -CertStoreLocation Cert:\LocalMachine\Root
```

#### For Current User:
```powershell
Import-Certificate -FilePath "path\to\wms_ca.crt" -CertStoreLocation Cert:\CurrentUser\Root
```

## 📱 Android Installation Process

### Step 1: Transfer Certificate to Android Device

#### Option A: Direct Download
1. Open your Android browser
2. Navigate to your WMS: `http://YOUR-SERVER-IP:3500`
3. Go to Settings → SSL Certificates
4. Download the Android certificate

#### Option B: File Transfer
1. Use the SSL folder method to get `android_ca_system.pem`
2. Transfer via:
   - **USB cable** and file manager
   - **Email** (send to yourself)
   - **Cloud storage** (Google Drive, Dropbox, etc.)
   - **ADB**: `adb push android_ca_system.pem /sdcard/`

### Step 2: Install Certificate on Android

#### Method A: Through Settings (Recommended)
1. Go to **Settings** → **Security** (or **Biometrics and security**)
2. Find **"Encryption & credentials"** or **"Credential storage"**
3. Tap **"Install from storage"** or **"Install certificates"**
4. Navigate to the transferred certificate file
5. Select the certificate file
6. When prompted for certificate type, choose **"CA certificate"**
7. Provide a name for the certificate (e.g., "WMS CA" or "AI MultiBarcode CA")
8. Confirm the installation

#### Method B: File Manager (Alternative)
1. Open your file manager app
2. Navigate to the certificate file location
3. Tap on the certificate file
4. Follow the installation prompts
5. Choose **"CA certificate"** when asked

### Step 3: Android Security Warning
- Android will show a warning about network monitoring
- This is normal for custom CA certificates
- Tap **"OK"** or **"Install anyway"** to proceed

## ✅ Verification

### Windows Verification

#### Check Certificate Installation:
1. Open Certificate Manager (`certmgr.msc`)
2. Navigate to: **Trusted Root Certification Authorities** → **Certificates**
3. Look for **"WMSRootCA"** in the list
4. Double-click to view details:
   - **Issued to**: WMSRootCA
   - **Issued by**: WMSRootCA
   - **Valid from**: Certificate creation date
   - **Valid to**: ~10 years from creation

#### Test Browser Connection:
1. Open your web browser
2. Navigate to: `https://localhost:3543` or `https://YOUR-SERVER-IP:3543`
3. **Expected result**: 🔒 Secure connection with no warnings
4. **Certificate details**: Should show valid certificate issued by WMSRootCA

### Android Verification

#### Check Certificate Installation:
1. Go to **Settings** → **Security** → **Encryption & credentials**
2. Tap **"Trusted credentials"**
3. Switch to the **"User"** tab
4. Look for your installed certificate (e.g., "WMS CA")
5. Tap to view certificate details

#### Test Android App Connection:
1. Open the AI MultiBarcode Capture Android app
2. Configure the HTTPS endpoint: `https://YOUR-SERVER-IP:3543/api/barcodes.php`
3. Test the connection
4. **Expected result**: Successful connection without SSL errors

## 🚨 Troubleshooting

### Common Windows Issues

#### Certificate Not Showing in Browser
- **Solution**: Restart your browser after installing the certificate
- **Alternative**: Clear browser cache and reload the page

#### "Certificate not trusted" Error
- **Cause**: Certificate installed in wrong store
- **Solution**: Ensure certificate is in "Trusted Root Certification Authorities", not "Personal" or other stores

#### Permission Denied During Installation
- **Cause**: Insufficient privileges
- **Solution**: Run as administrator or install for "Current User" only

### Common Android Issues

#### Certificate Installation Fails
- **Cause**: Android version restrictions or file format issues
- **Solution**: Ensure the file has `.pem` extension and try renaming to `.crt`

#### App Still Shows SSL Errors
- **Cause**: App may be using certificate pinning or not respecting system certificates
- **Solution**: Check app-specific SSL settings or use HTTP endpoint temporarily

#### Certificate Not Found in Trusted Credentials
- **Cause**: Installation failed or certificate installed in wrong category
- **Solution**: Reinstall and ensure you select "CA certificate" during installation

### Network-Related Issues

#### Can't Access HTTPS URL
- **Cause**: HTTPS port (3543) not accessible
- **Solution**:
  - Check firewall settings
  - Verify Docker port mapping
  - Use HTTP (port 3500) temporarily

#### Certificate Warnings Despite Installation
- **Cause**: Accessing by different hostname/IP than certificate was issued for
- **Solution**:
  - Access using the exact hostname in the certificate
  - Regenerate certificates with correct hostname/IP
  - Add hostname to system hosts file

## 🔄 Certificate Renewal

### When to Renew
- **Server certificates**: Valid for 1 year, renew annually
- **CA certificates**: Valid for 10 years
- **Automatic renewal**: Run certificate generation scripts

### Renewal Process
1. Run `create-certificates.bat` (Windows) or `./create-certificates.sh` (Linux/Mac)
2. Update the web server with new certificates
3. Reinstall CA certificate on client devices if CA was renewed

## 🛡️ Security Considerations

### Important Security Notes
- **Self-signed certificates** are not inherently trusted by devices
- **Only install on devices you control** - don't distribute CA certificates widely
- **Monitor certificate expiration** and renew before expiry
- **Use proper certificates from trusted CAs** for production/public environments

### Best Practices
- **Regular renewal**: Set reminders for certificate renewal
- **Secure storage**: Keep private keys secure and backed up
- **Access control**: Limit who can generate and install certificates
- **Documentation**: Keep records of which devices have certificates installed

---

**Next Steps**: After installing certificates, configure your Android AI MultiBarcode Capture app to use the HTTPS endpoint for secure communication. See the [HTTP(s) Integration Guide](09-HTTP-Integration.md) for complete setup instructions.
# Generating HTTPS Certificates

This guide explains how to generate SSL/TLS certificates for secure HTTPS communication between the web management system and Android devices using the automated certificate generation scripts.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Software Installation](#software-installation)
- [Configuration](#configuration)
- [Certificate Generation](#certificate-generation)
- [Integration](#integration)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

The certificate generation system creates a complete Public Key Infrastructure (PKI) for secure HTTPS communication:

- **Certificate Authority (CA)** - Self-signed root certificate for issuing other certificates
- **WMS Server Certificate** - SSL certificate for the Apache web server with Subject Alternative Names (SAN)
- **Android System Certificate** - CA certificate formatted for Android system-wide installation

## 📦 Prerequisites

Before generating certificates, ensure you have the following software installed:

### Required Software

1. **OpenSSL** - For certificate generation and management

**Note**: Java JDK is no longer required as the updated scripts generate Android system certificates using OpenSSL only.

### System Requirements

- **Windows**: Windows 10 or later
- **Linux**: Ubuntu 18.04+, CentOS 7+, or equivalent
- **macOS**: macOS 10.14 or later

## 🔧 Software Installation

### Windows Installation

#### OpenSSL Installation
1. Download OpenSSL from [https://slproweb.com/products/Win32OpenSSL.html](https://slproweb.com/products/Win32OpenSSL.html)
2. Choose "Win64 OpenSSL v3.x.x" (latest version)
3. Run the installer with administrator privileges
4. Add OpenSSL to your system PATH:
   - Open System Properties → Environment Variables
   - Add `C:\Program Files\OpenSSL-Win64\bin` to your PATH variable
5. Verify installation: Open Command Prompt and run `openssl version`

#### Java JDK Installation (No Longer Required)
Java JDK installation is no longer necessary for certificate generation. The updated scripts create Android system certificates using OpenSSL only.

### Linux Installation

#### Ubuntu/Debian
```bash
# Install OpenSSL
sudo apt-get update
sudo apt-get install openssl

# Verify installation
openssl version
```

#### CentOS/RHEL
```bash
# Install OpenSSL
sudo yum install openssl

# Verify installation
openssl version
```

### macOS Installation

#### Using Homebrew
```bash
# Install OpenSSL
brew install openssl

# Verify installation
openssl version
```

## ⚙️ Configuration

### Certificate Configuration File

The `certificates.conf` file contains all parameters needed for certificate generation. This file is located in the `WebInterface` directory.

#### Key Configuration Sections

1. **Certificate Authority (CA) Configuration**
```ini
CA_COUNTRY="US"
CA_STATE="New York"
CA_CITY="New York"
CA_ORGANIZATION="WMS Root CA"
CA_ORGANIZATIONAL_UNIT="Certificate Authority"
CA_COMMON_NAME="WMS Root CA"
CA_EMAIL="admin@wms.local"
CA_PASSWORD="wms_ca_password_2024"
```

2. **Web Server Certificate Configuration**
```ini
WMS_COUNTRY="US"
WMS_STATE="New York"
WMS_CITY="New York"
WMS_ORGANIZATION="WMS Organization"
WMS_ORGANIZATIONAL_UNIT="IT Department"
WMS_COMMON_NAME="wms.local"
WMS_EMAIL="webmaster@wms.local"
WMS_PASSWORD="wms_server_password_2024"
```

3. **Android Certificate Configuration** (for system installation)
```ini
# Android system certificates use the CA certificate
# No separate client certificate configuration needed
```

4. **Network Configuration**
```ini
# Subject Alternative Names for WMS certificate
WMS_SAN_DNS1="localhost"
WMS_SAN_DNS2="wms.local"
WMS_SAN_DNS3="*.wms.local"
WMS_SAN_IP1="127.0.0.1"
WMS_SAN_IP2="192.168.1.188"  # Update with your server IP
WMS_SAN_IP3="::1"
```

### Customizing Configuration

Before generating certificates, customize the `certificates.conf` file:

1. **Update IP Addresses**: Change `WMS_SAN_IP2` to your server's actual IP address
2. **Modify Organization Details**: Update company/organization information as needed
3. **Change Passwords**: Use strong, unique passwords for production environments
4. **Adjust Validity Periods**: Modify certificate validity periods if needed

#### Important Security Notes
- **Passwords are stored in plain text** - Secure the `certificates.conf` file appropriately
- **Use strong passwords** for production environments
- **Update IP addresses** to match your network configuration
- **Backup certificates** after generation

## 🚀 Certificate Generation

### Step 1: Navigate to WebInterface Directory
```bash
cd WebInterface
```

### Step 2: Verify Configuration
Review and customize the `certificates.conf` file as needed.

### Step 3: Run Certificate Generation Script

#### Windows
```cmd
create-certificates.bat
```

#### Linux/macOS
```bash
./create-certificates.sh
```

### Step 4: Certificate Generation Process

The script will automatically:

1. **Verify Prerequisites** - Check that OpenSSL is available (keytool no longer required)
2. **Create SSL Directory** - Create `ssl` folder if it doesn't exist
3. **Clean Existing Certificates** - Remove any existing certificates
4. **Generate Certificate Authority** - Create self-signed CA certificate
5. **Generate Server Certificate** - Create WMS server certificate with SAN extensions
6. **Generate Android System Certificate** - Create CA certificate in Android system formats
7. **Create Certificate Chains** - Generate full certificate chains for validation

### Generated Files

After successful execution, the following files will be created in the `ssl` directory:

#### Certificate Authority
- `wms_ca.key` - CA private key
- `wms_ca.crt` - CA certificate

#### Web Server Certificates
- `wms.key` - Server private key (passphrase removed for Apache)
- `wms.csr` - Server certificate signing request
- `wms.crt` - Server certificate
- `wms_chain.crt` - Server certificate with CA chain

#### Android System Certificates
- `android_ca_system.pem` - CA certificate in PEM format for manual installation
- `[hash].0` - CA certificate with OpenSSL hash-based filename for direct system installation

## 🔧 Integration

### Apache Web Server Configuration

1. **Update Apache SSL Configuration**

   Edit your Apache SSL configuration file (typically `ssl-default.conf`):
   ```apache
   SSLCertificateFile /etc/ssl/certs/wms.crt
   SSLCertificateKeyFile /etc/ssl/private/wms.key
   ```

2. **Copy Certificates to Apache Directories**
   ```bash
   # Copy server certificate
   sudo cp ssl/wms.crt /etc/ssl/certs/
   sudo cp ssl/wms.key /etc/ssl/private/

   # Set appropriate permissions
   sudo chmod 644 /etc/ssl/certs/wms.crt
   sudo chmod 600 /etc/ssl/private/wms.key
   ```

3. **Restart Apache**
   ```bash
   sudo systemctl restart apache2
   ```

### Android System Certificate Installation

#### Method 1: Manual Installation (Recommended)

1. **Transfer Certificate to Device**
   - Copy `android_ca_system.pem` to your Android device (via USB, email, cloud storage, etc.)

2. **Install Certificate through Settings**
   - Go to **Settings** > **Security** > **Encryption & credentials** > **Install from storage**
   - Navigate to the transferred `android_ca_system.pem` file
   - Select **CA certificate** when prompted for certificate type
   - Provide a name for the certificate (e.g., "WMS CA")
   - Confirm installation

3. **Verification**
   - Go to **Settings** > **Security** > **Encryption & credentials** > **Trusted credentials** > **User** tab
   - Verify your "WMS CA" certificate appears in the list

#### Benefits of System-Wide Installation

- **No application changes required** - All apps automatically trust your CA
- **Standard Android certificate validation** - Uses built-in certificate checking
- **Improved security** - No embedded certificates in application code
- **Universal compatibility** - Works with all HTTPS-enabled applications

### Docker Configuration

If using Docker, mount the certificates into your container:

```yaml
version: '3.8'
services:
  wms:
    image: your-wms-image
    volumes:
      - ./ssl/wms.crt:/etc/ssl/certs/wms.crt:ro
      - ./ssl/wms.key:/etc/ssl/private/wms.key:ro
    ports:
      - "443:443"
```

## 🔍 Verification

### Verify Certificate Chain
```bash
# Verify server certificate
openssl x509 -in ssl/wms.crt -text -noout

# Verify certificate chain
openssl verify -CAfile ssl/wms_ca.crt ssl/wms.crt

# Check certificate fingerprint
openssl x509 -noout -fingerprint -sha256 -in ssl/wms.crt
```

### Test HTTPS Connection
```bash
# Test HTTPS connection
curl -v --cacert ssl/wms_ca.crt https://wms.local/

# Test with client certificate
curl -v --cacert ssl/wms_ca.crt \
     --cert ssl/android_client.crt \
     --key ssl/android_client.key \
     https://wms.local/
```

## 🚨 Troubleshooting

### Common Issues

#### "OpenSSL not found" Error
- **Windows**: Ensure OpenSSL is installed and added to PATH
- **Linux**: Install OpenSSL package (`sudo apt-get install openssl`)
- **macOS**: Install via Homebrew (`brew install openssl`)

#### Java JDK No Longer Required
- Previous versions required Java keytool for Android keystores
- Current version generates system certificates using OpenSSL only
- Java JDK installation is no longer necessary for certificate generation

#### Certificate Generation Fails
- Check file permissions in the `ssl` directory
- Verify all configuration values in `certificates.conf`
- Ensure no antivirus software is blocking file creation

#### Browser Certificate Warnings
- Import the CA certificate (`wms_ca.crt`) into your browser's trusted root certificates
- Add certificate exception for the server domain

#### Android Certificate Issues
- Verify the CA certificate was installed in Android's system certificate store
- Check that the certificate appears in Settings > Security > Trusted credentials > User tab
- Ensure the server certificate was issued by the installed CA
- For network security config issues, verify the CA is properly trusted system-wide

### Log Analysis

Check the script output for detailed error messages:
- Certificate generation steps are logged
- OpenSSL errors provide specific failure reasons
- File permissions and paths are validated

### Re-generating Certificates

To regenerate certificates:
1. Delete all files in the `ssl` directory
2. Update `certificates.conf` if needed
3. Run the certificate generation script again

### Production Considerations

For production environments:
- Use strong, unique passwords
- Store private keys securely
- Implement certificate rotation procedures
- Monitor certificate expiration dates
- Consider using a proper Certificate Authority for public-facing services

## 📚 Additional Resources

- [OpenSSL Documentation](https://www.openssl.org/docs/)
- [Apache SSL Configuration](https://httpd.apache.org/docs/2.4/ssl/)
- [Android Security Best Practices](https://developer.android.com/training/articles/security-ssl)
- [Certificate Management Best Practices](https://tools.ietf.org/html/rfc5280)

---

**Next Steps**: After generating certificates, configure your web server and Android application to use HTTPS communication. See the [HTTP(s) Integration Guide](09-HTTP-Integration.md) for complete setup instructions.
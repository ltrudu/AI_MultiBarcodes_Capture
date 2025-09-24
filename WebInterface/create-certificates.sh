#!/bin/bash

echo "======================================"
echo "WMS SSL Certificate Generation Script"
echo "======================================"

# Check if OpenSSL is available
if ! command -v openssl &> /dev/null; then
    echo "ERROR: OpenSSL is not installed or not in PATH"
    echo "Please install OpenSSL:"
    echo "  Ubuntu/Debian: sudo apt-get install openssl"
    echo "  CentOS/RHEL: sudo yum install openssl"
    echo "  macOS: brew install openssl"
    exit 1
fi

# Check if Java keytool is available (for Android certificates)
if ! command -v keytool &> /dev/null; then
    echo "ERROR: Java keytool is not installed or not in PATH"
    echo "Please install Java JDK:"
    echo "  Ubuntu/Debian: sudo apt-get install openjdk-11-jdk"
    echo "  CentOS/RHEL: sudo yum install java-11-openjdk-devel"
    echo "  macOS: brew install openjdk@11"
    exit 1
fi

# Load configuration from certificates.conf
if [ ! -f "certificates.conf" ]; then
    echo "ERROR: certificates.conf file not found"
    echo "Please ensure certificates.conf exists in the same directory"
    exit 1
fi

echo "Loading configuration from certificates.conf..."
# Source the configuration file (skip comments and empty lines)
source <(grep -v '^#' certificates.conf | grep -v '^$')

echo "Configuration loaded successfully"

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create SSL directory if it doesn't exist (relative to script directory)
SSL_PATH="$SCRIPT_DIR/$SSL_DIR"
if [ ! -d "$SSL_PATH" ]; then
    echo "Creating SSL directory: $SSL_PATH"
    mkdir -p "$SSL_PATH"
fi

# Change to SSL directory
cd "$SSL_PATH"
echo "Working in directory: $(pwd)"

# Remove existing certificates if they exist
echo "Cleaning up existing certificates..."
rm -f "$CA_KEY_FILE" "$CA_CERT_FILE" \
      "$WMS_KEY_FILE" "$WMS_CSR_FILE" "$WMS_CERT_FILE" "wms.conf" \
      "android_ca_system.pem" "*.0" "wms_chain.crt" \
      "*.srl"

echo "======================================"
echo "Step 1: Creating Certificate Authority (CA)"
echo "======================================"

# Create CA private key
echo "Creating CA private key..."
openssl genrsa -aes256 -passout pass:$CA_PASSWORD -out "$CA_KEY_FILE" $KEY_SIZE
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create CA private key"
    exit 1
fi

# Create CA certificate configuration
cat > ca_temp.conf << EOF
[req]
distinguished_name = req_distinguished_name
[v3_ca]
basicConstraints = $CA_BASIC_CONSTRAINTS
keyUsage = critical,keyCertSign,cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
EOF

# Create CA certificate
echo "Creating CA certificate..."
MSYS_NO_PATHCONV=1 openssl req -new -x509 -days $CA_VALIDITY_DAYS \
    -key "$CA_KEY_FILE" -passin pass:$CA_PASSWORD \
    -out "$CA_CERT_FILE" \
    -subj "/C=$CA_COUNTRY/ST=$CA_STATE/L=$CA_CITY/O=$CA_ORGANIZATION/OU=$CA_ORGANIZATIONAL_UNIT/CN=$CA_COMMON_NAME/emailAddress=$CA_EMAIL" \
    -extensions v3_ca -config ca_temp.conf

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create CA certificate"
    exit 1
fi

# Clean up temporary file
rm -f ca_temp.conf

echo "======================================"
echo "Step 2: Creating WMS Server Certificate"
echo "======================================"

# Create WMS server private key
echo "Creating WMS server private key..."
openssl genrsa -aes256 -passout pass:$WMS_PASSWORD -out "$WMS_KEY_FILE" $KEY_SIZE
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create WMS server private key"
    exit 1
fi

# Create WMS server configuration file
echo "Creating WMS server configuration..."
cat > wms.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = $WMS_COUNTRY
ST = $WMS_STATE
L = $WMS_CITY
O = $WMS_ORGANIZATION
OU = $WMS_ORGANIZATIONAL_UNIT
CN = $WMS_COMMON_NAME
emailAddress = $WMS_EMAIL

[v3_req]
keyUsage = $SERVER_KEY_USAGE
extendedKeyUsage = $SERVER_EXT_KEY_USAGE
subjectAltName = @alt_names

[alt_names]
DNS.1 = $WMS_SAN_DNS1
DNS.2 = $WMS_SAN_DNS2
DNS.3 = $WMS_SAN_DNS3
IP.1 = $WMS_SAN_IP1
IP.2 = $WMS_SAN_IP2
IP.3 = $WMS_SAN_IP3
EOF

# Create WMS server certificate signing request
echo "Creating WMS server CSR..."
MSYS_NO_PATHCONV=1 openssl req -new -key "$WMS_KEY_FILE" -passin pass:$WMS_PASSWORD \
    -out "$WMS_CSR_FILE" -config wms.conf
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create WMS server CSR"
    exit 1
fi

# Sign WMS server certificate with CA
echo "Signing WMS server certificate..."
openssl x509 -req -in "$WMS_CSR_FILE" \
    -CA "$CA_CERT_FILE" -CAkey "$CA_KEY_FILE" -passin pass:$CA_PASSWORD \
    -CAcreateserial -out "$WMS_CERT_FILE" -days $SERVER_VALIDITY_DAYS \
    -extensions v3_req -extfile wms.conf
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to sign WMS server certificate"
    exit 1
fi

# Remove passphrase from WMS private key for Apache
echo "Removing passphrase from WMS private key..."
openssl rsa -in "$WMS_KEY_FILE" -passin pass:$WMS_PASSWORD -out "${WMS_KEY_FILE}.unencrypted"
mv "${WMS_KEY_FILE}.unencrypted" "$WMS_KEY_FILE"

echo "======================================"
echo "Step 3: Creating Android System Certificate"
echo "======================================"

# Create Android system certificate (PEM format CA certificate)
echo "Creating Android system certificate..."
echo "Converting CA certificate to PEM format for Android system installation..."

# Copy CA certificate to Android-specific name
cp "$CA_CERT_FILE" android_ca_system.pem
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create Android system certificate"
    exit 1
fi

# Create certificate with hash-based filename for Android system installation
echo "Creating certificate with Android system naming convention..."
CERT_HASH=$(openssl x509 -noout -hash -in "$CA_CERT_FILE")
cp "$CA_CERT_FILE" "${CERT_HASH}.0"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to create hashed certificate file"
    exit 1
fi

echo "Android system certificate created: ${CERT_HASH}.0"

echo "======================================"
echo "Step 4: Creating Certificate Chain Files"
echo "======================================"

# Create full certificate chain file (server cert + CA cert)
echo "Creating certificate chain for WMS..."
cat "$WMS_CERT_FILE" "$CA_CERT_FILE" > wms_chain.crt

# Set appropriate permissions
echo "Setting file permissions..."
chmod 600 *.key
chmod 644 *.crt *.csr *.conf *.pem *.0

echo "======================================"
echo "Certificate Generation Complete!"
echo "======================================"
echo ""
echo "Generated files:"
echo "- $CA_CERT_FILE (Certificate Authority certificate)"
echo "- $WMS_CERT_FILE (WMS server certificate)"
echo "- $WMS_KEY_FILE (WMS server private key)"
echo "- android_ca_system.pem (Android system CA certificate in PEM format)"
echo "- ${CERT_HASH}.0 (Android system CA certificate with hash filename)"
echo "- wms_chain.crt (WMS certificate chain)"
echo ""
echo "For Android system-wide certificate installation:"
echo "1. Transfer android_ca_system.pem to your Android device"
echo "2. Go to Settings > Security > Encryption & Credentials > Install from storage"
echo "3. Select android_ca_system.pem and install as \"CA certificate\""
echo "4. Or use the hashed filename ${CERT_HASH}.0 for direct system installation (root required)"
echo ""
echo "For Apache configuration:"
echo "1. Update SSL certificate paths in your Apache config"
echo "2. Use $WMS_CERT_FILE as SSLCertificateFile"
echo "3. Use $WMS_KEY_FILE as SSLCertificateKeyFile"
echo ""
echo "Certificate fingerprints:"
echo "CA Certificate:"
openssl x509 -noout -fingerprint -sha256 -in "$CA_CERT_FILE"
echo "WMS Server Certificate:"
openssl x509 -noout -fingerprint -sha256 -in "$WMS_CERT_FILE"
echo "Android Client Certificate:"
openssl x509 -noout -fingerprint -sha256 -in "$ANDROID_CERT_FILE"
echo ""
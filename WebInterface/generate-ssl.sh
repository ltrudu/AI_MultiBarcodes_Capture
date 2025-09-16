#!/bin/bash

# Generate SSL certificates for WMS
echo "🔐 Generating SSL certificates for WMS..."

# Create SSL directory
mkdir -p ssl

# Generate private key
openssl genrsa -out ssl/wms.key 2048

# Generate certificate signing request
openssl req -new -key ssl/wms.key -out ssl/wms.csr -subj "/C=US/ST=State/L=City/O=Organization/OU=IT Department/CN=wms.local/emailAddress=admin@wms.local"

# Generate self-signed certificate (valid for 365 days)
openssl x509 -req -days 365 -in ssl/wms.csr -signkey ssl/wms.key -out ssl/wms.crt

# Create certificate with Subject Alternative Names for IP and localhost
cat > ssl/wms.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = US
ST = State
L = City
O = WMS Organization
OU = IT Department
CN = wms.local

[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = wms.local
DNS.3 = *.wms.local
IP.1 = 127.0.0.1
IP.2 = 192.168.1.188
IP.3 = ::1
EOF

# Generate new certificate with SAN
openssl req -new -key ssl/wms.key -out ssl/wms.csr -config ssl/wms.conf
openssl x509 -req -days 365 -in ssl/wms.csr -signkey ssl/wms.key -out ssl/wms.crt -extensions v3_req -extfile ssl/wms.conf

# Set proper permissions
chmod 600 ssl/wms.key
chmod 644 ssl/wms.crt

echo "✅ SSL certificates generated successfully!"
echo "📄 Certificate: ssl/wms.crt"
echo "🔑 Private Key: ssl/wms.key"
echo ""
echo "⚠️  Note: This is a self-signed certificate."
echo "   Your Android app may need to accept self-signed certificates."
echo ""
echo "🔍 Certificate details:"
openssl x509 -in ssl/wms.crt -text -noout | grep -A 5 "Subject Alternative Name"
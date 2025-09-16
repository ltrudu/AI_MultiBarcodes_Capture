# Installation Guide - AI MultiBarcode Capture

This comprehensive guide covers the complete installation process for all components of the AI MultiBarcode Capture system.

## 📋 Prerequisites Verification

Before beginning installation, verify all requirements from the [System Requirements](02-System-Requirements.md) guide are met.

### Quick Verification Commands
```bash
# Verify Docker installation
docker --version
docker-compose --version

# Check available disk space
df -h  # Linux/macOS
dir C:\ # Windows

# Verify network connectivity
ping 8.8.8.8

# Check available ports
netstat -an | grep :8080  # Should return nothing if port is free
```

## 🚀 Installation Methods

Choose the installation method that best fits your environment:

1. **[Quick Installation](#quick-installation)** - For development and testing
2. **[Production Installation](#production-installation)** - For enterprise deployment
3. **[Developer Installation](#developer-installation)** - For development and customization

---

## 🎯 Quick Installation

Perfect for demonstrations and development environments.

### Step 1: Clone Repository
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture
```

### Step 2: Start Docker Environment
```bash
cd WebInterface
docker-compose up -d
```

### Step 3: Verify Installation
```bash
# Check all containers are running
docker-compose ps

# Expected output:
# NAME                    IMAGE               STATUS
# webinterface-db-1       mysql:8.0          Up
# webinterface-web-1      webinterface-web    Up
# webinterface-phpmyadmin-1 phpmyadmin:latest Up
```

### Step 4: Test Web Interface
Open browser and navigate to:
- **Main Interface**: http://localhost:8080
- **Database Admin**: http://localhost:8081

### Step 5: Build Android App
```bash
# Return to project root
cd ..

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install AI_MultiBarcodes_Capture/build/outputs/apk/debug/AI_MultiBarcodes_Capture-debug.apk
```

---

## 🏢 Production Installation

For enterprise environments with security and scalability requirements.

### Step 1: Prepare Production Environment

#### 1.1 Create Installation Directory
```bash
sudo mkdir -p /opt/ai-barcode-capture
sudo chown $USER:$USER /opt/ai-barcode-capture
cd /opt/ai-barcode-capture
```

#### 1.2 Clone and Configure
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git .
cp WebInterface/docker-compose.yml WebInterface/docker-compose.prod.yml
```

### Step 2: Production Configuration

#### 2.1 Edit Production Docker Compose
```yaml
# WebInterface/docker-compose.prod.yml
version: '3.8'

services:
  web:
    build: .
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./src:/var/www/html
      - ./ssl:/etc/ssl/certs/ai-barcode:ro
      - ./logs:/var/log/apache2
    environment:
      - ENVIRONMENT=production
      - SSL_ENABLED=true
    restart: unless-stopped
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: barcode_capture
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - db_data:/var/lib/mysql
      - ./backup:/backup
    restart: unless-stopped
    command: --default-authentication-plugin=mysql_native_password

  phpmyadmin:
    image: phpmyadmin:latest
    environment:
      PMA_HOST: db
      PMA_USER: ${MYSQL_USER}
      PMA_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "8081:80"
    restart: unless-stopped
    depends_on:
      - db

volumes:
  db_data:
    driver: local
```

#### 2.2 Create Environment Variables
```bash
# Create .env file
cat > WebInterface/.env << EOF
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_USER=barcode_user
MYSQL_PASSWORD=your_secure_password
EOF

# Secure the file
chmod 600 WebInterface/.env
```

### Step 3: SSL Configuration

#### 3.1 Generate SSL Certificates
```bash
# Create SSL directory
mkdir -p WebInterface/ssl

# Option 1: Self-signed certificate (development)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout WebInterface/ssl/private.key \
  -out WebInterface/ssl/certificate.crt \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=barcode-api.local"

# Option 2: Let's Encrypt (production)
# Follow Let's Encrypt documentation for your domain
```

#### 3.2 Configure Apache SSL
```apache
# WebInterface/ssl/ssl.conf
<VirtualHost *:443>
    ServerName barcode-api.company.com
    DocumentRoot /var/www/html

    SSLEngine on
    SSLCertificateFile /etc/ssl/certs/ai-barcode/certificate.crt
    SSLCertificateKeyFile /etc/ssl/certs/ai-barcode/private.key

    # Security headers
    Header always set Strict-Transport-Security "max-age=63072000; includeSubDomains; preload"
    Header always set X-Content-Type-Options nosniff
    Header always set X-Frame-Options DENY
    Header always set X-XSS-Protection "1; mode=block"
</VirtualHost>
```

### Step 4: Database Security

#### 4.1 Create Database Initialization Script
```sql
-- WebInterface/init/01-security.sql
-- Remove anonymous users
DELETE FROM mysql.user WHERE User='';

-- Remove remote root login
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');

-- Create application-specific user
CREATE USER IF NOT EXISTS 'barcode_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON barcode_capture.* TO 'barcode_user'@'%';

-- Flush privileges
FLUSH PRIVILEGES;
```

### Step 5: Production Deployment

#### 5.1 Start Production Environment
```bash
cd WebInterface
docker-compose -f docker-compose.prod.yml up -d
```

#### 5.2 Verify Production Deployment
```bash
# Check SSL certificate
openssl s_client -connect barcode-api.company.com:443 -servername barcode-api.company.com

# Test API endpoint
curl -k https://barcode-api.company.com/api/barcodes.php

# Monitor logs
docker-compose -f docker-compose.prod.yml logs -f
```

---

## 👨‍💻 Developer Installation

For developers who need to modify and extend the system.

### Step 1: Development Environment Setup

#### 1.1 Install Development Dependencies
```bash
# Android development
# Install Android Studio from https://developer.android.com/studio

# Web development dependencies
npm install -g live-server  # For frontend development
composer install            # For PHP dependencies (if applicable)
```

#### 1.2 Clone with Development Configuration
```bash
git clone https://github.com/your-repo/AI_MultiBarcode_Capture.git
cd AI_MultiBarcode_Capture

# Create development branch
git checkout -b development
```

### Step 2: Development Docker Configuration

#### 2.1 Development Docker Compose
```yaml
# WebInterface/docker-compose.dev.yml
version: '3.8'

services:
  web:
    build:
      context: .
      dockerfile: Dockerfile.dev
    ports:
      - "8080:80"
    volumes:
      - ./src:/var/www/html:rw
      - ./logs:/var/log/apache2:rw
    environment:
      - ENVIRONMENT=development
      - PHP_DISPLAY_ERRORS=On
      - PHP_ERROR_REPORTING=E_ALL
    restart: "no"
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: devpassword
      MYSQL_DATABASE: barcode_capture_dev
      MYSQL_USER: dev_user
      MYSQL_PASSWORD: devpassword
    ports:
      - "3306:3306"  # Expose MySQL port for external tools
    volumes:
      - db_dev_data:/var/lib/mysql
      - ./init:/docker-entrypoint-initdb.d:ro
    restart: "no"

  phpmyadmin:
    image: phpmyadmin:latest
    environment:
      PMA_HOST: db
      PMA_USER: root
      PMA_PASSWORD: devpassword
    ports:
      - "8081:80"
    restart: "no"

volumes:
  db_dev_data:
```

#### 2.2 Development Dockerfile
```dockerfile
# WebInterface/Dockerfile.dev
FROM php:8.1-apache

# Enable PHP development extensions
RUN docker-php-ext-install mysqli pdo pdo_mysql
RUN pecl install xdebug && docker-php-ext-enable xdebug

# Configure Xdebug
COPY xdebug.ini /usr/local/etc/php/conf.d/

# Enable Apache rewrite module
RUN a2enmod rewrite
RUN a2enmod headers

# Set development PHP configuration
COPY php.dev.ini /usr/local/etc/php/php.ini

# Copy source code
COPY src/ /var/www/html/

# Set permissions
RUN chown -R www-data:www-data /var/www/html
```

### Step 3: Android Development Setup

#### 3.1 Import Project
1. Open Android Studio
2. Choose "Open an existing Android Studio project"
3. Navigate to the project directory
4. Wait for Gradle sync

#### 3.2 Configure Zebra AI Vision SDK
```bash
# Download Zebra AI Vision SDK (requires Zebra developer account)
# Extract to project libs directory
mkdir -p AI_MultiBarcodes_Capture/libs
cp /path/to/zebra-sdk/*.aar AI_MultiBarcodes_Capture/libs/
```

#### 3.3 Development Build Configuration
```kotlin
// AI_MultiBarcodes_Capture/build.gradle.kts
android {
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Enable development features
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "DEFAULT_ENDPOINT", "\"http://10.0.2.2:8080/api/barcodes.php\"")
        }
    }
}
```

### Step 4: Development Workflow

#### 4.1 Start Development Environment
```bash
# Start all development services
cd WebInterface
docker-compose -f docker-compose.dev.yml up -d

# Start Android emulator (or connect physical device)
# Build and install debug version
cd ..
./gradlew installDebug
```

#### 4.2 Hot Reload Setup
```bash
# For web development (PHP files auto-reload)
# Files in WebInterface/src/ are mounted as volumes

# For Android development
# Use Android Studio's Instant Run or build incrementally
./gradlew assembleDebug --continuous
```

---

## 🔧 Post-Installation Configuration

### Database Initialization
```bash
# Access database
docker exec -it webinterface-db-1 mysql -u root -p

# Import schema if needed
mysql -u root -p barcode_capture < WebInterface/init/schema.sql
```

### Application Configuration
```bash
# Edit configuration files
nano WebInterface/src/config/config.php

# Test API endpoints
curl -X POST http://localhost:8080/api/barcodes.php \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'
```

### Network Configuration
```bash
# Find Docker host IP for Android configuration
docker network inspect webinterface_default

# Configure Android app endpoint
# Use the gateway IP or host machine IP
```

## 🧪 Installation Verification

### Complete System Test
```bash
# 1. Verify all services
docker-compose ps

# 2. Test web interface
curl -I http://localhost:8080

# 3. Test database connectivity
docker exec webinterface-db-1 mysqladmin ping

# 4. Test API endpoint
curl -X GET http://localhost:8080/api/sessions.php

# 5. Test Android installation
adb shell pm list packages | grep ai_multibarcodes
```

### Performance Verification
```bash
# Monitor resource usage
docker stats

# Check application logs
docker-compose logs -f web

# Monitor database performance
docker exec -it webinterface-db-1 mysqladmin processlist
```

## 🚨 Troubleshooting Common Issues

### Port Conflicts
```bash
# Check what's using port 8080
netstat -tulpn | grep :8080

# Kill conflicting process
sudo kill -9 $(lsof -t -i:8080)
```

### Permission Issues
```bash
# Fix file permissions
sudo chown -R $USER:$USER WebInterface/src
chmod -R 755 WebInterface/src
```

### Docker Issues
```bash
# Reset Docker environment
docker-compose down -v
docker system prune -f
docker-compose up -d
```

### Android Build Issues
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Check for missing dependencies
./gradlew dependencies
```

---

**Next Steps**: After successful installation, proceed to the [Docker Setup Guide](04-Docker-Setup-Guide.md) for advanced containerization or [Android App Configuration](07-Android-App-Configuration.md) for device-specific settings.
# Docker Setup Guide - AI MultiBarcode Capture

Complete guide for setting up the containerized web service infrastructure using Docker and Docker Compose.

## 📋 Table of Contents

1. [Docker Architecture Overview](#docker-architecture-overview)
2. [Prerequisites and Installation](#prerequisites-and-installation)
3. [Container Configuration](#container-configuration)
4. [Database Setup](#database-setup)
5. [Web Service Configuration](#web-service-configuration)
6. [SSL/HTTPS Setup](#sslhttps-setup)
7. [Production Deployment](#production-deployment)
8. [Monitoring and Maintenance](#monitoring-and-maintenance)
9. [Troubleshooting](#troubleshooting)

## 🏗️ Docker Architecture Overview

The AI MultiBarcode Capture system uses a multi-container Docker architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network (wms-network)             │
│                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │
│  │             │    │             │    │             │    │
│  │   Web App   │────│   MySQL     │    │ phpMyAdmin  │    │
│  │  (Apache +  │    │ Database    │    │  (Optional) │    │
│  │    PHP)     │    │             │    │             │    │
│  │             │    │             │    │             │    │
│  └─────────────┘    └─────────────┘    └─────────────┘    │
│       Port 8080          Port 3306         Port 8081       │
│       Port 8443     (Internal Only)   (Development Only)   │
└─────────────────────────────────────────────────────────────┘
```

### Container Responsibilities

| Container | Purpose | Ports | Dependencies |
|-----------|---------|-------|--------------|
| **web** | Apache + PHP web server, REST API | 8080 (HTTP), 8443 (HTTPS) | MySQL Database |
| **db** | MySQL 8.0 database server | 3306 (internal) | None |
| **phpmyadmin** | Database administration interface | 8081 | MySQL Database |

## 📦 Prerequisites and Installation

### System Requirements

**Minimum Requirements:**
- **RAM**: 4GB available
- **Storage**: 2GB free space
- **CPU**: 2 cores
- **Network**: Internet connection for initial setup

**Recommended Requirements:**
- **RAM**: 8GB available
- **Storage**: 10GB free space
- **CPU**: 4+ cores
- **Network**: Dedicated network interface

### Docker Installation

#### Windows
1. Download **Docker Desktop for Windows**
2. Run installer and restart system
3. Enable WSL 2 backend if prompted
4. Verify installation:
   ```bash
   docker --version
   docker-compose --version
   ```

#### Linux (Ubuntu/Debian)
```bash
# Update package index
sudo apt update

# Install Docker
sudo apt install docker.io docker-compose

# Add user to docker group
sudo usermod -aG docker $USER

# Restart session or run
newgrp docker

# Verify installation
docker --version
docker-compose --version
```

#### macOS
1. Download **Docker Desktop for Mac**
2. Install and start Docker Desktop
3. Verify installation:
   ```bash
   docker --version
   docker-compose --version
   ```

## 🔧 Container Configuration

### Docker Compose File Structure

The main configuration file is `WebInterface/docker-compose.yml`:

```yaml
services:
  web:
    build: .
    ports:
      - "8080:80"      # HTTP access
      - "8443:443"     # HTTPS access
    volumes:
      - ./src:/var/www/html
    depends_on:
      - db
    environment:
      - DB_HOST=db
      - DB_NAME=barcode_wms
      - DB_USER=wms_user
      - DB_PASS=wms_password
    networks:
      - wms-network

  db:
    image: mysql:8.0
    restart: always
    environment:
      MYSQL_DATABASE: barcode_wms
      MYSQL_USER: wms_user
      MYSQL_PASSWORD: wms_password
      MYSQL_ROOT_PASSWORD: root_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"    # External access (optional)
    networks:
      - wms-network

  phpmyadmin:
    image: phpmyadmin:latest
    restart: always
    ports:
      - "8081:80"
    environment:
      - PMA_HOST=db
      - PMA_USER=root
      - PMA_PASSWORD=root_password
    depends_on:
      - db
    networks:
      - wms-network

volumes:
  mysql_data:

networks:
  wms-network:
    driver: bridge
```

### Dockerfile Configuration

The web service Dockerfile (`WebInterface/Dockerfile`):

```dockerfile
FROM php:8.2-apache

# Install required PHP extensions
RUN docker-php-ext-install pdo pdo_mysql

# Enable Apache modules
RUN a2enmod rewrite headers expires ssl

# Install SSL certificate tools
RUN apt-get update && apt-get install -y openssl

# Copy application files
COPY src/ /var/www/html/

# Set proper permissions
RUN chown -R www-data:www-data /var/www/html
RUN chmod -R 755 /var/www/html

# Generate self-signed SSL certificate
RUN mkdir -p /etc/ssl/private /etc/ssl/certs
RUN openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/apache-selfsigned.key \
    -out /etc/ssl/certs/apache-selfsigned.crt \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"

# Enable SSL site
COPY ssl-site.conf /etc/apache2/sites-available/default-ssl.conf
RUN a2ensite default-ssl

# Expose ports
EXPOSE 80 443

# Start Apache
CMD ["apache2-foreground"]
```

## 🗄️ Database Setup

### Database Schema

The system automatically initializes with this schema:

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS barcode_wms;
USE barcode_wms;

-- Capture sessions table
CREATE TABLE IF NOT EXISTS capture_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_timestamp DATETIME NOT NULL,
    device_info VARCHAR(500),
    total_barcodes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_timestamp (session_timestamp),
    INDEX idx_created_at (created_at)
);

-- Barcodes table
CREATE TABLE IF NOT EXISTS barcodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    value VARCHAR(1000) NOT NULL,
    symbology INT NOT NULL,
    symbology_name VARCHAR(100),
    quantity INT DEFAULT 1,
    timestamp DATETIME NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES capture_sessions(id) ON DELETE CASCADE,
    INDEX idx_session_id (session_id),
    INDEX idx_value (value(255)),
    INDEX idx_symbology (symbology),
    INDEX idx_timestamp (timestamp),
    INDEX idx_processed (processed)
);

-- Symbology types lookup table
CREATE TABLE IF NOT EXISTS symbology_types (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    active BOOLEAN DEFAULT TRUE
);
```

### Database Connection Configuration

The application connects to the database using these environment variables:

```php
<?php
// WebInterface/src/config/database.php
class Database {
    private $host;
    private $db_name;
    private $username;
    private $password;
    public $conn;

    public function __construct() {
        $this->host = $_ENV['DB_HOST'] ?? 'db';
        $this->db_name = $_ENV['DB_NAME'] ?? 'barcode_wms';
        $this->username = $_ENV['DB_USER'] ?? 'wms_user';
        $this->password = $_ENV['DB_PASS'] ?? 'wms_password';
    }

    public function getConnection() {
        $this->conn = null;
        try {
            $this->conn = new PDO(
                "mysql:host=" . $this->host . ";dbname=" . $this->db_name,
                $this->username,
                $this->password
            );
            $this->conn->exec("set names utf8");
        } catch(PDOException $exception) {
            echo "Connection error: " . $exception->getMessage();
        }
        return $this->conn;
    }
}
?>
```

## 🌐 Web Service Configuration

### Apache Configuration

The web service runs on Apache with PHP 8.2:

```apache
# Basic Apache configuration
ServerName localhost
DocumentRoot /var/www/html

# Enable required modules
LoadModule rewrite_module modules/mod_rewrite.so
LoadModule headers_module modules/mod_headers.so
LoadModule expires_module modules/mod_expires.so

# Directory permissions
<Directory "/var/www/html">
    AllowOverride All
    Require all granted
</Directory>

# CORS headers for API access
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header always set Access-Control-Allow-Headers "Content-Type, Authorization, X-Requested-With"
```

### PHP Configuration

Key PHP settings for optimal performance:

```ini
; php.ini modifications
memory_limit = 256M
upload_max_filesize = 50M
post_max_size = 50M
max_execution_time = 300
max_input_vars = 3000

; Enable error logging
log_errors = On
error_log = /var/log/apache2/php_errors.log

; Security settings
expose_php = Off
allow_url_fopen = Off
```

## 🔒 SSL/HTTPS Setup

### Self-Signed Certificate (Development)

For development, the container automatically generates a self-signed certificate:

```bash
# Automatic generation in Dockerfile
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/apache-selfsigned.key \
    -out /etc/ssl/certs/apache-selfsigned.crt \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

### Production SSL Certificate

For production, replace with a proper SSL certificate:

1. **Obtain SSL certificate** from a trusted CA
2. **Copy certificate files** to the container:
   ```bash
   # Copy to Docker volume
   docker cp your-cert.crt webinterface-web-1:/etc/ssl/certs/
   docker cp your-private.key webinterface-web-1:/etc/ssl/private/
   ```
3. **Update Apache SSL configuration**:
   ```apache
   <VirtualHost *:443>
       ServerName your-domain.com
       DocumentRoot /var/www/html

       SSLEngine on
       SSLCertificateFile /etc/ssl/certs/your-cert.crt
       SSLCertificateKeyFile /etc/ssl/private/your-private.key
       SSLCertificateChainFile /etc/ssl/certs/chain.crt
   </VirtualHost>
   ```

### HTTPS Configuration for Android

When using HTTPS, update Android app configuration:

```java
// For self-signed certificates in development
private void configureSslForHttps(HttpsURLConnection connection) {
    try {
        // Create trust-all SSL context
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());

        // Disable hostname verification for development
        connection.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    } catch (Exception e) {
        Log.e("SSL", "Failed to configure SSL", e);
    }
}
```

## 🚀 Production Deployment

### Production Docker Compose

For production deployment, use this enhanced configuration:

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  web:
    build: .
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./src:/var/www/html:ro
      - ./logs:/var/log/apache2
      - ssl_certs:/etc/ssl
    depends_on:
      - db
    environment:
      - DB_HOST=db
      - DB_NAME=barcode_wms
      - DB_USER=wms_user
      - DB_PASS=${DB_PASSWORD}
      - ENVIRONMENT=production
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  db:
    image: mysql:8.0
    restart: always
    environment:
      MYSQL_DATABASE: barcode_wms
      MYSQL_USER: wms_user
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./mysql-conf:/etc/mysql/conf.d
    networks:
      - wms-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Remove phpMyAdmin in production
  # phpmyadmin: ...

volumes:
  mysql_data:
  ssl_certs:

networks:
  wms-network:
    driver: bridge

secrets:
  db_password:
    file: ./secrets/db_password.txt
  root_password:
    file: ./secrets/root_password.txt
```

### Production Deployment Commands

```bash
# Set environment variables
export DB_PASSWORD="your_secure_password"
export ROOT_PASSWORD="your_root_password"

# Deploy using production configuration
docker-compose -f docker-compose.prod.yml up -d

# Monitor deployment
docker-compose -f docker-compose.prod.yml logs -f

# Check service health
docker-compose -f docker-compose.prod.yml ps
```

### Production Security Checklist

- [ ] **Change default passwords** for all database users
- [ ] **Use proper SSL certificates** from trusted CA
- [ ] **Disable phpMyAdmin** in production
- [ ] **Configure firewall rules** to restrict access
- [ ] **Enable logging** and monitoring
- [ ] **Set up backup procedures** for database
- [ ] **Configure reverse proxy** (nginx/Apache) if needed
- [ ] **Implement rate limiting** for API endpoints

## 📊 Monitoring and Maintenance

### Health Checks

Monitor service health using Docker health checks:

```bash
# Check container health
docker-compose ps

# View health check logs
docker inspect webinterface-web-1 | grep -A 10 Health

# Manual health check
curl -f http://localhost:8080/health
```

### Log Management

Access and manage container logs:

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs web
docker-compose logs db

# Follow logs in real-time
docker-compose logs -f web

# View last 100 lines
docker-compose logs --tail=100 web
```

### Backup Procedures

#### Database Backup
```bash
# Create database backup
docker-compose exec db mysqldump -u root -p barcode_wms > backup_$(date +%Y%m%d_%H%M%S).sql

# Automated backup script
#!/bin/bash
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
docker-compose exec db mysqldump -u root -proot_password barcode_wms > $BACKUP_DIR/backup_$DATE.sql
gzip $BACKUP_DIR/backup_$DATE.sql
```

#### Volume Backup
```bash
# Backup Docker volumes
docker run --rm -v webinterface_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_data_backup.tar.gz -C /data .
```

### Performance Monitoring

Monitor system performance:

```bash
# Container resource usage
docker stats

# Database performance
docker-compose exec db mysql -u root -p -e "SHOW PROCESSLIST;"
docker-compose exec db mysql -u root -p -e "SHOW STATUS LIKE 'Slow_queries';"

# Web server performance
docker-compose exec web apachectl status
```

## 🔧 Troubleshooting

### Common Issues and Solutions

#### Container Won't Start
```bash
# Check logs for errors
docker-compose logs web
docker-compose logs db

# Verify port availability
netstat -tulpn | grep :8080
lsof -i :8080

# Restart services
docker-compose down
docker-compose up -d
```

#### Database Connection Issues
```bash
# Test database connectivity
docker-compose exec web mysql -h db -u wms_user -p

# Check database status
docker-compose exec db mysql -u root -p -e "SHOW DATABASES;"

# Reset database
docker-compose down
docker volume rm webinterface_mysql_data
docker-compose up -d
```

#### SSL/HTTPS Issues
```bash
# Check SSL certificate
openssl x509 -in /path/to/cert.crt -text -noout

# Test HTTPS connection
curl -k https://localhost:8443

# Regenerate self-signed certificate
docker-compose exec web openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/apache-selfsigned.key \
    -out /etc/ssl/certs/apache-selfsigned.crt \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

#### Performance Issues
```bash
# Check resource usage
docker stats webinterface-web-1 webinterface-db-1

# Optimize MySQL configuration
# Add to mysql-conf/my.cnf:
[mysqld]
innodb_buffer_pool_size = 1G
max_connections = 200
query_cache_size = 64M

# Restart to apply changes
docker-compose restart db
```

### Debug Mode

Enable debug mode for troubleshooting:

```bash
# Set debug environment variable
docker-compose exec web bash
export DEBUG=true

# Enable PHP error reporting
echo "error_reporting = E_ALL" >> /usr/local/etc/php/conf.d/debug.ini
echo "display_errors = On" >> /usr/local/etc/php/conf.d/debug.ini

# Restart Apache
apache2ctl restart
```

---

**🎯 This Docker setup provides a robust, scalable foundation for the AI MultiBarcode Capture system.** The containerized architecture ensures consistent deployment across different environments while providing flexibility for customization and scaling.
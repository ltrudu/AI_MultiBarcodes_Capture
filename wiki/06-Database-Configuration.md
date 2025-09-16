# Database Configuration - AI MultiBarcode Capture

This guide covers comprehensive database setup, schema management, optimization, and maintenance for the AI MultiBarcode Capture system.

## 🏗️ Database Architecture Overview

The system uses MySQL 8.0 with a normalized schema designed for high-performance barcode data storage and retrieval.

### Core Tables Structure
```
sessions (main session data)
├── barcodes (individual barcode records)
├── session_summary (aggregated session statistics)
└── symbology_types (barcode type definitions)
```

### Entity Relationship Diagram
```
┌─────────────────┐    1:N    ┌─────────────────┐
│    sessions     │◄──────────┤    barcodes     │
├─────────────────┤           ├─────────────────┤
│ id (PK)         │           │ id (PK)         │
│ device_info     │           │ session_id (FK) │
│ session_start   │           │ data            │
│ session_end     │           │ symbology_id    │
│ total_barcodes  │           │ timestamp       │
│ created_at      │           │ x_position      │
└─────────────────┘           │ y_position      │
                              └─────────────────┘
                                      │ N:1
                                      ▼
                              ┌─────────────────┐
                              │ symbology_types │
                              ├─────────────────┤
                              │ id (PK)         │
                              │ name            │
                              │ description     │
                              └─────────────────┘
```

## 📊 Complete Database Schema

### Sessions Table
```sql
CREATE TABLE sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_info VARCHAR(255) NOT NULL DEFAULT 'Unknown Device',
    session_start DATETIME NOT NULL,
    session_end DATETIME DEFAULT NULL,
    total_barcodes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_device_info (device_info),
    INDEX idx_session_start (session_start),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Barcodes Table
```sql
CREATE TABLE barcodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    data TEXT NOT NULL,
    symbology_id INT NOT NULL DEFAULT 1,
    timestamp DATETIME NOT NULL,
    x_position DECIMAL(10,4) DEFAULT NULL,
    y_position DECIMAL(10,4) DEFAULT NULL,
    confidence_score DECIMAL(5,4) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (symbology_id) REFERENCES symbology_types(id),

    INDEX idx_session_id (session_id),
    INDEX idx_symbology_id (symbology_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_data (data(255)),
    FULLTEXT INDEX ft_data (data)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Symbology Types Table
```sql
CREATE TABLE symbology_types (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Session Summary View
```sql
CREATE VIEW session_summary AS
SELECT
    s.id,
    s.device_info,
    s.session_start,
    s.session_end,
    s.total_barcodes,
    s.created_at,
    COUNT(b.id) as actual_barcode_count,
    MIN(b.timestamp) as first_barcode_time,
    MAX(b.timestamp) as last_barcode_time,
    TIMESTAMPDIFF(SECOND, MIN(b.timestamp), MAX(b.timestamp)) as session_duration_seconds,
    GROUP_CONCAT(DISTINCT st.name ORDER BY st.name SEPARATOR ', ') as symbology_types_used
FROM sessions s
LEFT JOIN barcodes b ON s.id = b.session_id
LEFT JOIN symbology_types st ON b.symbology_id = st.id
GROUP BY s.id, s.device_info, s.session_start, s.session_end, s.total_barcodes, s.created_at
ORDER BY s.created_at DESC;
```

## 🔧 Docker Database Configuration

### Production Docker Compose Configuration
```yaml
# WebInterface/docker-compose.yml
version: '3.8'

services:
  db:
    image: mysql:8.0
    container_name: ai_barcode_mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: barcode_capture
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - db_data:/var/lib/mysql
      - ./init:/docker-entrypoint-initdb.d:ro
      - ./backup:/backup:rw
      - ./my.cnf:/etc/mysql/conf.d/custom.cnf:ro
    ports:
      - "3306:3306"
    restart: unless-stopped
    command: >
      --default-authentication-plugin=mysql_native_password
      --innodb-buffer-pool-size=1G
      --innodb-log-file-size=256M
      --max-connections=200
      --query-cache-size=64M
      --slow-query-log=1
      --slow-query-log-file=/var/log/mysql/slow.log
      --long-query-time=2

volumes:
  db_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /opt/ai-barcode/mysql-data
```

### Custom MySQL Configuration
```ini
# WebInterface/my.cnf
[mysqld]
# Performance tuning
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# Connection settings
max_connections = 200
max_connect_errors = 100000
wait_timeout = 28800
interactive_timeout = 28800

# Query cache
query_cache_type = 1
query_cache_size = 64M
query_cache_limit = 2M

# Logging
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
log_queries_not_using_indexes = 1

# Character set
character_set_server = utf8mb4
collation_server = utf8mb4_unicode_ci

# Security
local_infile = 0
skip_name_resolve = 1

[mysql]
default_character_set = utf8mb4

[client]
default_character_set = utf8mb4
```

## 🚀 Database Initialization

### Complete Initialization Script
```sql
-- WebInterface/init/01-init-database.sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS barcode_capture
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE barcode_capture;

-- Drop tables if they exist (for clean initialization)
DROP TABLE IF EXISTS barcodes;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS symbology_types;
DROP VIEW IF EXISTS session_summary;

-- Create symbology_types table (must be first due to foreign keys)
CREATE TABLE symbology_types (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert symbology types (matching Android app enum values)
INSERT INTO symbology_types (id, name, description) VALUES
(1, 'CODE128', 'Code 128 barcode symbology'),
(2, 'CODE39', 'Code 39 barcode symbology'),
(3, 'CODE93', 'Code 93 barcode symbology'),
(4, 'CODABAR', 'Codabar barcode symbology'),
(5, 'EAN8', 'EAN-8 barcode symbology'),
(6, 'EAN13', 'EAN-13 barcode symbology'),
(7, 'UPCA', 'UPC-A barcode symbology'),
(8, 'UPCE', 'UPC-E barcode symbology'),
(9, 'ITF', 'Interleaved 2 of 5 barcode symbology'),
(10, 'RSS14', 'RSS-14 barcode symbology'),
(11, 'RSS_EXPANDED', 'RSS Expanded barcode symbology'),
(12, 'DATAMATRIX', 'Data Matrix 2D barcode symbology'),
(13, 'PDF417', 'PDF417 2D barcode symbology'),
(14, 'AZTEC', 'Aztec 2D barcode symbology'),
(15, 'QRCODE', 'QR Code 2D barcode symbology'),
(16, 'MAXICODE', 'MaxiCode 2D barcode symbology'),
(17, 'MICROQR', 'Micro QR Code 2D barcode symbology'),
(18, 'MICROPDF', 'Micro PDF417 2D barcode symbology'),
(19, 'GS1_DATABAR', 'GS1 DataBar barcode symbology'),
(20, 'DUTCH_POSTAL', 'Dutch Postal barcode symbology');

-- Create sessions table
CREATE TABLE sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_info VARCHAR(255) NOT NULL DEFAULT 'Unknown Device',
    session_start DATETIME NOT NULL,
    session_end DATETIME DEFAULT NULL,
    total_barcodes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_device_info (device_info),
    INDEX idx_session_start (session_start),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create barcodes table
CREATE TABLE barcodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    data TEXT NOT NULL,
    symbology_id INT NOT NULL DEFAULT 1,
    timestamp DATETIME NOT NULL,
    x_position DECIMAL(10,4) DEFAULT NULL,
    y_position DECIMAL(10,4) DEFAULT NULL,
    confidence_score DECIMAL(5,4) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (symbology_id) REFERENCES symbology_types(id),

    INDEX idx_session_id (session_id),
    INDEX idx_symbology_id (symbology_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_data (data(255)),
    FULLTEXT INDEX ft_data (data)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create session summary view
CREATE VIEW session_summary AS
SELECT
    s.id,
    s.device_info,
    s.session_start,
    s.session_end,
    s.total_barcodes,
    s.created_at,
    COUNT(b.id) as actual_barcode_count,
    MIN(b.timestamp) as first_barcode_time,
    MAX(b.timestamp) as last_barcode_time,
    TIMESTAMPDIFF(SECOND, MIN(b.timestamp), MAX(b.timestamp)) as session_duration_seconds,
    GROUP_CONCAT(DISTINCT st.name ORDER BY st.name SEPARATOR ', ') as symbology_types_used
FROM sessions s
LEFT JOIN barcodes b ON s.id = b.session_id
LEFT JOIN symbology_types st ON b.symbology_id = st.id
GROUP BY s.id, s.device_info, s.session_start, s.session_end, s.total_barcodes, s.created_at
ORDER BY s.created_at DESC;

SET FOREIGN_KEY_CHECKS = 1;

-- Create application user and grant permissions
CREATE USER IF NOT EXISTS 'barcode_app'@'%' IDENTIFIED BY 'secure_app_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON barcode_capture.* TO 'barcode_app'@'%';
FLUSH PRIVILEGES;

-- Insert sample data for testing (optional)
INSERT INTO sessions (device_info, session_start, total_barcodes) VALUES
('Samsung_Galaxy_S24_Android14', NOW() - INTERVAL 1 HOUR, 5),
('Zebra_TC21_Android11', NOW() - INTERVAL 30 MINUTE, 3);

INSERT INTO barcodes (session_id, data, symbology_id, timestamp) VALUES
(1, '1234567890123', 6, NOW() - INTERVAL 55 MINUTE),
(1, 'ABC123DEF456', 1, NOW() - INTERVAL 50 MINUTE),
(1, 'https://example.com/product/123', 15, NOW() - INTERVAL 45 MINUTE),
(2, '9876543210987', 6, NOW() - INTERVAL 25 MINUTE),
(2, 'XYZ789GHI012', 1, NOW() - INTERVAL 20 MINUTE);
```

## 🔒 Security Configuration

### User Management and Permissions
```sql
-- WebInterface/init/02-security.sql

-- Remove anonymous users and test databases
DELETE FROM mysql.user WHERE User='';
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
DROP DATABASE IF EXISTS test;
DELETE FROM mysql.db WHERE Db='test' OR Db='test\\_%';

-- Create application-specific users
CREATE USER IF NOT EXISTS 'barcode_app'@'%' IDENTIFIED BY 'secure_app_password';
CREATE USER IF NOT EXISTS 'barcode_readonly'@'%' IDENTIFIED BY 'readonly_password';
CREATE USER IF NOT EXISTS 'barcode_backup'@'localhost' IDENTIFIED BY 'backup_password';

-- Grant appropriate permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON barcode_capture.* TO 'barcode_app'@'%';
GRANT SELECT ON barcode_capture.* TO 'barcode_readonly'@'%';
GRANT SELECT, LOCK TABLES ON barcode_capture.* TO 'barcode_backup'@'localhost';

-- Restrict dangerous privileges
REVOKE FILE ON *.* FROM 'barcode_app'@'%';
REVOKE PROCESS ON *.* FROM 'barcode_app'@'%';
REVOKE SUPER ON *.* FROM 'barcode_app'@'%';

FLUSH PRIVILEGES;
```

### Database Connection Security
```php
// WebInterface/src/config/database.php
<?php
class DatabaseConfig {
    private static $host = 'db';
    private static $database = 'barcode_capture';
    private static $username = 'barcode_app';
    private static $password = 'secure_app_password';
    private static $options = [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
        PDO::MYSQL_ATTR_SSL_VERIFY_SERVER_CERT => false,
        PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
    ];

    public static function getConnection() {
        try {
            $dsn = "mysql:host=" . self::$host . ";dbname=" . self::$database . ";charset=utf8mb4";
            return new PDO($dsn, self::$username, self::$password, self::$options);
        } catch (PDOException $e) {
            error_log("Database connection failed: " . $e->getMessage());
            throw new Exception("Database connection failed");
        }
    }
}
?>
```

## 📈 Performance Optimization

### Database Tuning
```sql
-- Analyze table statistics
ANALYZE TABLE sessions, barcodes, symbology_types;

-- Optimize tables
OPTIMIZE TABLE sessions, barcodes, symbology_types;

-- Check index usage
SHOW INDEX FROM barcodes;
SHOW INDEX FROM sessions;

-- Performance monitoring queries
SELECT
    TABLE_NAME,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) as 'DB Size (MB)',
    TABLE_ROWS,
    ROUND((INDEX_LENGTH / 1024 / 1024), 2) as 'Index Size (MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'barcode_capture'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;
```

### Query Optimization Examples
```sql
-- Efficient session retrieval with pagination
SELECT
    s.*,
    COUNT(b.id) as barcode_count,
    MAX(b.timestamp) as last_scan_time
FROM sessions s
LEFT JOIN barcodes b ON s.id = b.session_id
WHERE s.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY s.id
ORDER BY s.created_at DESC
LIMIT 20 OFFSET 0;

-- Fast barcode search with full-text index
SELECT
    b.*,
    s.device_info,
    st.name as symbology_name
FROM barcodes b
JOIN sessions s ON b.session_id = s.id
JOIN symbology_types st ON b.symbology_id = st.id
WHERE MATCH(b.data) AGAINST('1234567890' IN BOOLEAN MODE)
ORDER BY b.timestamp DESC
LIMIT 50;

-- Efficient symbology statistics
SELECT
    st.name,
    COUNT(b.id) as barcode_count,
    COUNT(DISTINCT b.session_id) as session_count,
    AVG(b.confidence_score) as avg_confidence
FROM symbology_types st
LEFT JOIN barcodes b ON st.id = b.symbology_id
WHERE b.timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY st.id, st.name
ORDER BY barcode_count DESC;
```

## 🔄 Backup and Maintenance

### Automated Backup Script
```bash
#!/bin/bash
# WebInterface/scripts/backup-database.sh

CONTAINER_NAME="ai_barcode_mysql"
DB_NAME="barcode_capture"
DB_USER="barcode_backup"
DB_PASS="backup_password"
BACKUP_DIR="/backup"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory if it doesn't exist
docker exec $CONTAINER_NAME mkdir -p $BACKUP_DIR

# Full database backup
docker exec $CONTAINER_NAME mysqldump \
    -u $DB_USER -p$DB_PASS \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --hex-blob \
    $DB_NAME > "${BACKUP_DIR}/full_backup_${DATE}.sql"

# Schema-only backup
docker exec $CONTAINER_NAME mysqldump \
    -u $DB_USER -p$DB_PASS \
    --no-data \
    --routines \
    --triggers \
    --events \
    $DB_NAME > "${BACKUP_DIR}/schema_backup_${DATE}.sql"

# Compress backups
gzip "${BACKUP_DIR}/full_backup_${DATE}.sql"
gzip "${BACKUP_DIR}/schema_backup_${DATE}.sql"

# Clean old backups (keep last 30 days)
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: $(date)"
```

### Database Maintenance Script
```bash
#!/bin/bash
# WebInterface/scripts/maintenance.sh

CONTAINER_NAME="ai_barcode_mysql"
DB_NAME="barcode_capture"
DB_USER="root"
DB_PASS="your_root_password"

# Analyze and optimize tables
docker exec $CONTAINER_NAME mysql -u $DB_USER -p$DB_PASS -e "
USE $DB_NAME;
ANALYZE TABLE sessions, barcodes, symbology_types;
OPTIMIZE TABLE sessions, barcodes, symbology_types;
"

# Clean old data (optional - keep last 90 days)
docker exec $CONTAINER_NAME mysql -u $DB_USER -p$DB_PASS -e "
USE $DB_NAME;
DELETE FROM barcodes WHERE timestamp < DATE_SUB(NOW(), INTERVAL 90 DAY);
DELETE FROM sessions WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY) AND id NOT IN (SELECT DISTINCT session_id FROM barcodes);
"

echo "Maintenance completed: $(date)"
```

## 📊 Monitoring and Diagnostics

### Performance Monitoring Queries
```sql
-- Check slow queries
SELECT
    query_time,
    lock_time,
    rows_sent,
    rows_examined,
    sql_text
FROM mysql.slow_log
WHERE start_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY query_time DESC
LIMIT 10;

-- Monitor table sizes and growth
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(DATA_LENGTH/1024/1024,2) as 'Data Size (MB)',
    ROUND(INDEX_LENGTH/1024/1024,2) as 'Index Size (MB)',
    ROUND((DATA_LENGTH+INDEX_LENGTH)/1024/1024,2) as 'Total Size (MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'barcode_capture'
ORDER BY (DATA_LENGTH+INDEX_LENGTH) DESC;

-- Check connection usage
SHOW PROCESSLIST;

-- Monitor buffer pool usage
SHOW STATUS LIKE 'Innodb_buffer_pool%';
```

### Health Check Script
```bash
#!/bin/bash
# WebInterface/scripts/health-check.sh

CONTAINER_NAME="ai_barcode_mysql"
HEALTH_STATUS=0

# Check if container is running
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "ERROR: MySQL container is not running"
    HEALTH_STATUS=1
fi

# Check database connectivity
if ! docker exec $CONTAINER_NAME mysqladmin ping --silent; then
    echo "ERROR: MySQL is not responding"
    HEALTH_STATUS=1
fi

# Check disk space
DISK_USAGE=$(docker exec $CONTAINER_NAME df /var/lib/mysql | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 80 ]; then
    echo "WARNING: Disk usage is ${DISK_USAGE}%"
    HEALTH_STATUS=1
fi

# Check recent errors in logs
ERROR_COUNT=$(docker logs $CONTAINER_NAME --since="1h" 2>&1 | grep -i error | wc -l)
if [ $ERROR_COUNT -gt 0 ]; then
    echo "WARNING: Found $ERROR_COUNT errors in the last hour"
fi

if [ $HEALTH_STATUS -eq 0 ]; then
    echo "Database health check: PASSED"
else
    echo "Database health check: FAILED"
fi

exit $HEALTH_STATUS
```

---

**Next Steps**: After database configuration, proceed to [Android App Configuration](07-Android-App-Configuration.md) for device setup or [WMS Architecture](10-WMS-Architecture.md) for web interface customization.
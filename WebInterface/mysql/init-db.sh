#!/bin/bash
# AI MultiBarcode Capture - Database Initialization Script

# Wait for MySQL to be ready
until mysqladmin ping --silent; do
    echo "Waiting for MySQL to be ready..."
    sleep 2
done

echo "Initializing AI MultiBarcode Capture database..."

# Read environment variables with defaults
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root_password}
MYSQL_DATABASE=${MYSQL_DATABASE:-barcode_wms}
MYSQL_USER=${MYSQL_USER:-wms_user}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-wms_password}

# Initialize MySQL root password if not set
if [ ! -f /var/lib/mysql/mysql_init_complete ]; then
    echo "Setting up MySQL for first time..."

    # Start MySQL temporarily
    mysqld_safe --skip-grant-tables --skip-networking &
    MYSQL_PID=$!

    # Wait for MySQL to start
    until mysqladmin ping --silent; do
        sleep 1
    done

    # Set root password
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_ROOT_PASSWORD';"
    mysql -e "FLUSH PRIVILEGES;"

    # Stop temporary MySQL
    mysqladmin shutdown
    wait $MYSQL_PID

    # Mark initialization as complete
    touch /var/lib/mysql/mysql_init_complete
fi

# Create database and user if they don't exist
mysql -u root -p$MYSQL_ROOT_PASSWORD << EOF
-- Create database
CREATE DATABASE IF NOT EXISTS $MYSQL_DATABASE
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';
CREATE USER IF NOT EXISTS '$MYSQL_USER'@'localhost' IDENTIFIED BY '$MYSQL_PASSWORD';

-- Grant privileges
GRANT ALL PRIVILEGES ON $MYSQL_DATABASE.* TO '$MYSQL_USER'@'%';
GRANT ALL PRIVILEGES ON $MYSQL_DATABASE.* TO '$MYSQL_USER'@'localhost';

-- Use the database
USE $MYSQL_DATABASE;

-- Create tables if they don't exist
CREATE TABLE IF NOT EXISTS symbology_types (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sessions (
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

CREATE TABLE IF NOT EXISTS barcodes (
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

-- Insert symbology types if table is empty
INSERT IGNORE INTO symbology_types (id, name, description) VALUES
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

FLUSH PRIVILEGES;
EOF

echo "Database initialization completed successfully!"
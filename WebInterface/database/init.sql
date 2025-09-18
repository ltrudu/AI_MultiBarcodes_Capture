-- Initialize Barcode WMS Database
CREATE DATABASE IF NOT EXISTS barcode_wms;
USE barcode_wms;

-- Symbology Reference Table - SINGLE SOURCE OF TRUTH
DROP TABLE IF EXISTS symbologies;
CREATE TABLE symbologies (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    UNIQUE KEY unique_id (id),
    UNIQUE KEY unique_name (name)
);

-- Insert EXACT values from Android enum EBarcodesSymbologies.java
INSERT INTO symbologies (id, name) VALUES
(-1, 'UNKNOWN'),
(0, 'EAN 8'),
(1, 'EAN 13'),
(2, 'UPC A'),
(3, 'UPC E'),
(4, 'AZTEC'),
(5, 'CODABAR'),
(6, 'CODE128'),
(7, 'CODE39'),
(8, 'I2OF5'),
(9, 'GS1 DATABAR'),
(10, 'DATAMATRIX'),
(11, 'GS1 DATABAR EXPANDED'),
(12, 'MAILMARK'),
(13, 'MAXICODE'),
(14, 'PDF417'),
(15, 'QRCODE'),
(16, 'DOTCODE'),
(17, 'GRID MATRIX'),
(18, 'GS1 DATAMATRIX'),
(19, 'GS1 QRCODE'),
(20, 'MICROQR'),
(21, 'MICROPDF'),
(22, 'USPOSTNET'),
(23, 'USPLANET'),
(24, 'UK POSTAL'),
(25, 'JAPANESE POSTAL'),
(26, 'AUSTRALIAN POSTAL'),
(27, 'CANADIAN POSTAL'),
(28, 'DUTCH POSTAL'),
(29, 'US4STATE'),
(30, 'US4STATE FICS'),
(31, 'MSI'),
(32, 'CODE93'),
(33, 'TRIOPTIC39'),
(34, 'D2OF5'),
(35, 'CHINESE 2OF5'),
(36, 'KOREAN 3OF5'),
(37, 'CODE11'),
(38, 'TLC39'),
(39, 'HANXIN'),
(40, 'MATRIX 2OF5'),
(41, 'UPCE1'),
(42, 'GS1 DATABAR LIM'),
(43, 'FINNISH POSTAL 4S'),
(44, 'COMPOSITE AB'),
(45, 'COMPOSITE C');

-- Table to store barcode capture sessions
CREATE TABLE IF NOT EXISTS capture_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_timestamp DATETIME NOT NULL,
    device_info VARCHAR(500),
    device_ip VARCHAR(45),
    total_barcodes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_timestamp (session_timestamp),
    INDEX idx_created_at (created_at),
    INDEX idx_device_ip (device_ip)
);

-- Table to store individual barcode data
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


-- Create view for easy barcode data retrieval with symbology names
DROP VIEW IF EXISTS barcode_details;
CREATE VIEW barcode_details AS
SELECT
    b.id,
    b.session_id,
    b.value,
    b.symbology,
    COALESCE(s.name, 'UNKNOWN') as symbology_name,
    b.quantity,
    b.timestamp,
    b.processed,
    b.notes,
    b.created_at,
    cs.session_timestamp,
    cs.device_info,
    cs.device_ip
FROM barcodes b
LEFT JOIN symbologies s ON b.symbology = s.id
LEFT JOIN capture_sessions cs ON b.session_id = cs.id;

-- Create view for session statistics
DROP VIEW IF EXISTS session_statistics;
CREATE VIEW session_statistics AS
SELECT
    cs.id,
    cs.session_timestamp,
    cs.device_info,
    cs.device_ip,
    cs.created_at,
    COUNT(b.id) as total_barcodes,
    COUNT(DISTINCT b.symbology) as unique_symbologies,
    MIN(b.timestamp) as first_scan,
    MAX(b.timestamp) as last_scan,
    SUM(b.quantity) as total_quantity,
    COUNT(CASE WHEN b.processed = TRUE THEN 1 END) as processed_count,
    COUNT(CASE WHEN b.processed = FALSE THEN 1 END) as pending_count
FROM capture_sessions cs
LEFT JOIN barcodes b ON cs.id = b.session_id
GROUP BY cs.id, cs.session_timestamp, cs.device_info, cs.device_ip, cs.created_at;

-- Sample data for testing (optional)
-- INSERT INTO capture_sessions (session_timestamp, device_info, total_barcodes)
-- VALUES ('2024-01-15 10:30:00', 'Android Test Device', 3);

-- INSERT INTO barcodes (session_id, value, symbology, quantity, timestamp) VALUES
-- (1, '1234567890128', 17, 1, '2024-01-15 10:30:15'),
-- (1, 'TEST123', 9, 2, '2024-01-15 10:30:45'),
-- (1, 'SAMPLE456', 7, 1, '2024-01-15 10:31:00');
-- Initialize Barcode WMS Database
CREATE DATABASE IF NOT EXISTS barcode_wms;
USE barcode_wms;

-- Table to store barcode capture sessions
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

-- Table to store barcode symbology mappings (based on Android app constants)
CREATE TABLE IF NOT EXISTS symbology_types (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    active BOOLEAN DEFAULT TRUE
);

-- Insert symbology types based on Android app EBarcodesSymbologies
INSERT INTO symbology_types (id, name, description) VALUES
(0, 'UNKNOWN', 'Unknown symbology type'),
(1, 'AUSTRALIAN_POSTAL', 'Australian Postal barcode'),
(2, 'AZTEC', 'Aztec 2D barcode'),
(3, 'CANADIAN_POSTAL', 'Canadian Postal barcode'),
(4, 'CHINESE_2OF5', 'Chinese 2 of 5 barcode'),
(5, 'CODABAR', 'Codabar barcode'),
(6, 'CODE11', 'Code 11 barcode'),
(7, 'CODE39', 'Code 39 barcode'),
(8, 'CODE93', 'Code 93 barcode'),
(9, 'CODE128', 'Code 128 barcode'),
(10, 'COMPOSITE_AB', 'Composite AB barcode'),
(11, 'COMPOSITE_C', 'Composite C barcode'),
(12, 'D2OF5', 'Discrete 2 of 5 barcode'),
(13, 'DATAMATRIX', 'Data Matrix 2D barcode'),
(14, 'DOTCODE', 'DotCode 2D barcode'),
(15, 'DUTCH_POSTAL', 'Dutch Postal barcode'),
(16, 'EAN_8', 'EAN-8 barcode'),
(17, 'EAN_13', 'EAN-13 barcode'),
(18, 'FINNISH_POSTAL_4S', 'Finnish Postal 4-State barcode'),
(19, 'GRID_MATRIX', 'Grid Matrix 2D barcode'),
(20, 'GS1_DATABAR', 'GS1 DataBar barcode'),
(21, 'GS1_DATABAR_EXPANDED', 'GS1 DataBar Expanded barcode'),
(22, 'GS1_DATABAR_LIM', 'GS1 DataBar Limited barcode'),
(23, 'GS1_DATAMATRIX', 'GS1 Data Matrix barcode'),
(24, 'GS1_QRCODE', 'GS1 QR Code barcode'),
(25, 'HANXIN', 'Han Xin Code 2D barcode'),
(26, 'I2OF5', 'Interleaved 2 of 5 barcode'),
(27, 'JAPANESE_POSTAL', 'Japanese Postal barcode'),
(28, 'KOREAN_3OF5', 'Korean 3 of 5 barcode'),
(29, 'MAILMARK', 'Royal Mail Mailmark barcode'),
(30, 'MATRIX_2OF5', 'Matrix 2 of 5 barcode'),
(31, 'MAXICODE', 'MaxiCode 2D barcode'),
(32, 'MICROPDF', 'MicroPDF417 barcode'),
(33, 'MICROQR', 'Micro QR Code barcode'),
(34, 'MSI', 'MSI barcode'),
(35, 'PDF417', 'PDF417 2D barcode'),
(36, 'QRCODE', 'QR Code 2D barcode'),
(37, 'TLC39', 'TLC-39 barcode'),
(38, 'TRIOPTIC39', 'Trioptic Code 39 barcode'),
(39, 'UK_POSTAL', 'UK Postal barcode'),
(40, 'UPC_A', 'UPC-A barcode'),
(41, 'UPC_E', 'UPC-E barcode'),
(42, 'UPCE1', 'UPC-E1 barcode'),
(43, 'USPLANET', 'USPS PLANET barcode'),
(44, 'USPOSTNET', 'USPS POSTNET barcode'),
(45, 'US4STATE', 'USPS 4-State barcode'),
(46, 'US4STATE_FICS', 'USPS 4-State FICS barcode');

-- Create view for easy barcode data retrieval with symbology names
CREATE VIEW barcode_details AS
SELECT
    b.id,
    b.session_id,
    b.value,
    b.symbology,
    COALESCE(st.name, 'UNKNOWN') as symbology_name,
    b.quantity,
    b.timestamp,
    b.processed,
    b.notes,
    b.created_at,
    cs.session_timestamp,
    cs.device_info
FROM barcodes b
LEFT JOIN symbology_types st ON b.symbology = st.id
LEFT JOIN capture_sessions cs ON b.session_id = cs.id;

-- Create view for session statistics
CREATE VIEW session_statistics AS
SELECT
    cs.id,
    cs.session_timestamp,
    cs.device_info,
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
GROUP BY cs.id, cs.session_timestamp, cs.device_info, cs.created_at;

-- Sample data for testing (optional)
-- INSERT INTO capture_sessions (session_timestamp, device_info, total_barcodes)
-- VALUES ('2024-01-15 10:30:00', 'Android Test Device', 3);

-- INSERT INTO barcodes (session_id, value, symbology, quantity, timestamp) VALUES
-- (1, '1234567890128', 17, 1, '2024-01-15 10:30:15'),
-- (1, 'TEST123', 9, 2, '2024-01-15 10:30:45'),
-- (1, 'SAMPLE456', 7, 1, '2024-01-15 10:31:00');
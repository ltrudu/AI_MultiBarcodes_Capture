# Web Service Development - AI MultiBarcode Capture

This guide explains how to create a simple web service to receive and process barcode data sent by the Android application.

## 🎯 What You'll Learn

- How the Android app sends data to your web service
- What data format to expect from the Android application
- How to create a simple PHP endpoint to receive the data
- How to store the received data in a database

## 📡 How the Android App Sends Data

When configured in **HTTP(s) Post** mode, the Android app sends barcode session data to your web service endpoint via HTTP POST requests.

### Request Details
- **Method**: POST
- **Content-Type**: application/json
- **Endpoint**: Your configured URL (e.g., `http://your-server:8080/api/barcodes.php`)

### Data Format
The Android app sends JSON data in this format:

```json
{
  "session_start": "2024-03-15T14:30:22.123Z",
  "device_info": "Samsung_Galaxy_S24_Android14",
  "barcodes": [
    {
      "data": "1234567890123",
      "symbology": "EAN13",
      "timestamp": "2024-03-15T14:30:25.456Z"
    },
    {
      "data": "https://example.com/product/123",
      "symbology": "QRCODE",
      "timestamp": "2024-03-15T14:30:28.789Z"
    }
  ]
}
```

### Field Descriptions
- **session_start**: When the barcode scanning session began (ISO 8601 format)
- **device_info**: Unique identifier for the Android device (Manufacturer_Model_AndroidVersion)
- **barcodes**: Array of captured barcodes with:
  - **data**: The actual barcode content
  - **symbology**: Type of barcode (QRCODE, EAN13, CODE128, etc.)
  - **timestamp**: When this specific barcode was scanned

## 🔧 Creating a Simple Web Service

Here's a basic PHP script to receive and process the Android app data:

### Basic Endpoint (`barcodes.php`)

```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only accept POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit();
}

// Get the JSON data from the Android app
$input = file_get_contents('php://input');
$data = json_decode($input, true);

// Validate the data
if (!$data || !isset($data['barcodes']) || !isset($data['session_start'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid data format']);
    exit();
}

// Extract the data
$session_start = $data['session_start'];
$device_info = $data['device_info'] ?? 'Unknown Device';
$barcodes = $data['barcodes'];

// Log the received data (for debugging)
error_log("Received session from: " . $device_info);
error_log("Session start: " . $session_start);
error_log("Number of barcodes: " . count($barcodes));

// Process each barcode
foreach ($barcodes as $barcode) {
    $barcode_data = $barcode['data'];
    $symbology = $barcode['symbology'];
    $timestamp = $barcode['timestamp'];

    // Here you can save to database, file, or process as needed
    error_log("Barcode: $barcode_data (Type: $symbology, Time: $timestamp)");
}

// Send success response back to Android app
http_response_code(200);
echo json_encode([
    'success' => true,
    'message' => 'Data received successfully',
    'session_start' => $session_start,
    'barcodes_count' => count($barcodes)
]);
?>
```

## 💾 Storing Data in Database

If you want to store the received data in a MySQL database:

### Database Setup

```sql
-- Create database
CREATE DATABASE barcode_data;
USE barcode_data;

-- Sessions table
CREATE TABLE sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_info VARCHAR(255) NOT NULL,
    session_start DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Barcodes table
CREATE TABLE barcodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    data TEXT NOT NULL,
    symbology VARCHAR(50) NOT NULL,
    timestamp DATETIME NOT NULL,
    FOREIGN KEY (session_id) REFERENCES sessions(id)
);
```

### Enhanced PHP Endpoint with Database

```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Database configuration
$host = 'localhost';
$dbname = 'barcode_data';
$username = 'your_db_user';
$password = 'your_db_password';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit();
}

// Get and validate JSON data
$input = file_get_contents('php://input');
$data = json_decode($input, true);

if (!$data || !isset($data['barcodes']) || !isset($data['session_start'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid data format']);
    exit();
}

try {
    // Connect to database
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Convert ISO 8601 timestamp to MySQL format
    $session_start = date('Y-m-d H:i:s', strtotime($data['session_start']));
    $device_info = $data['device_info'] ?? 'Unknown Device';

    // Insert session
    $stmt = $pdo->prepare("INSERT INTO sessions (device_info, session_start) VALUES (?, ?)");
    $stmt->execute([$device_info, $session_start]);
    $session_id = $pdo->lastInsertId();

    // Insert barcodes
    $stmt = $pdo->prepare("INSERT INTO barcodes (session_id, data, symbology, timestamp) VALUES (?, ?, ?, ?)");

    $barcode_count = 0;
    foreach ($data['barcodes'] as $barcode) {
        $timestamp = date('Y-m-d H:i:s', strtotime($barcode['timestamp']));
        $stmt->execute([
            $session_id,
            $barcode['data'],
            $barcode['symbology'],
            $timestamp
        ]);
        $barcode_count++;
    }

    // Send success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Data saved successfully',
        'session_id' => $session_id,
        'barcodes_saved' => $barcode_count
    ]);

} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Database error']);
}
?>
```

## 🧪 Testing Your Web Service

### 1. Test with curl
```bash
curl -X POST http://localhost:8080/api/barcodes.php \
  -H "Content-Type: application/json" \
  -d '{
    "session_start": "2024-03-15T14:30:22.123Z",
    "device_info": "Test_Device",
    "barcodes": [
      {
        "data": "123456789",
        "symbology": "CODE128",
        "timestamp": "2024-03-15T14:30:25.456Z"
      }
    ]
  }'
```

### 2. Check Server Logs
```bash
# View PHP error log
tail -f /var/log/php_errors.log

# View Apache access log
tail -f /var/log/apache2/access.log
```

### 3. Test with Android App
1. Configure the Android app with your endpoint URL
2. Scan some barcodes
3. Upload the session
4. Check your server logs or database for the received data

## 🔍 Troubleshooting

### Common Issues

**Android app shows "Upload Failed":**
- Check that your web service is accessible from the Android device
- Verify the endpoint URL is correct
- Check server logs for error messages
- Ensure the web service returns proper HTTP status codes

**No data in database:**
- Verify database connection credentials
- Check that tables exist and have correct structure
- Look for SQL errors in the PHP error log
- Ensure proper timestamp format conversion

**CORS errors in browser:**
- Add proper CORS headers (already included in examples above)
- Ensure your web server allows the necessary HTTP methods

## 📁 File Structure

For a simple web service, organize your files like this:

```
your-web-server/
├── api/
│   ├── barcodes.php          # Main endpoint for receiving data
│   ├── config.php            # Database configuration
│   └── database.sql          # Database schema
├── logs/                     # Log files
└── index.html               # Optional: Simple status page
```

## 🎯 Next Steps

Once you have basic data reception working:
- **[Android App Configuration](07-Android-App-Configuration.md)** - Configure the Android app
- **[Docker WMS Deployment](10-Docker-WMS-Deployment.md)** - Use the complete web management system

---

**🎉 Success!** You now have a web service that can receive and process barcode data from the Android application. The service will log all received sessions and store them for further processing or analysis.
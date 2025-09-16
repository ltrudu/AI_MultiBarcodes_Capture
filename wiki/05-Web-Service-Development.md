# Web Service Development Guide - AI MultiBarcode Capture

Comprehensive guide for building custom web services that handle device POST requests and integrate with the barcode capture system.

## 📋 Table of Contents

1. [Web Service Architecture](#web-service-architecture)
2. [REST API Design](#rest-api-design)
3. [Database Integration](#database-integration)
4. [Request Handling](#request-handling)
5. [Response Formats](#response-formats)
6. [Authentication & Security](#authentication--security)
7. [Custom Endpoints](#custom-endpoints)
8. [Integration Examples](#integration-examples)
9. [Testing & Validation](#testing--validation)
10. [Performance Optimization](#performance-optimization)

## 🏗️ Web Service Architecture

### System Overview

The web service acts as the central hub for barcode data processing:

```
┌─────────────────┐    HTTP POST    ┌─────────────────┐    Database    ┌─────────────────┐
│                 │   (JSON Data)   │                 │    Queries     │                 │
│  Android App    │────────────────▶│  Web Service    │───────────────▶│  MySQL Database │
│  (Barcode       │                 │  (PHP/Apache)   │                │  (Sessions &    │
│   Scanner)      │◀────────────────│                 │◀───────────────│   Barcodes)     │
│                 │   Success/Error │                 │    Results     │                 │
└─────────────────┘                 └─────────────────┘                └─────────────────┘
                                              │
                                              │ Real-time Data
                                              ▼
                                    ┌─────────────────┐
                                    │                 │
                                    │  Web Interface  │
                                    │  (Management    │
                                    │   Dashboard)    │
                                    │                 │
                                    └─────────────────┘
```

### Core Components

1. **API Endpoint Handler** (`barcodes.php`)
2. **Database Abstraction Layer** (`database.php`)
3. **Request Validation & Processing**
4. **Response Generation & Error Handling**
5. **Real-time Data Management**

## 🔌 REST API Design

### Base API Structure

The API follows RESTful principles with these core endpoints:

| Method | Endpoint | Purpose | Request Body |
|--------|----------|---------|--------------|
| `POST` | `/api/barcodes.php` | Create new barcode session | JSON session data |
| `GET` | `/api/barcodes.php` | List all sessions | None |
| `GET` | `/api/barcodes.php?session_id=X` | Get session details | None |
| `PUT` | `/api/barcodes.php?barcode_id=X` | Update barcode status | JSON update data |
| `DELETE` | `/api/barcodes.php?reset=all` | Reset all data | None |

### API Implementation

#### Main API Handler Class

```php
<?php
// /src/api/barcodes.php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once '../config/database.php';

class BarcodeAPI {
    private $db;
    private $connection;

    public function __construct() {
        $this->db = new Database();
        $this->connection = $this->db->getConnection();
    }

    public function handleRequest() {
        $method = $_SERVER['REQUEST_METHOD'];

        try {
            switch ($method) {
                case 'POST':
                    $this->receiveBarcodeCaptureSession();
                    break;
                case 'GET':
                    if (isset($_GET['session_id'])) {
                        $this->getSessionDetails($_GET['session_id']);
                    } else {
                        $this->getAllSessions();
                    }
                    break;
                case 'PUT':
                    if (isset($_GET['barcode_id'])) {
                        $this->updateBarcodeStatus($_GET['barcode_id']);
                    }
                    break;
                case 'DELETE':
                    if (isset($_GET['reset']) && $_GET['reset'] === 'all') {
                        $this->resetAllData();
                    } else {
                        $this->sendError('Invalid DELETE request', 400);
                    }
                    break;
                default:
                    $this->sendError('Method not allowed', 405);
                    break;
            }
        } catch (Exception $e) {
            $this->sendError($e->getMessage(), 500);
        }
    }
}

// Initialize and handle the request
$api = new BarcodeAPI();
$api->handleRequest();
?>
```

### Request Processing Methods

#### POST - Receive Barcode Session

```php
private function receiveBarcodeCaptureSession() {
    $input = json_decode(file_get_contents('php://input'), true);

    if (!$input || !isset($input['barcodes'])) {
        $this->sendError('Invalid JSON data or missing barcodes array', 400);
        return;
    }

    try {
        // Convert ISO 8601 timestamps to MySQL format
        $session_timestamp = $this->convertTimestamp($input['session_timestamp'] ?? null);
        $device_info = $input['device_info'] ?? 'Unknown Device';
        $total_barcodes = count($input['barcodes']);

        // Create capture session
        $stmt = $this->connection->prepare("
            INSERT INTO capture_sessions (session_timestamp, device_info, total_barcodes)
            VALUES (:session_timestamp, :device_info, :total_barcodes)
        ");

        $stmt->bindParam(':session_timestamp', $session_timestamp);
        $stmt->bindParam(':device_info', $device_info);
        $stmt->bindParam(':total_barcodes', $total_barcodes);
        $stmt->execute();

        $session_id = $this->connection->lastInsertId();

        // Insert individual barcodes
        $barcode_stmt = $this->connection->prepare("
            INSERT INTO barcodes (session_id, value, symbology, symbology_name, quantity, timestamp)
            VALUES (:session_id, :value, :symbology, :symbology_name, :quantity, :timestamp)
        ");

        foreach ($input['barcodes'] as $barcode) {
            if (!isset($barcode['value']) || empty($barcode['value'])) {
                continue;
            }

            $symbology = $barcode['symbology'] ?? 0;
            $symbology_name = $this->getSymbologyName($symbology);
            $quantity = $barcode['quantity'] ?? 1;
            $barcode_timestamp = $this->convertTimestamp($barcode['timestamp'] ?? null);

            $barcode_stmt->bindParam(':session_id', $session_id);
            $barcode_stmt->bindParam(':value', $barcode['value']);
            $barcode_stmt->bindParam(':symbology', $symbology);
            $barcode_stmt->bindParam(':symbology_name', $symbology_name);
            $barcode_stmt->bindParam(':quantity', $quantity);
            $barcode_stmt->bindParam(':timestamp', $barcode_timestamp);
            $barcode_stmt->execute();
        }

        $this->sendResponse([
            'success' => true,
            'message' => 'Barcode capture session received successfully',
            'session_id' => $session_id,
            'total_barcodes' => $total_barcodes
        ]);

    } catch (Exception $e) {
        $this->sendError('Failed to save barcode session: ' . $e->getMessage(), 500);
    }
}
```

#### GET - Retrieve Sessions

```php
private function getAllSessions() {
    try {
        $stmt = $this->connection->prepare("
            SELECT * FROM session_statistics
            ORDER BY created_at DESC
            LIMIT 100
        ");
        $stmt->execute();
        $sessions = $stmt->fetchAll();

        $this->sendResponse([
            'success' => true,
            'sessions' => $sessions,
            'total' => count($sessions)
        ]);
    } catch (Exception $e) {
        $this->sendError('Failed to retrieve sessions: ' . $e->getMessage(), 500);
    }
}

private function getSessionDetails($session_id) {
    try {
        // Get session info
        $session_stmt = $this->connection->prepare("
            SELECT * FROM session_statistics WHERE id = :session_id
        ");
        $session_stmt->bindParam(':session_id', $session_id);
        $session_stmt->execute();
        $session = $session_stmt->fetch();

        if (!$session) {
            $this->sendError('Session not found', 404);
            return;
        }

        // Get barcodes for this session
        $barcodes_stmt = $this->connection->prepare("
            SELECT * FROM barcode_details WHERE session_id = :session_id
            ORDER BY timestamp ASC
        ");
        $barcodes_stmt->bindParam(':session_id', $session_id);
        $barcodes_stmt->execute();
        $barcodes = $barcodes_stmt->fetchAll();

        $this->sendResponse([
            'success' => true,
            'session' => $session,
            'barcodes' => $barcodes
        ]);
    } catch (Exception $e) {
        $this->sendError('Failed to retrieve session details: ' . $e->getMessage(), 500);
    }
}
```

## 🗄️ Database Integration

### Database Connection Class

```php
<?php
// /src/config/database.php
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
                $this->password,
                [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false,
                ]
            );
            $this->conn->exec("set names utf8");
        } catch(PDOException $exception) {
            throw new Exception("Connection error: " . $exception->getMessage());
        }
        return $this->conn;
    }
}
?>
```

### Database Views for Optimization

Create optimized views for common queries:

```sql
-- Session statistics view
CREATE VIEW session_statistics AS
SELECT
    cs.id,
    cs.session_timestamp,
    cs.device_info,
    cs.created_at,
    cs.total_barcodes,
    COUNT(DISTINCT b.symbology) as unique_symbologies,
    MIN(b.timestamp) as first_scan,
    MAX(b.timestamp) as last_scan,
    COALESCE(SUM(b.quantity), 0) as total_quantity,
    COUNT(CASE WHEN b.processed = 1 THEN 1 END) as processed_count,
    COUNT(CASE WHEN b.processed = 0 THEN 1 END) as pending_count
FROM capture_sessions cs
LEFT JOIN barcodes b ON cs.id = b.session_id
GROUP BY cs.id, cs.session_timestamp, cs.device_info, cs.created_at, cs.total_barcodes;

-- Barcode details view
CREATE VIEW barcode_details AS
SELECT
    b.*,
    cs.session_timestamp,
    cs.device_info
FROM barcodes b
JOIN capture_sessions cs ON b.session_id = cs.id;
```

## 📥 Request Handling

### Input Validation

Implement comprehensive input validation:

```php
private function validateBarcodeSession($input) {
    $errors = [];

    // Validate required fields
    if (!isset($input['barcodes']) || !is_array($input['barcodes'])) {
        $errors[] = 'Missing or invalid barcodes array';
    }

    if (empty($input['barcodes'])) {
        $errors[] = 'Barcodes array cannot be empty';
    }

    // Validate each barcode
    foreach ($input['barcodes'] as $index => $barcode) {
        if (!isset($barcode['value']) || empty($barcode['value'])) {
            $errors[] = "Barcode at index $index missing value";
        }

        if (isset($barcode['symbology']) && !is_numeric($barcode['symbology'])) {
            $errors[] = "Barcode at index $index has invalid symbology";
        }

        if (isset($barcode['quantity']) && (!is_numeric($barcode['quantity']) || $barcode['quantity'] < 1)) {
            $errors[] = "Barcode at index $index has invalid quantity";
        }
    }

    // Validate timestamps
    if (isset($input['session_timestamp']) && !$this->isValidTimestamp($input['session_timestamp'])) {
        $errors[] = 'Invalid session timestamp format';
    }

    return $errors;
}

private function isValidTimestamp($timestamp) {
    if (empty($timestamp)) return true; // Optional field

    // Check ISO 8601 format
    $date = DateTime::createFromFormat(DateTime::ATOM, $timestamp);
    return $date !== false;
}
```

### Timestamp Conversion

Handle different timestamp formats:

```php
private function convertTimestamp($timestamp) {
    if (!$timestamp) {
        return date('Y-m-d H:i:s');
    }

    try {
        // Handle ISO 8601 format from Android (2025-09-16T19:01:54.978Z)
        if (strpos($timestamp, 'T') !== false) {
            $datetime = new DateTime($timestamp);
            return $datetime->format('Y-m-d H:i:s');
        }

        // If already in MySQL format, return as-is
        if (preg_match('/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/', $timestamp)) {
            return $timestamp;
        }

        // Try to parse other formats
        $datetime = new DateTime($timestamp);
        return $datetime->format('Y-m-d H:i:s');
    } catch (Exception $e) {
        // If conversion fails, return current timestamp
        return date('Y-m-d H:i:s');
    }
}
```

## 📤 Response Formats

### Standard Response Structure

All API responses follow this consistent format:

```php
// Success Response
{
    "success": true,
    "message": "Operation completed successfully",
    "data": {
        // Response data here
    }
}

// Error Response
{
    "success": false,
    "error": "Error description",
    "error_code": "ERROR_CODE",
    "details": {
        // Additional error details
    }
}
```

### Response Helper Methods

```php
private function sendResponse($data, $status_code = 200) {
    http_response_code($status_code);
    echo json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    exit();
}

private function sendError($message, $status_code = 400) {
    http_response_code($status_code);
    echo json_encode([
        'success' => false,
        'error' => $message,
        'timestamp' => date('c'),
        'request_id' => uniqid()
    ], JSON_PRETTY_PRINT);
    exit();
}

private function sendValidationError($errors) {
    $this->sendError('Validation failed', 422, [
        'validation_errors' => $errors
    ]);
}
```

## 🔐 Authentication & Security

### Basic Authentication Implementation

```php
private function validateAuthentication() {
    if (!isset($_SERVER['HTTP_AUTHORIZATION'])) {
        $this->sendError('Authentication required', 401);
        return false;
    }

    $auth_header = $_SERVER['HTTP_AUTHORIZATION'];
    if (!preg_match('/Basic\s+(.*)$/i', $auth_header, $matches)) {
        $this->sendError('Invalid authentication format', 401);
        return false;
    }

    $credentials = base64_decode($matches[1]);
    list($username, $password) = explode(':', $credentials, 2);

    // Validate credentials against database or config
    if (!$this->verifyCredentials($username, $password)) {
        $this->sendError('Invalid credentials', 401);
        return false;
    }

    return true;
}

private function verifyCredentials($username, $password) {
    // Implementation depends on your authentication system
    $valid_users = [
        'api_user' => password_hash('secure_password', PASSWORD_DEFAULT)
    ];

    return isset($valid_users[$username]) &&
           password_verify($password, $valid_users[$username]);
}
```

### Request Rate Limiting

```php
class RateLimiter {
    private $redis;
    private $max_requests;
    private $time_window;

    public function __construct($max_requests = 100, $time_window = 3600) {
        $this->redis = new Redis();
        $this->redis->connect('redis-server', 6379);
        $this->max_requests = $max_requests;
        $this->time_window = $time_window;
    }

    public function isAllowed($identifier) {
        $key = "rate_limit:$identifier";
        $current = $this->redis->get($key);

        if ($current === false) {
            $this->redis->setex($key, $this->time_window, 1);
            return true;
        }

        if ($current >= $this->max_requests) {
            return false;
        }

        $this->redis->incr($key);
        return true;
    }
}

// Usage in API
$rate_limiter = new RateLimiter();
$client_ip = $_SERVER['REMOTE_ADDR'];

if (!$rate_limiter->isAllowed($client_ip)) {
    $this->sendError('Rate limit exceeded', 429);
}
```

## 🔗 Custom Endpoints

### Adding Custom Functionality

Create additional endpoints for specific business logic:

```php
// /src/api/custom.php
class CustomAPI extends BarcodeAPI {

    public function handleCustomRequest() {
        $action = $_GET['action'] ?? '';

        switch ($action) {
            case 'export':
                $this->exportSessionData();
                break;
            case 'stats':
                $this->getSystemStatistics();
                break;
            case 'bulk_update':
                $this->bulkUpdateBarcodes();
                break;
            default:
                $this->sendError('Unknown action', 400);
        }
    }

    private function exportSessionData() {
        $format = $_GET['format'] ?? 'json';
        $session_id = $_GET['session_id'] ?? null;

        if (!$session_id) {
            $this->sendError('Session ID required', 400);
            return;
        }

        $session_data = $this->getSessionWithBarcodes($session_id);

        switch ($format) {
            case 'csv':
                $this->exportAsCSV($session_data);
                break;
            case 'xml':
                $this->exportAsXML($session_data);
                break;
            default:
                $this->sendResponse($session_data);
        }
    }

    private function exportAsCSV($data) {
        header('Content-Type: text/csv');
        header('Content-Disposition: attachment; filename="session_' . $data['session']['id'] . '.csv"');

        $output = fopen('php://output', 'w');

        // CSV headers
        fputcsv($output, ['Session ID', 'Device', 'Barcode Value', 'Symbology', 'Quantity', 'Timestamp']);

        // CSV data
        foreach ($data['barcodes'] as $barcode) {
            fputcsv($output, [
                $data['session']['id'],
                $data['session']['device_info'],
                $barcode['value'],
                $barcode['symbology_name'],
                $barcode['quantity'],
                $barcode['timestamp']
            ]);
        }

        fclose($output);
        exit();
    }
}
```

### Webhook Integration

Add webhook support for real-time notifications:

```php
private function triggerWebhooks($event_type, $data) {
    $webhooks = $this->getConfiguredWebhooks();

    foreach ($webhooks as $webhook) {
        if (in_array($event_type, $webhook['events'])) {
            $this->sendWebhook($webhook['url'], $event_type, $data);
        }
    }
}

private function sendWebhook($url, $event_type, $data) {
    $payload = [
        'event' => $event_type,
        'timestamp' => date('c'),
        'data' => $data
    ];

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'X-Webhook-Signature: ' . $this->generateSignature($payload)
    ]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);

    $response = curl_exec($ch);
    $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    // Log webhook result
    error_log("Webhook sent to $url: HTTP $http_code");
}
```

## 🧪 Testing & Validation

### Unit Testing with PHPUnit

```php
<?php
// tests/BarcodeAPITest.php
use PHPUnit\Framework\TestCase;

class BarcodeAPITest extends TestCase {
    private $api;
    private $test_db;

    protected function setUp(): void {
        // Setup test database
        $this->test_db = new TestDatabase();
        $this->api = new BarcodeAPI($this->test_db);
    }

    public function testCreateBarcodeSession() {
        $test_data = [
            'barcodes' => [
                [
                    'value' => 'TEST123',
                    'symbology' => 1,
                    'quantity' => 1,
                    'timestamp' => '2025-09-16T19:01:54.978Z'
                ]
            ],
            'session_timestamp' => '2025-09-16T19:01:54.978Z',
            'device_info' => 'TestDevice'
        ];

        $response = $this->api->receiveBarcodeCaptureSession($test_data);

        $this->assertTrue($response['success']);
        $this->assertArrayHasKey('session_id', $response);
        $this->assertEquals(1, $response['total_barcodes']);
    }

    public function testInvalidBarcodeData() {
        $invalid_data = [
            'barcodes' => [] // Empty array should fail
        ];

        $this->expectException(ValidationException::class);
        $this->api->receiveBarcodeCaptureSession($invalid_data);
    }
}
```

### API Testing Script

```bash
#!/bin/bash
# tests/api_test.sh

API_BASE="http://localhost:8080/api"

echo "Testing API endpoints..."

# Test POST - Create session
echo "1. Testing POST /barcodes.php"
curl -X POST "$API_BASE/barcodes.php" \
     -H "Content-Type: application/json" \
     -d '{
       "barcodes": [
         {
           "value": "TEST123",
           "symbology": 1,
           "quantity": 1,
           "timestamp": "2025-09-16T19:01:54.978Z"
         }
       ],
       "session_timestamp": "2025-09-16T19:01:54.978Z",
       "device_info": "TestDevice"
     }'

echo -e "\n2. Testing GET /barcodes.php"
curl -X GET "$API_BASE/barcodes.php"

echo -e "\n3. Testing GET /barcodes.php?session_id=1"
curl -X GET "$API_BASE/barcodes.php?session_id=1"

echo -e "\n4. Testing invalid request"
curl -X POST "$API_BASE/barcodes.php" \
     -H "Content-Type: application/json" \
     -d '{"invalid": "data"}'

echo -e "\nAPI tests completed."
```

## ⚡ Performance Optimization

### Database Query Optimization

```php
// Use prepared statements for repeated queries
private $prepared_statements = [];

private function getPreparedStatement($query) {
    $hash = md5($query);

    if (!isset($this->prepared_statements[$hash])) {
        $this->prepared_statements[$hash] = $this->connection->prepare($query);
    }

    return $this->prepared_statements[$hash];
}

// Batch insert for multiple barcodes
private function batchInsertBarcodes($session_id, $barcodes) {
    $values = [];
    $params = [];

    foreach ($barcodes as $index => $barcode) {
        $values[] = "(:session_id$index, :value$index, :symbology$index, :symbology_name$index, :quantity$index, :timestamp$index)";
        $params[":session_id$index"] = $session_id;
        $params[":value$index"] = $barcode['value'];
        $params[":symbology$index"] = $barcode['symbology'] ?? 0;
        $params[":symbology_name$index"] = $this->getSymbologyName($barcode['symbology'] ?? 0);
        $params[":quantity$index"] = $barcode['quantity'] ?? 1;
        $params[":timestamp$index"] = $this->convertTimestamp($barcode['timestamp'] ?? null);
    }

    $query = "INSERT INTO barcodes (session_id, value, symbology, symbology_name, quantity, timestamp) VALUES " . implode(', ', $values);
    $stmt = $this->connection->prepare($query);
    $stmt->execute($params);
}
```

### Caching Implementation

```php
class APICache {
    private $redis;
    private $default_ttl = 300; // 5 minutes

    public function __construct() {
        $this->redis = new Redis();
        $this->redis->connect('redis-server', 6379);
    }

    public function get($key) {
        $data = $this->redis->get($key);
        return $data ? json_decode($data, true) : null;
    }

    public function set($key, $data, $ttl = null) {
        $ttl = $ttl ?? $this->default_ttl;
        $this->redis->setex($key, $ttl, json_encode($data));
    }

    public function invalidate($pattern) {
        $keys = $this->redis->keys($pattern);
        if (!empty($keys)) {
            $this->redis->del($keys);
        }
    }
}

// Usage in API methods
private function getAllSessionsCached() {
    $cache_key = 'sessions:all';
    $cached_data = $this->cache->get($cache_key);

    if ($cached_data) {
        return $cached_data;
    }

    $sessions = $this->getAllSessionsFromDB();
    $this->cache->set($cache_key, $sessions, 60); // Cache for 1 minute

    return $sessions;
}
```

---

**🚀 This guide provides everything needed to build robust, scalable web services for the AI MultiBarcode Capture system.** The modular architecture supports both simple deployments and complex enterprise integrations while maintaining performance and security standards.
# API Documentation - AI MultiBarcode Capture

Complete REST API reference for the AI MultiBarcode Capture system, including all endpoints, request/response formats, and integration examples.

## 📋 Table of Contents

1. [API Overview](#api-overview)
2. [Authentication](#authentication)
3. [Endpoints Reference](#endpoints-reference)
4. [Request/Response Formats](#requestresponse-formats)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)
7. [Integration Examples](#integration-examples)
8. [SDKs and Client Libraries](#sdks-and-client-libraries)

## 🌐 API Overview

### Base URL
```
Production: https://your-domain.com/api
Development: http://localhost:8080/api
```

### API Version
Current version: `v1` (included in all endpoints)

### Content Type
All requests and responses use JSON format:
```
Content-Type: application/json
```

### HTTP Methods
| Method | Usage |
|--------|-------|
| `GET` | Retrieve data |
| `POST` | Create new resources |
| `PUT` | Update existing resources |
| `DELETE` | Delete resources |
| `OPTIONS` | CORS preflight requests |

## 🔐 Authentication

### Authentication Methods

The API supports multiple authentication methods:

#### 1. No Authentication (Development)
For development and testing, authentication can be disabled.

#### 2. Basic Authentication
```http
Authorization: Basic base64(username:password)
```

#### 3. API Key Authentication
```http
X-API-Key: your-api-key-here
```

#### 4. Bearer Token Authentication
```http
Authorization: Bearer your-jwt-token-here
```

### Authentication Configuration

Authentication is configured in the Android app:

```java
// In Android app settings
private void addAuthenticationHeaders(HttpURLConnection connection) {
    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    boolean authEnabled = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_HTTPS_AUTHENTICATION, false);

    if (authEnabled) {
        // Load credentials from secure storage
        String username = KeystoreHelper.getFromKeystore(this, "api_username");
        String password = KeystoreHelper.getFromKeystore(this, "api_password");

        if (username != null && password != null) {
            String credentials = username + ":" + password;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        }
    }
}
```

## 📚 Endpoints Reference

### 1. Create Barcode Session

**POST** `/api/barcodes.php`

Creates a new barcode capture session with multiple barcodes.

#### Request Body
```json
{
  "barcodes": [
    {
      "value": "7141093114465",
      "symbology": 1,
      "quantity": 1,
      "timestamp": "2025-09-16T19:01:53.269Z"
    },
    {
      "value": "https://qr.westerndigital.com/yTMw2",
      "symbology": 15,
      "quantity": 1,
      "timestamp": "2025-09-16T19:01:53.269Z"
    }
  ],
  "session_timestamp": "2025-09-16T19:01:54.978Z",
  "device_info": "Zebra_TC53_Android14"
}
```

#### Response
```json
{
  "success": true,
  "message": "Barcode capture session received successfully",
  "session_id": "123",
  "total_barcodes": "2"
}
```

#### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `barcodes` | Array | Yes | Array of barcode objects |
| `barcodes[].value` | String | Yes | The scanned barcode value |
| `barcodes[].symbology` | Integer | No | Symbology type ID (default: 0) |
| `barcodes[].quantity` | Integer | No | Quantity scanned (default: 1) |
| `barcodes[].timestamp` | String | No | ISO 8601 timestamp |
| `session_timestamp` | String | No | Session start time (ISO 8601) |
| `device_info` | String | No | Device hostname/identifier |

#### Symbology IDs

| ID | Symbology Name | Description |
|----|----------------|-------------|
| 1 | CODABAR | Codabar barcode |
| 2 | CODE11 | Code 11 barcode |
| 3 | CODE39 | Code 39 barcode |
| 4 | AZTEC | Aztec 2D barcode |
| 5 | CODE93 | Code 93 barcode |
| 6 | CODE128 | Code 128 barcode |
| 12 | EAN_8 | EAN-8 barcode |
| 13 | EAN_13 | EAN-13 barcode |
| 15 | QRCODE | QR Code 2D barcode |
| 36 | GS1_DATABAR | GS1 DataBar barcode |

*For complete list, see [Android App Configuration](07-Android-App-Configuration.md)*

---

### 2. Get All Sessions

**GET** `/api/barcodes.php`

Retrieves a list of all barcode capture sessions.

#### Request Parameters
None required.

#### Optional Query Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `limit` | Integer | Maximum number of sessions (default: 100) |
| `offset` | Integer | Pagination offset (default: 0) |
| `device` | String | Filter by device info |
| `from_date` | String | Filter sessions from date (YYYY-MM-DD) |
| `to_date` | String | Filter sessions to date (YYYY-MM-DD) |

#### Response
```json
{
  "success": true,
  "sessions": [
    {
      "id": 2,
      "session_timestamp": "2025-09-16 19:30:00",
      "device_info": "Zebra_TC53_Android14",
      "created_at": "2025-09-16 17:26:34",
      "total_barcodes": 2,
      "unique_symbologies": 2,
      "first_scan": "2025-09-16 19:30:00",
      "last_scan": "2025-09-16 19:30:05",
      "total_quantity": "3",
      "processed_count": 0,
      "pending_count": 2
    }
  ],
  "total": 1,
  "pagination": {
    "limit": 100,
    "offset": 0,
    "has_more": false
  }
}
```

---

### 3. Get Session Details

**GET** `/api/barcodes.php?session_id={id}`

Retrieves detailed information about a specific session including all barcodes.

#### Request Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `session_id` | Integer | Yes | Session ID to retrieve |

#### Response
```json
{
  "success": true,
  "session": {
    "id": 2,
    "session_timestamp": "2025-09-16 19:30:00",
    "device_info": "Zebra_TC53_Android14",
    "created_at": "2025-09-16 17:26:34",
    "total_barcodes": 2,
    "unique_symbologies": 2,
    "first_scan": "2025-09-16 19:30:00",
    "last_scan": "2025-09-16 19:30:05",
    "total_quantity": "3",
    "processed_count": 0,
    "pending_count": 2
  },
  "barcodes": [
    {
      "id": 3,
      "session_id": 2,
      "value": "7141093114465",
      "symbology": 1,
      "symbology_name": "CODABAR",
      "quantity": 1,
      "timestamp": "2025-09-16 19:30:00",
      "processed": false,
      "notes": null,
      "created_at": "2025-09-16 17:26:34"
    },
    {
      "id": 4,
      "session_id": 2,
      "value": "https://qr.westerndigital.com/yTMw2",
      "symbology": 15,
      "symbology_name": "QRCODE",
      "quantity": 2,
      "timestamp": "2025-09-16 19:30:05",
      "processed": false,
      "notes": null,
      "created_at": "2025-09-16 17:26:34"
    }
  ]
}
```

---

### 4. Update Barcode Status

**PUT** `/api/barcodes.php?barcode_id={id}`

Updates the processing status and notes for a specific barcode.

#### Request Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `barcode_id` | Integer | Yes | Barcode ID to update |

#### Request Body
```json
{
  "processed": true,
  "notes": "Item processed and shipped"
}
```

#### Response
```json
{
  "success": true,
  "message": "Barcode status updated successfully"
}
```

---

### 5. Reset All Data

**DELETE** `/api/barcodes.php?reset=all`

Permanently deletes all barcode sessions and data from the system.

#### Request Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `reset` | String | Yes | Must be exactly "all" |

#### Response
```json
{
  "success": true,
  "message": "All barcode data has been reset successfully",
  "deleted_sessions": 15,
  "deleted_barcodes": 145
}
```

---

### 6. Health Check

**GET** `/api/health.php`

Returns system health status and version information.

#### Response
```json
{
  "success": true,
  "status": "healthy",
  "version": "1.4.0",
  "database": "connected",
  "uptime": "2 days, 14 hours",
  "timestamp": "2025-09-16T17:30:00Z"
}
```

---

### 7. System Statistics

**GET** `/api/stats.php`

Returns aggregate statistics about the system usage.

#### Response
```json
{
  "success": true,
  "statistics": {
    "total_sessions": 150,
    "total_barcodes": 3500,
    "unique_devices": 12,
    "processed_barcodes": 3200,
    "pending_barcodes": 300,
    "top_symbologies": [
      {"name": "QRCODE", "count": 1200},
      {"name": "CODE128", "count": 800},
      {"name": "EAN_13", "count": 600}
    ],
    "active_devices": [
      {"device": "Zebra_TC53_Android14", "last_scan": "2025-09-16T17:25:00Z"},
      {"device": "Samsung_Galaxy_S24_Android14", "last_scan": "2025-09-16T17:20:00Z"}
    ]
  }
}
```

## 📝 Request/Response Formats

### Standard Request Headers
```http
Content-Type: application/json
Accept: application/json
User-Agent: AI-MultiBarcode-Capture/1.4.0
Authorization: Basic base64(credentials)  # If authentication enabled
```

### Standard Response Format

All API responses follow this structure:

#### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2025-09-16T17:30:00Z",
  "request_id": "abc123def456"
}
```

#### Error Response
```json
{
  "success": false,
  "error": "Error description",
  "error_code": "VALIDATION_ERROR",
  "details": {
    "field": "value",
    "validation_errors": ["Field is required"]
  },
  "timestamp": "2025-09-16T17:30:00Z",
  "request_id": "abc123def456"
}
```

### Timestamp Formats

The API accepts and returns timestamps in these formats:

#### Input (from Android)
```json
"2025-09-16T19:01:54.978Z"  // ISO 8601 with milliseconds
"2025-09-16T19:01:54Z"      // ISO 8601 without milliseconds
```

#### Output (to clients)
```json
"2025-09-16 19:01:54"       // MySQL datetime format
"2025-09-16T19:01:54Z"      // ISO 8601 (when requested)
```

## ❌ Error Handling

### HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST requests |
| 204 | No Content | Successful DELETE requests |
| 400 | Bad Request | Invalid request format |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 422 | Unprocessable Entity | Validation errors |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server-side errors |

### Error Codes

| Error Code | Description | Resolution |
|------------|-------------|------------|
| `VALIDATION_ERROR` | Request validation failed | Check request format and required fields |
| `AUTH_REQUIRED` | Authentication required | Provide valid credentials |
| `AUTH_INVALID` | Invalid credentials | Check username/password or API key |
| `RATE_LIMIT` | Too many requests | Reduce request frequency |
| `SESSION_NOT_FOUND` | Session ID not found | Verify session exists |
| `BARCODE_NOT_FOUND` | Barcode ID not found | Verify barcode exists |
| `DATABASE_ERROR` | Database connection issue | Check server status |
| `INVALID_TIMESTAMP` | Timestamp format error | Use ISO 8601 format |

### Error Response Examples

#### Validation Error
```json
{
  "success": false,
  "error": "Validation failed",
  "error_code": "VALIDATION_ERROR",
  "details": {
    "validation_errors": [
      "barcodes array is required",
      "barcodes array cannot be empty",
      "barcode value at index 0 is required"
    ]
  },
  "timestamp": "2025-09-16T17:30:00Z",
  "request_id": "validation_error_123"
}
```

#### Authentication Error
```json
{
  "success": false,
  "error": "Authentication required",
  "error_code": "AUTH_REQUIRED",
  "details": {
    "message": "This endpoint requires authentication"
  },
  "timestamp": "2025-09-16T17:30:00Z",
  "request_id": "auth_error_456"
}
```

## 🚦 Rate Limiting

### Rate Limit Headers

All responses include rate limiting information:

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1632847200
X-RateLimit-Window: 3600
```

### Default Limits

| Client Type | Requests/Hour | Burst Limit |
|-------------|---------------|-------------|
| Unauthenticated | 100 | 10/minute |
| Authenticated | 1000 | 50/minute |
| Premium | 5000 | 100/minute |

### Rate Limit Exceeded Response

```json
{
  "success": false,
  "error": "Rate limit exceeded",
  "error_code": "RATE_LIMIT",
  "details": {
    "limit": 1000,
    "window": 3600,
    "reset_time": "2025-09-16T18:00:00Z"
  },
  "timestamp": "2025-09-16T17:30:00Z"
}
```

## 🔗 Integration Examples

### Android App Integration

The reference Android app implementation:

```java
// Create JSON payload
JsonObject rootObject = new JsonObject();
rootObject.add("barcodes", barcodesArray);
rootObject.addProperty("session_timestamp", iso8601Format.format(new Date()));
rootObject.addProperty("device_info", getDeviceHostname());

// Send HTTP POST request
String jsonData = gson.toJson(rootObject);
boolean success = performHttpPost(endpointUrl, jsonData);

private boolean performHttpPost(String endpointUrl, String jsonData) throws IOException {
    URL url = new URL(endpointUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    // Configure HTTPS if needed
    if (connection instanceof HttpsURLConnection) {
        configureSslForHttps((HttpsURLConnection) connection);
    }

    // Set headers and method
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setConnectTimeout(10000);
    connection.setReadTimeout(10000);
    connection.setDoOutput(true);

    // Add authentication if enabled
    addAuthenticationHeaders(connection);

    // Send data
    try (OutputStream os = connection.getOutputStream()) {
        byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
    }

    // Check response
    int responseCode = connection.getResponseCode();
    return responseCode >= 200 && responseCode < 300;
}
```

### cURL Examples

#### Create Session
```bash
curl -X POST http://localhost:8080/api/barcodes.php \
  -H "Content-Type: application/json" \
  -d '{
    "barcodes": [
      {
        "value": "123456789",
        "symbology": 1,
        "quantity": 1,
        "timestamp": "2025-09-16T19:01:54.978Z"
      }
    ],
    "session_timestamp": "2025-09-16T19:01:54.978Z",
    "device_info": "TestDevice"
  }'
```

#### Get Sessions
```bash
curl -X GET http://localhost:8080/api/barcodes.php
```

#### Get Session Details
```bash
curl -X GET http://localhost:8080/api/barcodes.php?session_id=123
```

#### Update Barcode Status
```bash
curl -X PUT http://localhost:8080/api/barcodes.php?barcode_id=456 \
  -H "Content-Type: application/json" \
  -d '{
    "processed": true,
    "notes": "Item processed successfully"
  }'
```

### JavaScript/Web Integration

```javascript
class BarcodeAPI {
    constructor(baseUrl, apiKey = null) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    async createSession(sessionData) {
        const response = await fetch(`${this.baseUrl}/barcodes.php`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(this.apiKey && { 'X-API-Key': this.apiKey })
            },
            body: JSON.stringify(sessionData)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    async getSessions() {
        const response = await fetch(`${this.baseUrl}/barcodes.php`, {
            headers: {
                ...(this.apiKey && { 'X-API-Key': this.apiKey })
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    async getSessionDetails(sessionId) {
        const response = await fetch(`${this.baseUrl}/barcodes.php?session_id=${sessionId}`, {
            headers: {
                ...(this.apiKey && { 'X-API-Key': this.apiKey })
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }
}

// Usage
const api = new BarcodeAPI('http://localhost:8080/api');

// Create a session
const sessionData = {
    barcodes: [
        {
            value: '123456789',
            symbology: 1,
            quantity: 1,
            timestamp: new Date().toISOString()
        }
    ],
    session_timestamp: new Date().toISOString(),
    device_info: 'WebClient'
};

api.createSession(sessionData)
    .then(result => console.log('Session created:', result))
    .catch(error => console.error('Error:', error));
```

### Python Integration

```python
import requests
import json
from datetime import datetime

class BarcodeAPI:
    def __init__(self, base_url, api_key=None):
        self.base_url = base_url
        self.session = requests.Session()
        if api_key:
            self.session.headers.update({'X-API-Key': api_key})

    def create_session(self, barcodes, device_info='PythonClient'):
        payload = {
            'barcodes': barcodes,
            'session_timestamp': datetime.utcnow().isoformat() + 'Z',
            'device_info': device_info
        }

        response = self.session.post(
            f'{self.base_url}/barcodes.php',
            json=payload
        )
        response.raise_for_status()
        return response.json()

    def get_sessions(self):
        response = self.session.get(f'{self.base_url}/barcodes.php')
        response.raise_for_status()
        return response.json()

    def get_session_details(self, session_id):
        response = self.session.get(
            f'{self.base_url}/barcodes.php',
            params={'session_id': session_id}
        )
        response.raise_for_status()
        return response.json()

# Usage
api = BarcodeAPI('http://localhost:8080/api')

# Create a session
barcodes = [
    {
        'value': '123456789',
        'symbology': 1,
        'quantity': 1,
        'timestamp': datetime.utcnow().isoformat() + 'Z'
    }
]

result = api.create_session(barcodes)
print(f"Session created: {result['session_id']}")
```

## 📦 SDKs and Client Libraries

### Official SDKs

#### Android SDK
Built into the AI MultiBarcode Capture app with complete integration.

#### JavaScript/TypeScript SDK
```bash
npm install @ai-multibarcode/api-client
```

#### Python SDK
```bash
pip install ai-multibarcode-api
```

### Community SDKs

| Language | Repository | Maintainer |
|----------|------------|------------|
| .NET | [github.com/community/dotnet-sdk](https://github.com/community/dotnet-sdk) | Community |
| Go | [github.com/community/go-sdk](https://github.com/community/go-sdk) | Community |
| Ruby | [github.com/community/ruby-sdk](https://github.com/community/ruby-sdk) | Community |

---

**🔌 This API documentation provides everything needed to integrate with the AI MultiBarcode Capture system.** The RESTful design ensures compatibility with any programming language or platform while maintaining simplicity and reliability.
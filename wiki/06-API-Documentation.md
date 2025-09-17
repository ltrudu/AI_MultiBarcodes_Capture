# API Documentation - AI MultiBarcode Capture

This document provides complete API documentation for the AI MultiBarcode Capture Web Management System.

## 🔗 Base URL

All API endpoints are accessible at:
```
http://YOUR_SERVER:3500/api/
```

Example: `http://localhost:3500/api/barcodes.php`

## 📡 Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/barcodes.php` | Receive barcode data from Android app |
| GET | `/api/barcodes.php` | Get all capture sessions |
| GET | `/api/barcodes.php?session_id={id}` | Get specific session details |
| PUT | `/api/barcodes.php?barcode_id={id}` | Update barcode processing status |

## 📤 POST - Receive Barcode Data

**Endpoint:** `POST /api/barcodes.php`

**Description:** Receives barcode capture sessions from the Android application.

### Request Format

```json
{
  "session_timestamp": "2024-01-15T10:30:00.000Z",
  "device_info": "Samsung_Galaxy_S24_Android14",
  "barcodes": [
    {
      "value": "1234567890128",
      "symbology": "EAN13",
      "quantity": 1,
      "timestamp": "2024-01-15T10:30:15.000Z"
    },
    {
      "value": "https://example.com/qr",
      "symbology": "QRCODE",
      "quantity": 1,
      "timestamp": "2024-01-15T10:30:17.000Z"
    }
  ]
}
```

### Request Parameters

- **session_timestamp**: ISO 8601 timestamp when capture session started
- **device_info**: Unique identifier for the Android device (Manufacturer_Model_AndroidVersion)
- **barcodes**: Array of captured barcode objects
  - **value**: The barcode data/content
  - **symbology**: Type of barcode (QRCODE, EAN13, CODE128, etc.)
  - **quantity**: Number of items scanned (always 1 for individual scans)
  - **timestamp**: ISO 8601 timestamp when this barcode was captured

### Response Format

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Barcode capture session received successfully",
  "session_id": 123,
  "total_barcodes": 2
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Invalid JSON data or missing required fields",
  "error_details": "Missing session_timestamp field"
}
```

## 📥 GET - Retrieve All Sessions

**Endpoint:** `GET /api/barcodes.php`

**Description:** Returns a list of all capture sessions with summary statistics.

### Response Format

```json
{
  "success": true,
  "sessions": [
    {
      "id": 123,
      "session_timestamp": "2024-01-15T10:30:00.000Z",
      "device_info": "Samsung_Galaxy_S24_Android14",
      "total_barcodes": 5,
      "processed_barcodes": 3,
      "created_at": "2024-01-15T10:31:00.000Z"
    },
    {
      "id": 122,
      "session_timestamp": "2024-01-15T09:15:00.000Z",
      "device_info": "Zebra_TC58_Android14",
      "total_barcodes": 12,
      "processed_barcodes": 12,
      "created_at": "2024-01-15T09:16:00.000Z"
    }
  ],
  "total_sessions": 2,
  "total_barcodes": 17
}
```

## 📥 GET - Retrieve Session Details

**Endpoint:** `GET /api/barcodes.php?session_id={id}`

**Description:** Returns detailed information about a specific session including all barcodes.

### Parameters

- **session_id**: The ID of the session to retrieve

### Response Format

```json
{
  "success": true,
  "session": {
    "id": 123,
    "session_timestamp": "2024-01-15T10:30:00.000Z",
    "device_info": "Samsung_Galaxy_S24_Android14",
    "total_barcodes": 2,
    "created_at": "2024-01-15T10:31:00.000Z"
  },
  "barcodes": [
    {
      "id": 456,
      "value": "1234567890128",
      "symbology": "EAN13",
      "quantity": 1,
      "timestamp": "2024-01-15T10:30:15.000Z",
      "processed": false,
      "notes": null
    },
    {
      "id": 457,
      "value": "https://example.com/qr",
      "symbology": "QRCODE",
      "quantity": 1,
      "timestamp": "2024-01-15T10:30:17.000Z",
      "processed": true,
      "notes": "Verified and processed"
    }
  ]
}
```

## 🔄 PUT - Update Barcode Status

**Endpoint:** `PUT /api/barcodes.php?barcode_id={id}`

**Description:** Updates the processing status and notes for a specific barcode.

### Parameters

- **barcode_id**: The ID of the barcode to update

### Request Format

```json
{
  "processed": true,
  "notes": "Item received and verified in warehouse"
}
```

### Response Format

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Barcode status updated successfully",
  "barcode_id": 456
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Barcode not found",
  "barcode_id": 456
}
```

## 🗄️ Database Schema

### Tables

#### `capture_sessions`
| Column | Type | Description |
|--------|------|-------------|
| `id` | INT PRIMARY KEY | Auto-incrementing session ID |
| `session_timestamp` | DATETIME | When the capture session occurred |
| `device_info` | VARCHAR(255) | Information about the capturing device |
| `total_barcodes` | INT | Number of barcodes in the session |
| `created_at` | TIMESTAMP | When the session was received by server |
| `updated_at` | TIMESTAMP | Last modification time |

#### `barcodes`
| Column | Type | Description |
|--------|------|-------------|
| `id` | INT PRIMARY KEY | Auto-incrementing barcode ID |
| `session_id` | INT FOREIGN KEY | Reference to capture_sessions |
| `value` | TEXT | Barcode value/data |
| `symbology` | VARCHAR(50) | Barcode type (EAN13, QRCODE, etc.) |
| `quantity` | INT | Quantity scanned |
| `timestamp` | DATETIME | When this barcode was scanned |
| `processed` | BOOLEAN | Processing status (default: false) |
| `notes` | TEXT | Optional processing notes |

#### `symbology_types`
Reference table mapping symbology names to standardized types used by the Android app.

### Database Views

#### `barcode_details`
Combines barcode data with session information for comprehensive reporting.

#### `session_statistics`
Aggregated statistics for each session including counts and processing status.

## 🔧 Testing the API

### Using curl

**Test GET endpoint:**
```bash
curl -X GET "http://localhost:3500/api/barcodes.php"
```

**Test POST endpoint:**
```bash
curl -X POST "http://localhost:3500/api/barcodes.php" \
  -H "Content-Type: application/json" \
  -d '{
    "session_timestamp": "2024-01-01T12:00:00.000Z",
    "device_info": "Test_Device_Android14",
    "barcodes": [
      {
        "value": "TEST123456",
        "symbology": "CODE128",
        "quantity": 1,
        "timestamp": "2024-01-01T12:00:15.000Z"
      }
    ]
  }'
```

**Test PUT endpoint:**
```bash
curl -X PUT "http://localhost:3500/api/barcodes.php?barcode_id=1" \
  -H "Content-Type: application/json" \
  -d '{
    "processed": true,
    "notes": "Testing barcode processing"
  }'
```

### Using JavaScript (Browser)

```javascript
// Fetch all sessions
fetch('http://localhost:3500/api/barcodes.php')
  .then(response => response.json())
  .then(data => console.log('Sessions:', data.sessions));

// Get specific session
fetch('http://localhost:3500/api/barcodes.php?session_id=123')
  .then(response => response.json())
  .then(data => console.log('Session details:', data.session));

// Update barcode status
fetch('http://localhost:3500/api/barcodes.php?barcode_id=456', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    processed: true,
    notes: 'Processed via web interface'
  })
})
.then(response => response.json())
.then(data => console.log('Update result:', data));
```

## 🚨 Error Handling

### Common HTTP Status Codes

- **200 OK**: Request successful
- **400 Bad Request**: Invalid request data or missing required fields
- **404 Not Found**: Requested resource (session/barcode) not found
- **405 Method Not Allowed**: HTTP method not supported for endpoint
- **500 Internal Server Error**: Server-side error (check logs)

### Error Response Format

All error responses follow this format:
```json
{
  "success": false,
  "message": "Human-readable error description",
  "error_code": "OPTIONAL_ERROR_CODE",
  "details": "Additional error information (optional)"
}
```

## 🔒 Authentication

Currently, the API does not require authentication for development and demonstration purposes. For production deployments, consider implementing:

- **API Key Authentication**: Include API key in headers
- **OAuth 2.0**: For more complex authentication needs
- **IP Whitelisting**: Restrict access to specific IP addresses
- **HTTPS**: Always use HTTPS in production

## 📊 Rate Limiting

No rate limiting is currently implemented. For production use, consider:

- Limiting requests per minute per IP address
- Implementing request queuing for high-volume scenarios
- Monitoring API usage patterns

## 🔄 Integration Examples

### Android App Integration

The Android app automatically uses the POST endpoint when configured for HTTP(s) Post mode:

1. **Configure endpoint** in app settings: `http://YOUR_SERVER:3500/api/barcodes.php`
2. **Capture barcodes** using the camera interface
3. **Upload session** - app automatically POSTs data to the endpoint
4. **Verify upload** - check web interface for received data

### Third-Party System Integration

```python
# Python example
import requests
import json

# Get all sessions
response = requests.get('http://localhost:3500/api/barcodes.php')
sessions = response.json()['sessions']

# Process each session
for session in sessions:
    session_id = session['id']

    # Get session details
    details = requests.get(f'http://localhost:3500/api/barcodes.php?session_id={session_id}')
    barcodes = details.json()['barcodes']

    # Mark barcodes as processed
    for barcode in barcodes:
        if not barcode['processed']:
            requests.put(
                f'http://localhost:3500/api/barcodes.php?barcode_id={barcode["id"]}',
                json={'processed': True, 'notes': 'Auto-processed by integration'}
            )
```

---

**📖 Next Steps**: Use this API documentation to integrate the AI MultiBarcode Capture system with your existing warehouse management or inventory systems. The REST API provides full programmatic access to all captured barcode data.
# Barcode WMS - Web Interface

A complete Warehouse Management System (WMS) web interface designed to receive and manage barcode data from the AI MultiBarcode Capture Android application.

## Features

- **REST API Endpoints**: Receive barcode capture sessions via HTTP POST
- **Web Dashboard**: View and manage capture sessions with real-time statistics
- **Database Storage**: MySQL database with proper schema for barcode data
- **Docker Support**: Complete containerized setup with Apache, PHP, and MySQL
- **Responsive Design**: Modern web interface optimized for desktop and mobile
- **Real-time Updates**: Auto-refresh functionality for live monitoring

## Architecture

### Backend Components
- **Apache HTTP Server**: Web server with PHP support
- **PHP 8.2**: Backend API and web application
- **MySQL 8.0**: Database for storing barcode capture data
- **phpMyAdmin**: Database administration interface

### Frontend Components
- **Modern HTML5/CSS3**: Responsive web interface
- **Vanilla JavaScript**: No framework dependencies
- **Zebra Technologies Theme**: Consistent branding and styling

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Available ports: 8080 (Web), 8081 (phpMyAdmin), 3306 (MySQL)

### Installation

1. **Clone and navigate to the WebInterface directory**
   ```bash
   cd WebInterface
   ```

2. **Copy environment configuration**
   ```bash
   cp .env.example .env
   ```

3. **Start the services**
   ```bash
   docker-compose up -d
   ```

4. **Wait for initialization** (about 30-60 seconds for MySQL setup)

5. **Access the applications**
   - **WMS Dashboard**: http://localhost:8080
   - **phpMyAdmin**: http://localhost:8081
   - **MySQL Database**: localhost:3306

### Default Credentials
- **MySQL Root**: root / root_password
- **MySQL WMS User**: wms_user / wms_password
- **phpMyAdmin**: Use root credentials above

## API Documentation

### Receive Barcode Data
**POST** `/api/barcodes.php`

Accepts JSON data from the Android application:

```json
{
  "session_timestamp": "2024-01-15T10:30:00.000Z",
  "device_info": "Android Device Model",
  "barcodes": [
    {
      "value": "1234567890128",
      "symbology": 17,
      "quantity": 1,
      "timestamp": "2024-01-15T10:30:15.000Z"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Barcode capture session received successfully",
  "session_id": 123,
  "total_barcodes": 1
}
```

### Get All Sessions
**GET** `/api/barcodes.php`

Returns list of all capture sessions with statistics.

### Get Session Details
**GET** `/api/barcodes.php?session_id=123`

Returns detailed information about a specific session including all barcodes.

### Update Barcode Status
**PUT** `/api/barcodes.php?barcode_id=456`

Updates the processing status of a specific barcode:

```json
{
  "processed": true,
  "notes": "Item received and verified"
}
```

## Database Schema

### Tables

#### `capture_sessions`
- `id`: Primary key
- `session_timestamp`: When the capture session occurred
- `device_info`: Information about the capturing device
- `total_barcodes`: Number of barcodes in the session
- `created_at`, `updated_at`: Timestamps

#### `barcodes`
- `id`: Primary key
- `session_id`: Foreign key to capture_sessions
- `value`: Barcode value/data
- `symbology`: Numeric symbology type (matches Android app)
- `symbology_name`: Human-readable symbology name
- `quantity`: Quantity scanned
- `timestamp`: When this specific barcode was scanned
- `processed`: Boolean processing status
- `notes`: Optional processing notes

#### `symbology_types`
Reference table mapping symbology IDs to names (matches Android app constants).

### Views

#### `barcode_details`
Combines barcode data with symbology names and session information.

#### `session_statistics`
Aggregated statistics for each session including counts and processing status.

## Configuration

### Environment Variables
- `DB_HOST`: Database hostname (default: db)
- `DB_NAME`: Database name (default: barcode_wms)
- `DB_USER`: Database username (default: wms_user)
- `DB_PASS`: Database password (default: wms_password)

### Port Configuration
Edit `docker-compose.yml` to change port mappings:
- Web interface: Change `8080:80`
- phpMyAdmin: Change `8081:80`
- MySQL: Change `3306:3306`

## Android App Configuration

To connect your Android app to this WMS:

1. **Enable HTTPS Post Mode** in the Android app settings
2. **Set Endpoint URL** to: `http://your-server:8080/api/barcodes.php`
3. **Configure Authentication** (optional) if you implement it
4. **Test Connection** by capturing barcodes in the app

## Development

### File Structure
```
WebInterface/
├── docker-compose.yml      # Docker services configuration
├── Dockerfile             # Web server container definition
├── apache/
│   └── 000-default.conf   # Apache virtual host configuration
├── database/
│   └── init.sql           # Database initialization script
└── src/                   # Web application source
    ├── index.html         # Main web interface
    ├── config/
    │   └── database.php   # Database connection class
    ├── api/
    │   └── barcodes.php   # REST API endpoints
    ├── css/
    │   └── wms-style.css  # Zebra-themed styling
    └── js/
        └── wms-app.js     # Frontend JavaScript application
```

### Adding Features

1. **New API Endpoints**: Add to `src/api/` directory
2. **Database Changes**: Update `database/init.sql` and rebuild
3. **Frontend Features**: Modify `src/js/wms-app.js` and `src/index.html`
4. **Styling**: Update `src/css/wms-style.css`

## Monitoring

### Logs
```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f web
docker-compose logs -f db
```

### Health Checks
- **Web Service**: http://localhost:8080
- **Database**: Check phpMyAdmin at http://localhost:8081
- **API Status**: http://localhost:8080/api/barcodes.php (should return empty sessions list)

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using the port
   netstat -tulpn | grep :8080
   # Change port in docker-compose.yml
   ```

2. **Database Connection Issues**
   ```bash
   # Check database service status
   docker-compose ps db
   # View database logs
   docker-compose logs db
   ```

3. **Permission Issues**
   ```bash
   # Reset file permissions
   docker-compose exec web chown -R www-data:www-data /var/www/html
   ```

4. **Clear Database**
   ```bash
   # Stop services and remove data
   docker-compose down -v
   # Restart fresh
   docker-compose up -d
   ```

## Security Considerations

### Production Deployment
- Change default passwords in `.env`
- Use HTTPS with SSL certificates
- Implement authentication for API endpoints
- Configure firewall rules
- Enable database encryption
- Regular security updates

### Network Security
- Use reverse proxy (nginx) for SSL termination
- Implement rate limiting
- Add IP whitelisting for API endpoints
- Enable audit logging

## Support

For issues related to:
- **Android App**: See main project documentation
- **Database**: Check phpMyAdmin interface
- **API**: Review Docker logs and error responses
- **Web Interface**: Use browser developer tools for debugging

## License

This WMS interface is part of the AI MultiBarcode Capture project and follows the same licensing terms.
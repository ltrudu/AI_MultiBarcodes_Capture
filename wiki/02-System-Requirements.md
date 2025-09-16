# System Requirements - AI MultiBarcode Capture

This document outlines the hardware and software requirements for deploying the AI MultiBarcode Capture system.

## 🖥️ Server/Host Requirements

### Minimum Requirements
- **OS**: Windows 10/11, Linux (Ubuntu 20.04+), macOS (10.15+)
- **RAM**: 4GB minimum, 8GB recommended
- **Storage**: 10GB free space (2GB for Docker images, 8GB for data)
- **CPU**: Dual-core 2.0GHz minimum
- **Network**: 100Mbps Ethernet or WiFi with stable connectivity

### Recommended Requirements
- **OS**: Linux Ubuntu 22.04 LTS or Windows 11 Pro
- **RAM**: 16GB for high-volume environments
- **Storage**: 50GB+ SSD storage for production
- **CPU**: Quad-core 3.0GHz+ for multiple concurrent devices
- **Network**: Gigabit Ethernet for enterprise deployment

## 📱 Android Device Requirements

### Supported Devices
- **Zebra Devices** (Recommended):
  - TC21/TC26 series
  - TC52/TC57 series
  - MC33 series
  - ET40/ET45 series
  - L10 Rugged Tablet series

- **Generic Android Devices**:
  - Any Android device with camera and network connectivity
  - Performance may vary based on camera quality and processing power

### Android Specifications
- **Android Version**: 11+ (API level 30+)
- **Camera**: Rear camera with autofocus capability
- **RAM**: 3GB minimum, 6GB+ recommended for smooth operation
- **Storage**: 500MB free space for app installation
- **Network**: WiFi or cellular data connectivity
- **Permissions**: Camera, Storage, Internet access

## 🐳 Docker Environment

### Docker Requirements
- **Docker Engine**: 24.0+ (latest stable recommended)
- **Docker Compose**: v2.0+ (v2.20+ recommended)
- **Available Ports**: 8080 (Web), 3306 (MySQL), 8081 (phpMyAdmin)

### Container Resource Allocation
```yaml
# Minimum resource allocation
services:
  web:
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  db:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

## 🌐 Network Requirements

### Connectivity
- **Local Network**: All devices must be on the same network segment or have routing configured
- **Firewall**: Ports 8080, 3306, 8081 must be accessible
- **Bandwidth**: Minimum 1Mbps per active scanning device
- **Latency**: <100ms between Android devices and server for optimal performance

### Network Configuration Examples

#### Development Setup
```
Android Device: 192.168.1.100
Docker Host:    192.168.1.101
Endpoint:       http://192.168.1.101:8080/api/barcodes.php
```

#### Production Setup with Load Balancer
```
Android Devices: 10.0.0.0/24 network
Load Balancer:   https://barcode-api.company.com
Backend Servers: 10.0.1.0/24 network
```

## 🛠️ Development Environment

### Android Development
- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **Android SDK**: API 35 (Android 14)
- **Build Tools**: 34.0.0+
- **Gradle**: 8.11.0+
- **Java**: JDK 1.8 (required for Zebra AI Vision SDK compatibility)

### Web Development
- **PHP**: 8.1+ (for container compatibility)
- **MySQL**: 8.0+ (Docker image version)
- **Apache**: 2.4+ (Docker image version)
- **Modern Browser**: Chrome 90+, Firefox 88+, Safari 14+

## 📊 Performance Characteristics

### Barcode Processing Performance
- **Detection Speed**: 30-60 FPS on modern devices
- **Processing Latency**: <100ms per barcode
- **Multi-barcode Capacity**: Up to 10 simultaneous barcodes per frame
- **Upload Speed**: Typical 1-5 seconds for 100 barcode session

### Database Performance
- **Insert Rate**: 1000+ barcodes/second (single device)
- **Concurrent Users**: 50+ Android devices simultaneously
- **Session Storage**: 10,000+ sessions without performance degradation
- **Query Response**: <100ms for typical dashboard operations

### Web Interface Performance
- **Page Load**: <2 seconds initial load
- **Real-time Updates**: 1-second refresh interval
- **Session Display**: <500ms to display 1000 barcodes
- **Export Generation**: <5 seconds for 10,000 barcode Excel export

## 🔒 Security Requirements

### Network Security
- **HTTPS**: Required for production deployments
- **Firewall**: Restrict access to necessary ports only
- **VPN**: Recommended for remote device access
- **Certificate Management**: Valid SSL certificates for public deployment

### Application Security
- **Authentication**: Configurable endpoint authentication
- **Data Encryption**: HTTPS transport encryption
- **Access Control**: IP-based restriction capabilities
- **Audit Logging**: Complete session and device tracking

## 🚀 Scalability Considerations

### Horizontal Scaling
```yaml
# Multi-container production setup
version: '3.8'
services:
  web:
    image: ai-barcode-web
    deploy:
      replicas: 3

  db:
    image: mysql:8.0
    deploy:
      replicas: 1

  load-balancer:
    image: nginx:alpine
    ports:
      - "443:443"
```

### Vertical Scaling Guidelines
- **Small Deployment**: 1-10 devices, 2CPU/4GB RAM
- **Medium Deployment**: 10-50 devices, 4CPU/8GB RAM
- **Large Deployment**: 50+ devices, 8CPU/16GB RAM+

## ✅ Pre-Installation Checklist

### Infrastructure Checklist
- [ ] Docker environment installed and tested
- [ ] Network connectivity between all components verified
- [ ] Required ports (8080, 3306, 8081) available
- [ ] Sufficient disk space allocated (minimum 10GB)
- [ ] Backup strategy planned for production data

### Development Checklist
- [ ] Android Studio installed with latest SDK
- [ ] Zebra AI Vision SDK dependencies accessible
- [ ] Git repository cloned and accessible
- [ ] Development certificates configured (if using HTTPS)
- [ ] Test Android devices prepared with developer options enabled

### Security Checklist
- [ ] SSL certificates obtained for production domains
- [ ] Firewall rules configured and tested
- [ ] Authentication strategy determined
- [ ] Access control policies defined
- [ ] Monitoring and logging strategy implemented

## 🔧 Hardware Recommendations by Use Case

### Small Office/Lab (1-5 devices)
- **Server**: Intel NUC or similar mini-PC
- **RAM**: 8GB DDR4
- **Storage**: 256GB SSD
- **Network**: Standard WiFi router with gigabit ports

### Warehouse/Distribution (10-25 devices)
- **Server**: Dell PowerEdge T340 or similar tower server
- **RAM**: 16GB ECC DDR4
- **Storage**: 1TB SSD + backup storage
- **Network**: Managed switch with PoE, enterprise WiFi access points

### Enterprise/Multi-site (50+ devices)
- **Server**: Rack-mounted server cluster with load balancing
- **RAM**: 32GB+ per server node
- **Storage**: SAN/NAS with redundancy and backup
- **Network**: Enterprise-grade networking with VLANs and redundancy

---

**Next Steps**: Review these requirements against your environment, then proceed to the [Installation Guide](03-Installation-Guide.md) for detailed setup instructions.
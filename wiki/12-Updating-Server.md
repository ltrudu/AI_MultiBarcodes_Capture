# Updating the Server

When new updates are available for the AI MultiBarcode Capture web server, you can update your running system without rebuilding Docker containers or losing your data. This guide covers the complete process from getting the latest code to updating the running server.

## 🔄 Complete Update Process

### Step 1: Get Latest Code from Git

First, update your local repository with the latest changes:

```bash
# Navigate to your project directory
cd path/to/AI_MultiBarcode_Capture

# Fetch the latest changes from the repository
git fetch origin

# Pull the latest changes to your local branch
git pull origin master
```

**Alternative using Git Desktop or other Git clients:**
- Open your Git client
- Select the AI_MultiBarcode_Capture repository
- Click "Fetch origin" or "Pull origin"

### Step 2: Update the Running Server

After pulling the latest code, use the update scripts to apply changes to your running Docker container:

#### For Windows:
```batch
cd WebInterface
update-webserver.bat
```

#### For Linux/macOS:
```bash
cd WebInterface
./update-webserver.sh
```

## 🎯 What the Update Scripts Do

The update scripts perform these operations automatically:

1. **Container Status Check**: Verify the Docker container exists and is running
2. **File Transfer**: Copy all updated website files to the container:
   - Main website files (`index.html`, CSS, JavaScript)
   - API endpoints and backend code
   - Language translation files
   - Configuration files
3. **Permission Setup**: Set proper file permissions for the web server
4. **Service Reload**: Gracefully reload Apache without stopping the service

## 📋 Step-by-Step Example

Here's a complete example of updating your server:

### Windows Example:
```batch
# Navigate to your project
cd C:\Projects\AI_MultiBarcode_Capture

# Get latest updates
git pull origin master

# Update the running server
cd WebInterface
update-webserver.bat
```

### Linux/macOS Example:
```bash
# Navigate to your project
cd ~/Projects/AI_MultiBarcode_Capture

# Get latest updates
git pull origin master

# Update the running server
cd WebInterface
./update-webserver.sh
```

## ⚡ Quick Update Commands

For frequent updates, you can combine the commands:

### Windows (PowerShell):
```powershell
git pull origin master; cd WebInterface; .\update-webserver.bat; cd ..
```

### Linux/macOS:
```bash
git pull origin master && cd WebInterface && ./update-webserver.sh && cd ..
```

## 🔍 Verifying the Update

After running the update script, verify it worked:

1. **Check Script Output**: The script should show "SUCCESS" message
2. **Test Website**: Visit `http://localhost:3500` to ensure it loads
3. **Check Functionality**: Test basic features like viewing sessions
4. **Check Android Connectivity**: Verify the mobile app still connects properly

## 🛠️ Troubleshooting Updates

### Container Not Found
**Error**: "Docker container 'multibarcode-webinterface' does not exist"

**Solution**: Run the initial setup first:
```bash
# Windows
start-services.bat

# Linux/macOS
./start-services.sh
```

### Container Not Running
The script automatically handles this by starting the container, but if it fails:

```bash
# Manually start the container
docker start multibarcode-webinterface

# Check container status
docker ps
```

### Permission Errors
If you get permission errors during file copying:

**Windows**: Run Command Prompt as Administrator
**Linux/macOS**: Ensure your user can run Docker commands without sudo

### Update Script Fails
If the update script encounters errors:

1. **Check Docker Status**: Ensure Docker Desktop is running
2. **Check Container Logs**: `docker logs multibarcode-webinterface`
3. **Manual Container Restart**: Use `update-network-ip.bat/sh` to restart with fresh container
4. **Full Rebuild**: As last resort, stop container and run `start-services.bat/sh`

## 🔄 Update Frequency

### When to Update:
- **New Features**: When new functionality is released
- **Bug Fixes**: When issues are resolved in the repository
- **Security Updates**: When security improvements are available
- **Performance Improvements**: When optimizations are released

### Best Practices:
- **Backup Important Data**: Export important barcode sessions before major updates
- **Test in Development**: If possible, test updates in a development environment first
- **Monitor Logs**: Check container logs after updates for any errors
- **Update During Low Usage**: Perform updates when the system is less active

## 📊 Monitoring After Updates

After updating, monitor the system:

```bash
# Check container status
docker ps

# Monitor container logs
docker logs -f multibarcode-webinterface

# Check disk usage
docker system df

# Check container resource usage
docker stats multibarcode-webinterface
```

## ⚠️ Important Notes

- **Data Preservation**: Updates preserve all your barcode sessions and database data
- **Brief Downtime**: The web interface may be unavailable for 10-30 seconds during the update
- **No Container Rebuild**: This method updates files without recreating the container
- **Database Intact**: MySQL database and all data remain unchanged
- **Settings Preserved**: All configuration settings are maintained

## 🔗 Alternative Update Methods

### Full Container Rebuild (when needed):
If the update scripts don't work or you need a complete refresh:

```bash
# Stop and remove container
docker stop multibarcode-webinterface
docker rm multibarcode-webinterface

# Restart with latest image
./start-services.bat  # Windows
./start-services.sh   # Linux/macOS
```

### Manual File Updates:
For specific file updates, you can manually copy individual files:

```bash
# Example: Update only a specific API file
docker cp src/api/barcodes.php multibarcode-webinterface:/var/www/html/api/
docker exec multibarcode-webinterface service apache2 reload
```

## 📚 Related Documentation

- **[Docker WMS Deployment](10-Docker-WMS-Deployment.md)** - Initial setup and deployment
- **[Managing IP Changes](11-Managing-IP-Changes.md)** - Network configuration updates
- **[Troubleshooting Guide](15-Troubleshooting-Guide.md)** - General troubleshooting help

---

**💡 Tip**: Set up a regular update schedule (weekly or monthly) to keep your system current with the latest improvements and security updates.
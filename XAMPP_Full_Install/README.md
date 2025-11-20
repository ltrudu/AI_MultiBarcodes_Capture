# XAMPP Full Installation Package

This folder contains the pre-configured XAMPP installation archive and automated installation scripts.

## 📦 Files Overview

| File/Folder | Purpose |
|-------------|---------|
| `archive/` | **Subfolder containing XAMPP archive files** |
| `archive/xampp.7z.001`, `.002`, etc. | Split archive parts (download all parts, <10MB each) |
| `install_Xampp_to_C.bat` | **Automated installation to C:\xampp** (auto-joins + extracts + cleanup) |
| `install_Xampp_to_D.bat` | **Automated installation to D:\xampp** (auto-joins + extracts + cleanup) |

---

## 🚀 Quick Start - For End Users

### Option 1: Automated Extraction (Easiest - Windows 10/11)

**For C: drive installation:**
```batch
# Download all files to XAMPP_Full_Install/archive/ folder
# (archive/xampp.7z.001, .002, etc.)
# Then run from XAMPP_Full_Install folder:
install_Xampp_to_C.bat
```

**For D: drive installation:**
```batch
# Download all files to XAMPP_Full_Install/archive/ folder
# (archive/xampp.7z.001, .002, etc.)
# Then run from XAMPP_Full_Install folder:
install_Xampp_to_D.bat
```

The automated scripts will:
- ✅ Automatically join split archives (native Windows commands!)
- ✅ Extract to correct location (C:\xampp or D:\xampp)
- ✅ Verify extraction completed successfully
- ✅ Clean up temporary files (keeps split archives intact)
- ✅ Display next steps

### Option 2: Manual Extraction (Windows 11)

1. Download all files to `XAMPP_Full_Install/archive/` folder
   - `archive/xampp.7z.001`, `.002`, `.003`, etc. (split parts)
2. Right-click `archive/xampp.7z.001` (first part)
3. Select **"Extract All..."**
4. Enter destination: `C:\` (or `D:\`)
5. Click **"Extract"**
6. Windows 11 will automatically use all split parts and create `C:\xampp\` or `D:\xampp\`

### Option 3: Using 7-Zip

1. Download all files to `XAMPP_Full_Install/archive/` folder
2. Right-click `archive/xampp.7z.001` (or `archive/xampp.7z` if full)
3. Select **7-Zip → Extract to "C:\"** (or `D:\`)
4. 7-Zip will automatically use all parts and create the xampp folder

---

## 📋 Requirements

- **Windows 10/11** (recommended) or Windows 7+ with PowerShell
- **NO additional software required** - uses native Windows commands!
- **Disk space**: ~600MB for extraction

---

## 🔍 Archive Contents

The `archive/xampp.7z` archive contains a complete, pre-configured XAMPP installation:

```
xampp\
├── apache\          (Apache 2.4.x web server)
│   ├── bin\
│   ├── conf\
│   └── modules\
├── mysql\           (MySQL 8.x database)
│   ├── bin\
│   └── data\
├── php\             (PHP 8.2.x)
│   ├── php.exe
│   └── php.ini
├── phpMyAdmin\      (Database management UI)
└── htdocs\          (Web root - initially empty)
```

**Total Size**: ~450MB compressed, ~1.2GB extracted

---

## ✅ Verification After Extraction

After extraction, verify these key files exist:

**For C:\xampp:**
```batch
dir C:\xampp\apache\bin\httpd.exe
dir C:\xampp\mysql\bin\mysqld.exe
dir C:\xampp\php\php.exe
dir C:\xampp\xampp-control.exe
```

**For D:\xampp:**
```batch
dir D:\xampp\apache\bin\httpd.exe
dir D:\xampp\mysql\bin\mysqld.exe
dir D:\xampp\php\php.exe
dir D:\xampp\xampp-control.exe
```

If all files exist, extraction was successful! ✅

---

## 🐛 Troubleshooting

### "Archive parts not found"
**Solution**: Download ALL files to `XAMPP_Full_Install/archive/` folder
- Expected: `archive/xampp.7z.001`, `archive/xampp.7z.002`, `archive/xampp.7z.003`, etc.

### Scripts fail to run
**Solution**:
- Ensure PowerShell is available (Windows 7+ has it by default)
- Check PowerShell version: `powershell -command "$PSVersionTable"`
- Or use the automated installation scripts (install_Xampp_to_C.bat or install_Xampp_to_D.bat)

### Extraction fails with automated scripts
**Solution**: Use manual extraction:
1. Right-click `archive/xampp.7z.001`
2. Select "Extract All..."
3. Destination: `C:\` or `D:\`
4. Windows will use all parts automatically

### "Access Denied" during installation
**Solution**:
- Try install_Xampp_to_D.bat instead of install_Xampp_to_C.bat
- Or run the batch file as administrator (right-click → Run as administrator)

### Files missing after extraction
**Solution**:
- Re-download all archive parts
- Ensure no antivirus interference
- Verify you have enough disk space (~1.5GB)

---

## 📖 Next Steps After Extraction

1. ✅ Verify XAMPP extracted successfully
2. ✅ Navigate to AI MultiBarcode Capture project
3. ✅ Run: `cd WebInterface`
4. ✅ Run: `xampp_start_server.bat`
5. ✅ Access web interface at: http://localhost:3500

The `xampp_start_server.bat` script automatically detects whether XAMPP is in C:\xampp or D:\xampp!

---

## 📚 Documentation

For complete installation instructions, see:
- **[XAMPP Installation Guide](../wiki/04-Installation-Guide-XAMPP.md)**
- **[Quick Start Guide](../wiki/01-Quick-Start-Guide.md)**

---

## 💡 Windows 11 Extraction Methods

### Method 1: Built-in Extractor (GUI)
- Right-click → "Extract All..."
- No additional software needed
- Works with split archives automatically

### Method 2: PowerShell + tar (Command Line)
```powershell
tar -xf xampp.7z.001 -C C:\
```

### Method 3: PowerShell COM (Fallback)
```powershell
$shell = New-Object -ComObject Shell.Application
$zip = $shell.NameSpace("$PWD\xampp.7z.001")
$dest = $shell.NameSpace("C:\")
$dest.CopyHere($zip.Items(), 16)
```

### Method 4: Automated Scripts (Recommended)
```batch
install_Xampp_to_C.bat    # Automatically joins, extracts, and cleans up
```

**Note**: All scripts automatically look for the archive in the `archive/` subfolder!

---

## 🔐 Security Note

This XAMPP installation is pre-configured for development/testing purposes:
- MySQL root account has **no password**
- Change passwords before production use
- See security hardening guide in wiki

---

## 📞 Support

For issues or questions:
- **GitHub Issues**: https://github.com/ltrudu/AI_MutliBarcodes_Capture/issues
- **Wiki Documentation**: [Complete Installation Guide](../wiki/04-Installation-Guide-XAMPP.md)

---

**Note**: This package is designed for the AI MultiBarcode Capture system. For general XAMPP installation, visit https://www.apachefriends.org/

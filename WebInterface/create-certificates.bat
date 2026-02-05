@echo off
setlocal enabledelayedexpansion

echo ======================================
echo WMS SSL Certificate Generation Script
echo ======================================

REM Check if OpenSSL is available
where openssl >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: OpenSSL is not installed or not in PATH
    echo Please install OpenSSL and add it to your PATH
    echo You can download it from: https://slproweb.com/products/Win32OpenSSL.html
    exit /b 1
)

REM Check if Java keytool is available (for Android certificates)
where keytool >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java keytool is not installed or not in PATH
    echo Please install Java JDK and add it to your PATH
    exit /b 1
)

REM Load configuration from certificates.conf
if not exist "certificates.conf" (
    echo ERROR: certificates.conf file not found
    echo Please ensure certificates.conf exists in the same directory
    exit /b 1
)

REM Parse configuration file
for /f "tokens=1,2 delims==" %%a in ('type certificates.conf ^| findstr /v "^#" ^| findstr /v "^$"') do (
    set "%%a=%%b"
)

echo Configuration loaded from certificates.conf

REM Get the directory where the script is located
set "SCRIPT_DIR=%~dp0"

REM Create SSL directory if it doesn't exist (relative to script directory)
set "SSL_PATH=%SCRIPT_DIR%%SSL_DIR%"
if not exist "%SSL_PATH%" (
    echo Creating SSL directory: %SSL_PATH%
    mkdir "%SSL_PATH%"
)

REM Change to SSL directory
cd /d "%SSL_PATH%"
echo Working in directory: %CD%

REM Remove existing certificates if they exist
echo Cleaning up existing certificates...
if exist "%CA_KEY_FILE%" del /f "%CA_KEY_FILE%"
if exist "%CA_CERT_FILE%" del /f "%CA_CERT_FILE%"
if exist "%WMS_KEY_FILE%" del /f "%WMS_KEY_FILE%"
if exist "%WMS_CSR_FILE%" del /f "%WMS_CSR_FILE%"
if exist "%WMS_CERT_FILE%" del /f "%WMS_CERT_FILE%"
if exist "wms.conf" del /f "wms.conf"
if exist "android_ca_system.pem" del /f "android_ca_system.pem"
if exist "*.0" del /f "*.0"
if exist "wms_chain.crt" del /f "wms_chain.crt"

echo ======================================
echo Step 1: Creating Certificate Authority (CA)
echo ======================================

REM Create CA private key
echo Creating CA private key...
openssl genrsa -aes256 -passout pass:%CA_PASSWORD% -out "%CA_KEY_FILE%" %KEY_SIZE%
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create CA private key
    exit /b 1
)

REM Create CA certificate
echo Creating CA certificate...
openssl req -new -x509 -days %CA_VALIDITY_DAYS% -key "%CA_KEY_FILE%" -passin pass:%CA_PASSWORD% -out "%CA_CERT_FILE%" -subj "/C=US/ST=NewYork/L=NewYork/O=WMSRootCA/OU=CertificateAuthority/CN=WMSRootCA/emailAddress=admin@wms.local"

if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create CA certificate
    exit /b 1
)

echo ======================================
echo Step 2: Creating WMS Server Certificate
echo ======================================

REM Create WMS server private key
echo Creating WMS server private key...
openssl genrsa -aes256 -passout pass:%WMS_PASSWORD% -out "%WMS_KEY_FILE%" %KEY_SIZE%
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create WMS server private key
    exit /b 1
)

REM Create WMS server configuration file
echo Creating WMS server configuration...
(
echo [req]
echo distinguished_name = req_distinguished_name
echo req_extensions = v3_req
echo prompt = no
echo.
echo [req_distinguished_name]
echo C = %WMS_COUNTRY%
echo ST = %WMS_STATE%
echo L = %WMS_CITY%
echo O = %WMS_ORGANIZATION%
echo OU = %WMS_ORGANIZATIONAL_UNIT%
echo CN = %WMS_COMMON_NAME%
echo emailAddress = %WMS_EMAIL%
echo.
echo [v3_req]
echo keyUsage = %SERVER_KEY_USAGE%
echo extendedKeyUsage = %SERVER_EXT_KEY_USAGE%
echo subjectAltName = @alt_names
echo.
echo [alt_names]
echo DNS.1 = %WMS_SAN_DNS1%
echo DNS.2 = %WMS_SAN_DNS2%
echo DNS.3 = %WMS_SAN_DNS3%
echo IP.1 = %WMS_SAN_IP1%
echo IP.2 = %WMS_SAN_IP2%
echo IP.3 = %WMS_SAN_IP3%
if defined WMS_SAN_IP4 echo IP.4 = %WMS_SAN_IP4%
) > wms.conf

REM Create WMS server certificate signing request
echo Creating WMS server CSR...
openssl req -new -key "%WMS_KEY_FILE%" -passin pass:%WMS_PASSWORD% -out "%WMS_CSR_FILE%" -config wms.conf
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create WMS server CSR
    exit /b 1
)

REM Sign WMS server certificate with CA
echo Signing WMS server certificate...
openssl x509 -req -in "%WMS_CSR_FILE%" -CA "%CA_CERT_FILE%" -CAkey "%CA_KEY_FILE%" -passin pass:%CA_PASSWORD% -CAcreateserial -out "%WMS_CERT_FILE%" -days %SERVER_VALIDITY_DAYS% -extensions v3_req -extfile wms.conf
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to sign WMS server certificate
    exit /b 1
)

REM Remove passphrase from WMS private key for Apache
echo Removing passphrase from WMS private key...
openssl rsa -in "%WMS_KEY_FILE%" -passin pass:%WMS_PASSWORD% -out "%WMS_KEY_FILE%.unencrypted"
move "%WMS_KEY_FILE%.unencrypted" "%WMS_KEY_FILE%"

echo ======================================
echo Step 3: Creating Android System Certificate
echo ======================================

REM Create Android system certificate (PEM format CA certificate)
echo Creating Android system certificate...
echo Converting CA certificate to PEM format for Android system installation...

REM Copy CA certificate to Android-specific name
copy "%CA_CERT_FILE%" android_ca_system.pem
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create Android system certificate
    exit /b 1
)

REM Create certificate with hash-based filename for Android system installation
echo Creating certificate with Android system naming convention...
for /f %%i in ('openssl x509 -noout -hash -in "%CA_CERT_FILE%"') do set CERT_HASH=%%i
copy "%CA_CERT_FILE%" "%CERT_HASH%.0"
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to create hashed certificate file
    exit /b 1
)

echo Android system certificate created: %CERT_HASH%.0

echo ======================================
echo Step 4: Creating Certificate Chain Files
echo ======================================

REM Create full certificate chain file (server cert + CA cert)
echo Creating certificate chain for WMS...
copy "%WMS_CERT_FILE%" + "%CA_CERT_FILE%" wms_chain.crt

echo ======================================
echo Certificate Generation Complete!
echo ======================================
echo.
echo Generated files:
echo - %CA_CERT_FILE% (Certificate Authority certificate)
echo - %WMS_CERT_FILE% (WMS server certificate)
echo - %WMS_KEY_FILE% (WMS server private key)
echo - android_ca_system.pem (Android system CA certificate in PEM format)
echo - %CERT_HASH%.0 (Android system CA certificate with hash filename)
echo - wms_chain.crt (WMS certificate chain)
echo.
echo For Android system-wide certificate installation:
echo 1. Transfer android_ca_system.pem to your Android device
echo 2. Go to Settings ^> Security ^> Encryption ^& Credentials ^> Install from storage
echo 3. Select android_ca_system.pem and install as "CA certificate"
echo 4. Or use the hashed filename %CERT_HASH%.0 for direct system installation (root required)
echo.
echo For Apache configuration:
echo 1. Update SSL certificate paths in your Apache config
echo 2. Use %WMS_CERT_FILE% as SSLCertificateFile
echo 3. Use %WMS_KEY_FILE% as SSLCertificateKeyFile
echo.
echo Certificate generation process completed successfully!
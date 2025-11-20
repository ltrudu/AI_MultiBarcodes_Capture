@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo XAMPP Automated Installer - Install to D:\xampp
echo =========================================================
echo.

REM Check if D: drive exists
if not exist "D:\" (
    echo [ERROR] D: drive not found!
    echo Please use install_Xampp_to_C.bat instead.
    pause
    exit /b 1
)

echo [STEP 1] Checking for archive files...
echo.

REM Check what we have
set "HAVE_FULL=0"
set "HAVE_SPLIT=0"
set "JOINED_ARCHIVE=0"

if exist "archive\xampp.zip" (
    set "HAVE_FULL=1"
    echo [OK] Found: archive\xampp.zip
)

if exist "archive\xampp.zip.001" (
    set "HAVE_SPLIT=1"
    echo [OK] Found: archive\xampp.zip.001 (split parts)
)

if "%HAVE_FULL%"=="0" if "%HAVE_SPLIT%"=="0" (
    echo [ERROR] No archive files found!
    echo.
    echo Please download to archive\ folder:
    echo - archive\xampp.zip.001, .002, .003, etc.
    echo.
    pause
    exit /b 1
)

echo.

REM If we only have split files, join them
if "%HAVE_FULL%"=="0" if "%HAVE_SPLIT%"=="1" (
    echo [STEP 2] Joining split archive parts...
    echo This may take a few minutes...
    echo.

    REM Use PowerShell to join files
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$dest='%CD%\archive\xampp.zip'; $parts=Get-ChildItem '%CD%\archive\xampp.zip.0*'|Sort Name; $out=[IO.File]::OpenWrite($dest); try{ foreach($p in $parts){ Write-Host 'Adding:' $p.Name; $in=[IO.File]::OpenRead($p.FullName); try{ $in.CopyTo($out) }finally{ $in.Close() } } }finally{ $out.Close() }; Write-Host 'Join complete!'"

    if errorlevel 1 (
        echo [ERROR] Failed to join archive parts!
        pause
        exit /b 1
    )

    echo.
    set "JOINED_ARCHIVE=1"
)

REM Verify we have the full archive now
if not exist "archive\xampp.zip" (
    echo [ERROR] archive\xampp.zip not found!
    pause
    exit /b 1
)

echo [OK] Archive ready: archive\xampp.zip
for %%A in ("archive\xampp.zip") do set SIZE=%%~zA
set /a SIZE_MB=!SIZE! / 1048576
echo [OK] Archive size: !SIZE_MB! MB
echo.

REM Check if D:\xampp exists
if exist "D:\xampp" (
    echo [WARNING] D:\xampp already exists!
    choice /C YN /M "Overwrite"
    if errorlevel 2 (
        if "%JOINED_ARCHIVE%"=="1" del /Q "archive\xampp.zip" 2>nul
        exit /b 0
    )
    rmdir /S /Q "D:\xampp" 2>nul
)

echo [STEP 3] Extracting to D:\xampp...
echo This may take several minutes...
echo.

REM Try tar first (Windows 10+)
tar -xf "archive\xampp.zip" -C "D:\" 2>nul

if errorlevel 1 (
    echo [INFO] tar failed, trying PowerShell...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$shell=New-Object -COM Shell.Application; $zip=$shell.NameSpace('%CD%\archive\xampp.zip'); $dest=$shell.NameSpace('D:\'); if($zip){ $dest.CopyHere($zip.Items(),16) }else{ exit 1 }"

    if errorlevel 1 (
        echo [ERROR] Extraction failed!
        echo Please extract manually:
        echo 1. Right-click archive\xampp.zip
        echo 2. Extract All to D:\
        if "%JOINED_ARCHIVE%"=="1" del /Q "archive\xampp.zip" 2>nul
        pause
        exit /b 1
    )
)

echo [STEP 4] Verifying installation...
echo.

if not exist "D:\xampp\apache\bin\httpd.exe" (
    echo [ERROR] Apache not found!
    pause
    exit /b 1
)

if not exist "D:\xampp\mysql\bin\mysqld.exe" (
    echo [ERROR] MySQL not found!
    pause
    exit /b 1
)

echo [SUCCESS] XAMPP installed successfully!
echo.
echo [OK] D:\xampp\apache\bin\httpd.exe
echo [OK] D:\xampp\mysql\bin\mysqld.exe
echo [OK] D:\xampp\php\php.exe
echo.

REM Clean up joined archive
if "%JOINED_ARCHIVE%"=="1" (
    del /Q "archive\xampp.zip" 2>nul
    echo [OK] Cleaned up temporary archive (split files kept)
    echo.
)

echo Next Steps:
echo 1. cd WebInterface
echo 2. xampp_start_server.bat
echo.
pause

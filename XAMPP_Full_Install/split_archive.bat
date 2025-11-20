@echo off
REM =========================================================
REM Split XAMPP Archive for GitHub Upload - Native Windows
REM =========================================================
REM This script splits xampp.7z using native Windows commands
REM NO 7-ZIP REQUIRED!
REM GitHub file size limit: 10MB (using 9.8MB for safety)
REM =========================================================

setlocal enabledelayedexpansion

echo =========================================================
echo XAMPP Archive Splitter (Native Windows - No 7-Zip Required)
echo =========================================================
echo.

REM Check if xampp.7z exists in archive subfolder
if not exist "archive\xampp.7z" (
    echo [ERROR] xampp.7z not found in archive subfolder!
    echo.
    echo Expected location: archive\xampp.7z
    echo Current directory: %CD%
    echo.
    echo Please ensure the file structure is:
    echo XAMPP_Full_Install\
    echo   └── archive\
    echo       └── xampp.7z
    echo.
    pause
    exit /b 1
)

echo [INFO] Source file: archive\xampp.7z
echo [INFO] Chunk size: 10,280,000 bytes (~9.8MB - safe for GitHub 10MB limit)
echo [INFO] Using native Windows PowerShell - No 7-Zip required!
echo.

REM Get file size
for %%A in ("archive\xampp.7z") do set SIZE=%%~zA
set /a SIZE_MB=!SIZE! / 1048576
echo [INFO] Source file size: !SIZE_MB! MB
echo.

REM Create output directory
if not exist "split_parts" mkdir "split_parts"

REM Delete old split files if they exist
if exist "split_parts\xampp.7z.*" (
    echo [INFO] Removing old split files...
    del /Q "split_parts\xampp.7z.*" 2>nul
)

echo [INFO] Splitting archive using PowerShell...
echo [INFO] This may take a few minutes for large files...
echo.

REM Split file using PowerShell
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $source = '%CD%\archive\xampp.7z'; $destination = '%CD%\split_parts\xampp.7z'; $chunkSize = 10280000; $buffer = New-Object byte[] $chunkSize; $reader = [System.IO.File]::OpenRead($source); $partNumber = 1; try { while (($bytesRead = $reader.Read($buffer, 0, $chunkSize)) -gt 0) { $partFile = '{0}.{1:D3}' -f $destination, $partNumber; $writer = [System.IO.File]::OpenWrite($partFile); try { $writer.Write($buffer, 0, $bytesRead); Write-Host \"[OK] Created part $partNumber : $partFile ($bytesRead bytes)\"; } finally { $writer.Close(); } $partNumber++; } } finally { $reader.Close(); } Write-Host \"`n[SUCCESS] Split complete! Created $($partNumber - 1) parts.\"; }"

if errorlevel 1 (
    echo.
    echo [ERROR] Split operation failed!
    echo.
    echo Please ensure:
    echo 1. PowerShell is available (Windows 7+ has it by default)
    echo 2. You have write permissions to this folder
    echo 3. You have enough disk space (need ~2x the archive size)
    echo.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Archive split completed!
echo.

REM Count the number of parts created
set COUNT=0
for %%F in (split_parts\xampp.7z.*) do set /a COUNT+=1

echo Results:
echo - Output directory: split_parts\
echo - Number of parts: !COUNT!
echo - Part files: xampp.7z.001, xampp.7z.002, xampp.7z.003, etc.
echo - Each part: ~9.8MB (GitHub safe)
echo.
echo Next Steps:
echo 1. Upload all files from split_parts\ to GitHub archive\ folder
echo 2. Users will run join_archive.bat to reassemble the archive
echo 3. No 7-Zip required - uses native Windows commands!
echo.
echo [INFO] Listing created parts...
echo.
dir /B "split_parts\xampp.7z.*"

echo.
pause

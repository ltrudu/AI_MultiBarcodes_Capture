@echo off
REM =========================================================
REM Split XAMPP Archive for GitHub Upload - Native Windows
REM =========================================================
REM This script splits xampp.7z using native Windows commands
REM NO 7-ZIP REQUIRED!
REM GitHub file size limit: 10MB (using 9.8MB for safety)
REM =========================================================
REM
REM USAGE: Run this script from the archive folder where xampp.7z is located
REM =========================================================

setlocal enabledelayedexpansion

echo =========================================================
echo XAMPP Archive Splitter (Native Windows - No 7-Zip Required)
echo =========================================================
echo.

REM Check if xampp.zip exists in current directory

if exist "xampp.zip" (
    echo SUCCESS: xampp.zip FOUND!
    for %%A in ("xampp.zip") do set SIZE=%%~zA
    echo File size: %SIZE% bytes
) else (
    echo ERROR: xampp.zip NOT FOUND!
	pause
    exit /b 1
)


echo [INFO] Source file: xampp.zip
echo [INFO] Chunk size: 10,280,000 bytes (~9.8MB - safe for GitHub 10MB limit)
echo [INFO] Using native Windows PowerShell - No 7-Zip required!
echo.

REM Get file size
for %%A in ("xampp.zip") do set SIZE=%%~zA
set /a SIZE_MB=!SIZE! / 1048576
echo [INFO] Source file size: !SIZE_MB! MB
echo.

REM Delete old split files if they exist in current directory
if exist "xampp.zip.001" (
    echo [INFO] Removing old split files...
    del /Q "xampp.zip.0*" 2>nul
)

echo [INFO] Splitting archive using PowerShell...
echo [INFO] This may take a few minutes for large files...
echo [INFO] Split parts will be created in current directory (archive folder)
echo.

REM Split file using PowerShell - output to current directory
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $source = '%CD%\xampp.zip'; $destination = '%CD%\xampp.zip'; $chunkSize = 10280000; $buffer = New-Object byte[] $chunkSize; $reader = [System.IO.File]::OpenRead($source); $partNumber = 1; try { while (($bytesRead = $reader.Read($buffer, 0, $chunkSize)) -gt 0) { $partFile = '{0}.{1:D3}' -f $destination, $partNumber; $writer = [System.IO.File]::OpenWrite($partFile); try { $writer.Write($buffer, 0, $bytesRead); Write-Host \"[OK] Created part $partNumber : $partFile ($bytesRead bytes)\"; } finally { $writer.Close(); } $partNumber++; } } finally { $reader.Close(); } Write-Host \"`n[SUCCESS] Split complete! Created $($partNumber - 1) parts.\"; }"

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
for %%F in (xampp.zip.0*) do set /a COUNT+=1

echo Results:
echo - Output directory: %CD%
echo - Number of parts: !COUNT!
echo - Part files: xampp.zip.001, xampp.zip.002, xampp.zip.003, etc.
echo - Each part: ~9.8MB (GitHub safe)
echo.
echo Next Steps:
echo 1. Commit and push all xampp.zip.* files to GitHub
echo 2. Delete the original xampp.zip from version control (too large)
echo 3. Users will automatically join the parts using install_Xampp_to_C.bat or install_Xampp_to_D.bat
echo 4. Or users can manually run join_archive.bat to reassemble
echo.
echo [INFO] Listing created parts...
echo.
dir /B "xampp.zip.0*"

echo.
echo [INFO] You can now safely delete xampp.zip (the original full archive)
echo       Keep only the split parts (xampp.zip.001, .002, etc.) for GitHub
echo.
pause

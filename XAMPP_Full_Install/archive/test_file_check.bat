@echo off
echo Testing file detection...
echo Current directory: %CD%
echo.

if exist "xampp.7z" (
    echo SUCCESS: xampp.7z FOUND!
    for %%A in ("xampp.7z") do set SIZE=%%~zA
    echo File size: %SIZE% bytes
) else (
    echo ERROR: xampp.7z NOT FOUND!
)

pause

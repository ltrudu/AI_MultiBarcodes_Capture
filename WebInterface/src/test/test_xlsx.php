<?php
// Simple test script for XLSX export functionality
require_once '../lib/SimpleXLSXWriter.php';

echo "Testing XLSX Export Functionality...\n";

try {
    // Test data similar to what would come from the database
    $testData = [
        ['Date', 'Symbology', 'Data', 'Quantity'],
        ['14:30:25', 'Code128', '123456789', '1'],
        ['14:31:10', 'QR Code', 'https://example.com', '2'],
        ['14:32:05', 'EAN13', '1234567890123', '1']
    ];

    // Create XLSX writer
    $xlsx = new SimpleXLSXWriter('test_export.xlsx');

    // Add rows
    foreach ($testData as $row) {
        $xlsx->addRow($row);
    }

    // Try to create the file
    $zipFile = $xlsx->writeToFile();

    if (file_exists($zipFile)) {
        $fileSize = filesize($zipFile);
        echo "✅ XLSX file created successfully!\n";
        echo "📁 File: $zipFile\n";
        echo "📏 Size: $fileSize bytes\n";

        // Check if it's a valid ZIP file
        $zip = new ZipArchive();
        if ($zip->open($zipFile) === TRUE) {
            echo "✅ ZIP structure is valid\n";
            echo "📂 Contains " . $zip->numFiles . " files:\n";
            for ($i = 0; $i < $zip->numFiles; $i++) {
                $filename = $zip->getNameIndex($i);
                echo "   - $filename\n";
            }
            $zip->close();
        } else {
            echo "❌ Invalid ZIP structure\n";
        }

        // Clean up test file
        unlink($zipFile);
        echo "🧹 Test file cleaned up\n";

    } else {
        echo "❌ XLSX file creation failed\n";
    }

} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    echo "📍 File: " . $e->getFile() . "\n";
    echo "📍 Line: " . $e->getLine() . "\n";
}

echo "\nTest completed.\n";
?>
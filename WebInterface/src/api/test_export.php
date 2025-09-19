<?php
// Simple test script for export functionality
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Simulate POST data
$_SERVER['REQUEST_METHOD'] = 'POST';
$_POST = [];

// Simulate JSON input
$test_data = json_encode([
    'session_ids' => ['1', '2'],
    'format' => 'txt'
]);

// Simulate php://input
$input_stream = fopen('php://memory', 'r+');
fwrite($input_stream, $test_data);
rewind($input_stream);

echo "Testing export functionality...\n";
echo "Input data: " . $test_data . "\n";

try {
    require_once 'export.php';
    echo "Export script loaded successfully!\n";
} catch (Exception $e) {
    echo "Error: " . $e->getMessage() . "\n";
    echo "Stack trace: " . $e->getTraceAsString() . "\n";
}
?>
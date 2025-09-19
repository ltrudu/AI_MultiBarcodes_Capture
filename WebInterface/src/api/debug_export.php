<?php
// Debug version of export.php with detailed error reporting
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't display errors to prevent header issues
ini_set('log_errors', 1);

$debug_info = [
    'timestamp' => date('Y-m-d H:i:s'),
    'method' => $_SERVER['REQUEST_METHOD'],
    'step' => 'start',
    'errors' => []
];

try {
    $debug_info['step'] = 'reading_input';
    $input = file_get_contents('php://input');
    $debug_info['raw_input'] = $input;

    $debug_info['step'] = 'parsing_json';
    $data = json_decode($input, true);
    $debug_info['parsed_data'] = $data;

    if (!$data) {
        throw new Exception('Invalid JSON input: ' . json_last_error_msg());
    }

    $debug_info['step'] = 'validating_input';
    if (!isset($data['session_ids']) || !isset($data['format'])) {
        throw new Exception('Missing session_ids or format parameter');
    }

    $debug_info['step'] = 'connecting_database';
    require_once '../config/database.php';

    $debug_info['step'] = 'creating_database_object';
    $db = new Database();

    $debug_info['step'] = 'getting_connection';
    $connection = $db->getConnection();

    $debug_info['step'] = 'preparing_query';
    $sessionIds = $data['session_ids'];
    $placeholders = str_repeat('?,', count($sessionIds) - 1) . '?';

    $query = "
        SELECT
            b.id,
            b.value as barcode_value,
            b.symbology,
            b.symbology_name,
            b.quantity,
            b.timestamp as scan_date,
            COALESCE(b.processed, 0) as processed,
            COALESCE(b.notes, '') as notes,
            s.device_info,
            s.device_ip,
            s.session_timestamp as session_created
        FROM barcodes b
        JOIN capture_sessions s ON b.session_id = s.id
        WHERE b.session_id IN ($placeholders)
        ORDER BY s.session_timestamp DESC, b.timestamp ASC
        LIMIT 10
    ";

    $debug_info['step'] = 'executing_query';
    $debug_info['query'] = $query;
    $debug_info['session_ids'] = $sessionIds;

    $stmt = $connection->prepare($query);
    $stmt->execute($sessionIds);

    $debug_info['step'] = 'fetching_results';
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $debug_info['result_count'] = count($results);
    $debug_info['sample_results'] = array_slice($results, 0, 2); // First 2 results for debugging

    $debug_info['step'] = 'success';
    $debug_info['status'] = 'Export would work with ' . count($results) . ' records';

} catch (Exception $e) {
    $debug_info['step'] = 'error';
    $debug_info['error_message'] = $e->getMessage();
    $debug_info['error_trace'] = $e->getTraceAsString();
    $debug_info['errors'][] = [
        'message' => $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine()
    ];
}

// Return debug information as JSON
echo json_encode($debug_info, JSON_PRETTY_PRINT);
?>
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

class MockBarcodeAPI {

    public function handleRequest() {
        $method = $_SERVER['REQUEST_METHOD'];

        try {
            switch ($method) {
                case 'POST':
                    $this->receiveBarcodeCaptureSession();
                    break;
                case 'GET':
                    if (isset($_GET['session_id'])) {
                        $this->getSessionDetails($_GET['session_id']);
                    } else {
                        $this->getAllSessions();
                    }
                    break;
                case 'PUT':
                    if (isset($_GET['barcode_id'])) {
                        $this->updateBarcodeStatus($_GET['barcode_id']);
                    }
                    break;
                case 'DELETE':
                    if (isset($_GET['reset']) && $_GET['reset'] === 'all') {
                        $this->resetAllData();
                    } else {
                        $this->sendError('Invalid DELETE request', 400);
                    }
                    break;
                default:
                    $this->sendError('Method not allowed', 405);
                    break;
            }
        } catch (Exception $e) {
            $this->sendError($e->getMessage(), 500);
        }
    }

    private function receiveBarcodeCaptureSession() {
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['barcodes'])) {
            $this->sendError('Invalid JSON data or missing barcodes array', 400);
            return;
        }

        // Mock successful response
        $session_id = rand(1000, 9999);
        $total_barcodes = count($input['barcodes']);

        $this->sendResponse([
            'success' => true,
            'message' => 'Barcode capture session received successfully (MOCK MODE)',
            'session_id' => $session_id,
            'total_barcodes' => $total_barcodes
        ]);
    }

    private function getAllSessions() {
        // Mock sessions data
        $sessions = [
            [
                'id' => 1,
                'session_timestamp' => '2024-12-17 14:30:00',
                'device_info' => 'Samsung Galaxy S21 - Android 13',
                'total_barcodes' => 15,
                'unique_symbologies' => 3,
                'processed_count' => 10,
                'first_scan' => '2024-12-17 14:30:05',
                'last_scan' => '2024-12-17 14:35:22',
                'created_at' => '2024-12-17 14:30:00'
            ],
            [
                'id' => 2,
                'session_timestamp' => '2024-12-17 13:15:00',
                'device_info' => 'Zebra TC8300 - Android 11',
                'total_barcodes' => 8,
                'unique_symbologies' => 2,
                'processed_count' => 8,
                'first_scan' => '2024-12-17 13:15:10',
                'last_scan' => '2024-12-17 13:18:45',
                'created_at' => '2024-12-17 13:15:00'
            ],
            [
                'id' => 3,
                'session_timestamp' => '2024-12-17 12:00:00',
                'device_info' => 'Motorola MC9300 - Android 10',
                'total_barcodes' => 23,
                'unique_symbologies' => 4,
                'processed_count' => 5,
                'first_scan' => '2024-12-17 12:00:15',
                'last_scan' => '2024-12-17 12:12:33',
                'created_at' => '2024-12-17 12:00:00'
            ]
        ];

        $this->sendResponse([
            'success' => true,
            'sessions' => $sessions,
            'total' => count($sessions)
        ]);
    }

    private function getSessionDetails($session_id) {
        // Mock session details
        $session = [
            'id' => intval($session_id),
            'session_timestamp' => '2024-12-17 14:30:00',
            'device_info' => 'Samsung Galaxy S21 - Android 13',
            'total_barcodes' => 15,
            'unique_symbologies' => 3,
            'processed_count' => 10,
            'first_scan' => '2024-12-17 14:30:05',
            'last_scan' => '2024-12-17 14:35:22',
            'created_at' => '2024-12-17 14:30:00'
        ];

        $barcodes = [
            [
                'id' => 1,
                'session_id' => intval($session_id),
                'value' => '1234567890123',
                'symbology' => 34,
                'symbology_name' => 'EAN-13',
                'quantity' => 1,
                'processed' => true,
                'notes' => 'Product scanned successfully',
                'timestamp' => '2024-12-17 14:30:05'
            ],
            [
                'id' => 2,
                'session_id' => intval($session_id),
                'value' => 'ABC123DEF456',
                'symbology' => 1,
                'symbology_name' => 'Code 128',
                'quantity' => 2,
                'processed' => false,
                'notes' => '',
                'timestamp' => '2024-12-17 14:31:15'
            ],
            [
                'id' => 3,
                'session_id' => intval($session_id),
                'value' => '987654321',
                'symbology' => 6,
                'symbology_name' => 'Code 39',
                'quantity' => 1,
                'processed' => true,
                'notes' => 'Inventory item verified',
                'timestamp' => '2024-12-17 14:32:30'
            ]
        ];

        $this->sendResponse([
            'success' => true,
            'session' => $session,
            'barcodes' => $barcodes
        ]);
    }

    private function updateBarcodeStatus($barcode_id) {
        $input = json_decode(file_get_contents('php://input'), true);

        // Mock successful update
        $this->sendResponse([
            'success' => true,
            'message' => 'Barcode status updated successfully (MOCK MODE)'
        ]);
    }

    private function resetAllData() {
        // Mock successful reset
        $this->sendResponse([
            'success' => true,
            'message' => 'All barcode data has been reset successfully (MOCK MODE)',
            'deleted_sessions' => 3,
            'deleted_barcodes' => 46
        ]);
    }

    private function sendResponse($data, $status_code = 200) {
        http_response_code($status_code);
        echo json_encode($data, JSON_PRETTY_PRINT);
        exit();
    }

    private function sendError($message, $status_code = 400) {
        http_response_code($status_code);
        echo json_encode([
            'success' => false,
            'error' => $message
        ], JSON_PRETTY_PRINT);
        exit();
    }
}

// Initialize and handle the request
$api = new MockBarcodeAPI();
$api->handleRequest();
?>
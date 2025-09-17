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

require_once '../config/database.php';

class BarcodeAPI {
    private $db;
    private $connection;

    public function __construct() {
        $this->db = new Database();
        $this->connection = $this->db->getConnection();
    }

    public function handleRequest() {
        $method = $_SERVER['REQUEST_METHOD'];
        $request_uri = $_SERVER['REQUEST_URI'];

        // Parse the request path
        $path = parse_url($request_uri, PHP_URL_PATH);
        $path_parts = explode('/', trim($path, '/'));

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

        $this->connection->beginTransaction();

        try {
            // Create capture session
            $session_timestamp = $this->convertTimestamp($input['session_timestamp'] ?? null);
            $device_info = $input['device_info'] ?? 'Unknown Device';
            $total_barcodes = count($input['barcodes']);

            $stmt = $this->connection->prepare("
                INSERT INTO capture_sessions (session_timestamp, device_info, total_barcodes)
                VALUES (:session_timestamp, :device_info, :total_barcodes)
            ");

            $stmt->bindParam(':session_timestamp', $session_timestamp);
            $stmt->bindParam(':device_info', $device_info);
            $stmt->bindParam(':total_barcodes', $total_barcodes);
            $stmt->execute();

            $session_id = $this->connection->lastInsertId();

            // Insert individual barcodes
            $barcode_stmt = $this->connection->prepare("
                INSERT INTO barcodes (session_id, value, symbology, symbology_name, quantity, timestamp)
                VALUES (:session_id, :value, :symbology, :symbology_name, :quantity, :timestamp)
            ");

            foreach ($input['barcodes'] as $barcode) {
                if (!isset($barcode['value']) || empty($barcode['value'])) {
                    continue;
                }

                $symbology = $barcode['symbology'] ?? 0;
                $symbology_name = $this->getSymbologyName($symbology);
                $quantity = $barcode['quantity'] ?? 1;
                $barcode_timestamp = $this->convertTimestamp($barcode['timestamp'] ?? null);

                $barcode_stmt->bindParam(':session_id', $session_id);
                $barcode_stmt->bindParam(':value', $barcode['value']);
                $barcode_stmt->bindParam(':symbology', $symbology);
                $barcode_stmt->bindParam(':symbology_name', $symbology_name);
                $barcode_stmt->bindParam(':quantity', $quantity);
                $barcode_stmt->bindParam(':timestamp', $barcode_timestamp);
                $barcode_stmt->execute();
            }

            $this->connection->commit();

            $this->sendResponse([
                'success' => true,
                'message' => 'Barcode capture session received successfully',
                'session_id' => $session_id,
                'total_barcodes' => $total_barcodes
            ]);

        } catch (Exception $e) {
            $this->connection->rollBack();
            $this->sendError('Failed to save barcode session: ' . $e->getMessage(), 500);
        }
    }

    private function getAllSessions() {
        try {
            $stmt = $this->connection->prepare("
                SELECT * FROM session_statistics
                ORDER BY created_at DESC
                LIMIT 100
            ");
            $stmt->execute();
            $sessions = $stmt->fetchAll();

            $this->sendResponse([
                'success' => true,
                'sessions' => $sessions,
                'total' => count($sessions)
            ]);
        } catch (Exception $e) {
            $this->sendError('Failed to retrieve sessions: ' . $e->getMessage(), 500);
        }
    }

    private function getSessionDetails($session_id) {
        try {
            // Get session info
            $session_stmt = $this->connection->prepare("
                SELECT * FROM session_statistics WHERE id = :session_id
            ");
            $session_stmt->bindParam(':session_id', $session_id);
            $session_stmt->execute();
            $session = $session_stmt->fetch();

            if (!$session) {
                $this->sendError('Session not found', 404);
                return;
            }

            // Get barcodes for this session
            $barcodes_stmt = $this->connection->prepare("
                SELECT * FROM barcode_details WHERE session_id = :session_id
                ORDER BY timestamp ASC
            ");
            $barcodes_stmt->bindParam(':session_id', $session_id);
            $barcodes_stmt->execute();
            $barcodes = $barcodes_stmt->fetchAll();

            $this->sendResponse([
                'success' => true,
                'session' => $session,
                'barcodes' => $barcodes
            ]);
        } catch (Exception $e) {
            $this->sendError('Failed to retrieve session details: ' . $e->getMessage(), 500);
        }
    }

    private function updateBarcodeStatus($barcode_id) {
        $input = json_decode(file_get_contents('php://input'), true);

        try {
            $stmt = $this->connection->prepare("
                UPDATE barcodes
                SET processed = :processed, notes = :notes, updated_at = CURRENT_TIMESTAMP
                WHERE id = :barcode_id
            ");

            $processed = $input['processed'] ?? false;
            $notes = $input['notes'] ?? '';

            $stmt->bindParam(':processed', $processed, PDO::PARAM_BOOL);
            $stmt->bindParam(':notes', $notes);
            $stmt->bindParam(':barcode_id', $barcode_id);
            $stmt->execute();

            if ($stmt->rowCount() > 0) {
                $this->sendResponse([
                    'success' => true,
                    'message' => 'Barcode status updated successfully'
                ]);
            } else {
                $this->sendError('Barcode not found', 404);
            }
        } catch (Exception $e) {
            $this->sendError('Failed to update barcode status: ' . $e->getMessage(), 500);
        }
    }

    private function getSymbologyName($symbology_id) {
        // Map integer values to human-readable names from EBarcodesSymbologies enum
        // Based on Zebra AI DataCapture documentation: https://techdocs.zebra.com/ai-datacapture/latest/barcodedecoder/#barcodesymbologies
        $symbology_map = array(
            -1 => 'UNKNOWN',
            0 => 'EAN 8',
            1 => 'EAN 13',
            2 => 'UPC A',
            3 => 'UPC E',
            4 => 'AZTEC',
            5 => 'CODABAR',
            6 => 'CODE128',
            7 => 'CODE39',
            8 => 'I2OF5',
            9 => 'GS1 DATABAR',
            10 => 'DATAMATRIX',
            11 => 'GS1 DATABAR EXPANDED',
            12 => 'MAILMARK',
            13 => 'MAXICODE',
            14 => 'PDF417',
            15 => 'QRCODE',
            16 => 'DOTCODE',
            17 => 'GRID MATRIX',
            18 => 'GS1 DATAMATRIX',
            19 => 'GS1 QRCODE',
            20 => 'MICROQR',
            21 => 'MICROPDF',
            22 => 'USPOSTNET',
            23 => 'USPLANET',
            24 => 'UK POSTAL',
            25 => 'JAPANESE POSTAL',
            26 => 'AUSTRALIAN POSTAL',
            27 => 'CANADIAN POSTAL',
            28 => 'DUTCH POSTAL',
            29 => 'US4STATE',
            30 => 'US4STATE FICS',
            31 => 'MSI',
            32 => 'CODE93',
            33 => 'TRIOPTIC39',
            34 => 'D2OF5',
            35 => 'CHINESE 2OF5',
            36 => 'KOREAN 3OF5',
            37 => 'CODE11',
            38 => 'TLC39',
            39 => 'HANXIN',
            40 => 'MATRIX 2OF5',
            41 => 'UPCE1',
            42 => 'GS1 DATABAR LIM',
            43 => 'FINNISH POSTAL 4S',
            44 => 'COMPOSITE AB',
            45 => 'COMPOSITE C'
        );

        return isset($symbology_map[$symbology_id]) ? $symbology_map[$symbology_id] : 'UNKNOWN';
    }

    private function resetAllData() {
        try {
            // Delete all barcodes first (due to foreign key constraint)
            $delete_barcodes = $this->connection->prepare("DELETE FROM barcodes");
            $delete_barcodes->execute();
            $deleted_barcodes_count = $delete_barcodes->rowCount();

            // Delete all capture sessions
            $delete_sessions = $this->connection->prepare("DELETE FROM capture_sessions");
            $delete_sessions->execute();
            $deleted_sessions_count = $delete_sessions->rowCount();

            // Reset auto increment counters (DDL statements auto-commit)
            $this->connection->exec("ALTER TABLE barcodes AUTO_INCREMENT = 1");
            $this->connection->exec("ALTER TABLE capture_sessions AUTO_INCREMENT = 1");

            $this->sendResponse([
                'success' => true,
                'message' => 'All barcode data has been reset successfully',
                'deleted_sessions' => $deleted_sessions_count,
                'deleted_barcodes' => $deleted_barcodes_count
            ]);

        } catch (Exception $e) {
            $this->sendError('Failed to reset data: ' . $e->getMessage(), 500);
        }
    }

    private function convertTimestamp($timestamp) {
        if (!$timestamp) {
            return date('Y-m-d H:i:s');
        }

        try {
            // Handle ISO 8601 format from Android (2025-09-16T19:01:54.978Z)
            if (strpos($timestamp, 'T') !== false) {
                $datetime = new DateTime($timestamp);
                return $datetime->format('Y-m-d H:i:s');
            }

            // If already in MySQL format, return as-is
            if (preg_match('/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/', $timestamp)) {
                return $timestamp;
            }

            // Try to parse other formats
            $datetime = new DateTime($timestamp);
            return $datetime->format('Y-m-d H:i:s');
        } catch (Exception $e) {
            // If conversion fails, return current timestamp
            return date('Y-m-d H:i:s');
        }
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
$api = new BarcodeAPI();
$api->handleRequest();
?>
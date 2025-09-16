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
            $session_timestamp = $input['session_timestamp'] ?? date('Y-m-d H:i:s');
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

                $symbology_name = $this->getSymbologyName($barcode['symbology'] ?? 0);

                $barcode_stmt->bindParam(':session_id', $session_id);
                $barcode_stmt->bindParam(':value', $barcode['value']);
                $barcode_stmt->bindParam(':symbology', $barcode['symbology'] ?? 0);
                $barcode_stmt->bindParam(':symbology_name', $symbology_name);
                $barcode_stmt->bindParam(':quantity', $barcode['quantity'] ?? 1);
                $barcode_stmt->bindParam(':timestamp', $barcode['timestamp'] ?? $session_timestamp);
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

            $stmt->bindParam(':processed', $input['processed'] ?? false, PDO::PARAM_BOOL);
            $stmt->bindParam(':notes', $input['notes'] ?? '');
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
        try {
            $stmt = $this->connection->prepare("SELECT name FROM symbology_types WHERE id = :id");
            $stmt->bindParam(':id', $symbology_id);
            $stmt->execute();
            $result = $stmt->fetch();
            return $result ? $result['name'] : 'UNKNOWN';
        } catch (Exception $e) {
            return 'UNKNOWN';
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
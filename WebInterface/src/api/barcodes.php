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
                    $input = json_decode(file_get_contents('php://input'), true);
                    if (isset($input['action']) && $input['action'] === 'bulk_merge') {
                        $this->bulkMergeSessions();
                    } else {
                        $this->receiveBarcodeCaptureSession();
                    }
                    break;
                case 'GET':
                    if (isset($_GET['session_id'])) {
                        $this->getSessionDetails($_GET['session_id']);
                    } else {
                        $this->getAllSessions();
                    }
                    break;
                case 'PUT':
                    $input = json_decode(file_get_contents('php://input'), true);
                    if (isset($input['action']) && $input['action'] === 'update_barcode') {
                        $this->updateBarcodeData();
                    } else if (isset($_GET['barcode_id'])) {
                        $this->updateBarcodeStatus($_GET['barcode_id']);
                    }
                    break;
                case 'DELETE':
                    if (isset($_GET['reset']) && $_GET['reset'] === 'all') {
                        $this->resetAllData();
                    } else {
                        $input = json_decode(file_get_contents('php://input'), true);
                        if (isset($input['action']) && $input['action'] === 'bulk_delete') {
                            $this->bulkDeleteSessions();
                        } else {
                            $this->sendError('Invalid DELETE request', 400);
                        }
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
            $device_ip = $input['device_ip'] ?? '0.0.0.0';
            $total_barcodes = count($input['barcodes']);

            $stmt = $this->connection->prepare("
                INSERT INTO capture_sessions (session_timestamp, device_info, device_ip, total_barcodes)
                VALUES (:session_timestamp, :device_info, :device_ip, :total_barcodes)
            ");

            $stmt->bindParam(':session_timestamp', $session_timestamp);
            $stmt->bindParam(':device_info', $device_info);
            $stmt->bindParam(':device_ip', $device_ip);
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
                // Get symbology name from database - ONLY source of truth
                $symbology_stmt = $this->connection->prepare("SELECT name FROM symbologies WHERE id = ?");
                $symbology_stmt->execute([$symbology]);
                $symbology_result = $symbology_stmt->fetch(PDO::FETCH_ASSOC);
                $symbology_name = $symbology_result ? $symbology_result['name'] : 'UNKNOWN';
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

    private function updateBarcodeData() {
        $input = json_decode(file_get_contents('php://input'), true);

        try {
            $barcode_id = $input['barcode_id'] ?? null;
            $value = $input['value'] ?? null;
            $symbology = $input['symbology'] ?? null;
            $quantity = $input['quantity'] ?? null;

            // Validate required fields
            if (!$barcode_id || !$value || $symbology === null || !$quantity) {
                $this->sendError('Missing required fields: barcode_id, value, symbology, quantity', 400);
                return;
            }

            // Validate symbology value
            // Validate symbology exists in database
            $check_stmt = $this->connection->prepare("SELECT COUNT(*) FROM symbologies WHERE id = ?");
            $check_stmt->execute([$symbology]);
            if ($check_stmt->fetchColumn() == 0) {
                $this->sendError('Invalid symbology value: ' . $symbology, 400);
                return;
            }

            // Validate quantity
            if ($quantity < 1) {
                $this->sendError('Quantity must be at least 1', 400);
                return;
            }

            // Get the symbology name
            // Get symbology name from database - ONLY source of truth
            $symbology_stmt = $this->connection->prepare("SELECT name FROM symbologies WHERE id = ?");
            $symbology_stmt->execute([$symbology]);
            $symbology_result = $symbology_stmt->fetch(PDO::FETCH_ASSOC);
            $symbology_name = $symbology_result ? $symbology_result['name'] : 'UNKNOWN';

            // Update the barcode
            $stmt = $this->connection->prepare("
                UPDATE barcodes
                SET value = :value, symbology = :symbology, symbology_name = :symbology_name,
                    quantity = :quantity, updated_at = CURRENT_TIMESTAMP
                WHERE id = :barcode_id
            ");

            $stmt->bindParam(':value', $value);
            $stmt->bindParam(':symbology', $symbology, PDO::PARAM_INT);
            $stmt->bindParam(':symbology_name', $symbology_name);
            $stmt->bindParam(':quantity', $quantity, PDO::PARAM_INT);
            $stmt->bindParam(':barcode_id', $barcode_id, PDO::PARAM_INT);
            $stmt->execute();

            if ($stmt->rowCount() > 0) {
                $this->sendResponse([
                    'success' => true,
                    'message' => 'Barcode updated successfully',
                    'updated_fields' => [
                        'value' => $value,
                        'symbology' => $symbology,
                        'symbology_name' => $symbology_name,
                        'quantity' => $quantity
                    ]
                ]);
            } else {
                $this->sendError('Barcode not found or no changes made', 404);
            }
        } catch (Exception $e) {
            $this->sendError('Failed to update barcode: ' . $e->getMessage(), 500);
        }
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

    private function bulkDeleteSessions() {
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['session_ids']) || !is_array($input['session_ids'])) {
            $this->sendError('Invalid request data or missing session_ids array', 400);
            return;
        }

        $session_ids = $input['session_ids'];

        if (count($session_ids) === 0) {
            $this->sendError('No session IDs provided', 400);
            return;
        }

        $this->connection->beginTransaction();

        try {
            // Create placeholders for prepared statement
            $placeholders = str_repeat('?,', count($session_ids) - 1) . '?';

            // Delete barcodes first (due to foreign key constraint)
            $delete_barcodes_stmt = $this->connection->prepare("
                DELETE FROM barcodes WHERE session_id IN ($placeholders)
            ");
            $delete_barcodes_stmt->execute($session_ids);
            $deleted_barcodes_count = $delete_barcodes_stmt->rowCount();

            // Delete sessions
            $delete_sessions_stmt = $this->connection->prepare("
                DELETE FROM capture_sessions WHERE id IN ($placeholders)
            ");
            $delete_sessions_stmt->execute($session_ids);
            $deleted_sessions_count = $delete_sessions_stmt->rowCount();

            $this->connection->commit();

            $this->sendResponse([
                'success' => true,
                'message' => "Successfully deleted $deleted_sessions_count session(s) and $deleted_barcodes_count barcode(s)",
                'deleted_sessions' => $deleted_sessions_count,
                'deleted_barcodes' => $deleted_barcodes_count
            ]);

        } catch (Exception $e) {
            $this->connection->rollBack();
            $this->sendError('Failed to delete sessions: ' . $e->getMessage(), 500);
        }
    }

    private function bulkMergeSessions() {
        $input = json_decode(file_get_contents('php://input'), true);

        if (!$input || !isset($input['session_ids']) || !is_array($input['session_ids'])) {
            $this->sendError('Invalid request data or missing session_ids array', 400);
            return;
        }

        $session_ids = $input['session_ids'];

        if (count($session_ids) < 2) {
            $this->sendError('At least 2 sessions are required for merging', 400);
            return;
        }

        $this->connection->beginTransaction();

        try {
            // Get all barcodes from selected sessions
            $placeholders = str_repeat('?,', count($session_ids) - 1) . '?';
            $get_barcodes_stmt = $this->connection->prepare("
                SELECT b.value, b.symbology, s.name as symbology_name, SUM(b.quantity) as total_quantity
                FROM barcodes b
                LEFT JOIN symbologies s ON b.symbology = s.id
                WHERE b.session_id IN ($placeholders)
                GROUP BY b.value, b.symbology, s.name
                ORDER BY b.value
            ");
            $get_barcodes_stmt->execute($session_ids);
            $consolidated_barcodes = $get_barcodes_stmt->fetchAll();

            if (count($consolidated_barcodes) === 0) {
                $this->connection->rollBack();
                $this->sendError('No barcodes found in selected sessions', 400);
                return;
            }

            // Create new merged session
            $current_time = date('Y-m-d H:i:s');
            $device_info = 'Merged by user';
            $device_ip = $_SERVER['REMOTE_ADDR'] ?? ($_SERVER['HTTP_X_FORWARDED_FOR'] ?? ($_SERVER['HTTP_CLIENT_IP'] ?? '0.0.0.0'));
            $total_barcodes = count($consolidated_barcodes);

            $create_session_stmt = $this->connection->prepare("
                INSERT INTO capture_sessions (session_timestamp, device_info, device_ip, total_barcodes)
                VALUES (:session_timestamp, :device_info, :device_ip, :total_barcodes)
            ");
            $create_session_stmt->bindParam(':session_timestamp', $current_time);
            $create_session_stmt->bindParam(':device_info', $device_info);
            $create_session_stmt->bindParam(':device_ip', $device_ip);
            $create_session_stmt->bindParam(':total_barcodes', $total_barcodes);
            $create_session_stmt->execute();

            $new_session_id = $this->connection->lastInsertId();

            // Insert consolidated barcodes into new session
            $insert_barcode_stmt = $this->connection->prepare("
                INSERT INTO barcodes (session_id, value, symbology, symbology_name, quantity, timestamp)
                VALUES (:session_id, :value, :symbology, :symbology_name, :quantity, :timestamp)
            ");

            foreach ($consolidated_barcodes as $barcode) {
                $insert_barcode_stmt->bindParam(':session_id', $new_session_id);
                $insert_barcode_stmt->bindParam(':value', $barcode['value']);
                $insert_barcode_stmt->bindParam(':symbology', $barcode['symbology']);
                $insert_barcode_stmt->bindParam(':symbology_name', $barcode['symbology_name']);
                $insert_barcode_stmt->bindParam(':quantity', $barcode['total_quantity']);
                $insert_barcode_stmt->bindParam(':timestamp', $current_time);
                $insert_barcode_stmt->execute();
            }

            // Delete original sessions and their barcodes
            $delete_barcodes_stmt = $this->connection->prepare("
                DELETE FROM barcodes WHERE session_id IN ($placeholders)
            ");
            $delete_barcodes_stmt->execute($session_ids);

            $delete_sessions_stmt = $this->connection->prepare("
                DELETE FROM capture_sessions WHERE id IN ($placeholders)
            ");
            $delete_sessions_stmt->execute($session_ids);

            $this->connection->commit();

            $this->sendResponse([
                'success' => true,
                'message' => "Successfully merged " . count($session_ids) . " sessions into new session",
                'new_session_id' => $new_session_id,
                'merged_sessions_count' => count($session_ids),
                'total_barcodes' => $total_barcodes,
                'consolidated_barcodes' => count($consolidated_barcodes)
            ]);

        } catch (Exception $e) {
            $this->connection->rollBack();
            $this->sendError('Failed to merge sessions: ' . $e->getMessage(), 500);
        }
    }
}

// Initialize and handle the request
$api = new BarcodeAPI();
$api->handleRequest();
?>
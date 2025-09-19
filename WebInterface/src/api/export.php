<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('log_errors', 1);

require_once '../config/database.php';
require_once '../lib/SimpleXLSXWriter.php';

class ExportAPI {
    private $db;
    private $connection;

    public function __construct() {
        try {
            $this->db = new Database();
            $this->connection = $this->db->getConnection();
        } catch (Exception $e) {
            error_log("Database connection failed: " . $e->getMessage());
            throw new Exception("Database connection failed: " . $e->getMessage());
        }
    }

    public function handleRequest() {
        header('Access-Control-Allow-Origin: *');
        header('Access-Control-Allow-Methods: POST, OPTIONS');
        header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

        // Handle preflight requests
        if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
            http_response_code(200);
            exit();
        }

        $method = $_SERVER['REQUEST_METHOD'];

        try {
            switch ($method) {
                case 'POST':
                    $this->exportSessions();
                    break;
                default:
                    http_response_code(405);
                    echo json_encode(['error' => 'Method not allowed']);
                    break;
            }
        } catch (Exception $e) {
            http_response_code(500);
            echo json_encode(['error' => 'Internal server error: ' . $e->getMessage()]);
        }
    }

    private function exportSessions() {
        $input = json_decode(file_get_contents('php://input'), true);

        if (!isset($input['session_ids']) || !isset($input['format'])) {
            http_response_code(400);
            echo json_encode(['error' => 'Missing session_ids or format parameter']);
            return;
        }

        $sessionIds = $input['session_ids'];
        $format = strtolower($input['format']);

        if (empty($sessionIds)) {
            http_response_code(400);
            echo json_encode(['error' => 'No sessions selected for export']);
            return;
        }

        // Validate format
        if (!in_array($format, ['txt', 'csv', 'excel', 'xlsx'])) {
            http_response_code(400);
            echo json_encode(['error' => 'Invalid format. Supported formats: txt, csv, excel, xlsx']);
            return;
        }

        // Normalize excel format
        if ($format === 'excel') {
            $format = 'xlsx';
        }

        try {
            // Get export data
            $exportData = $this->getExportData($sessionIds);

            if (empty($exportData)) {
                http_response_code(404);
                echo json_encode(['error' => 'No data found for selected sessions']);
                return;
            }

            // Generate filename
            $timestamp = date('Y-m-d_H-i-s');
            $filename = "barcode_export_{$timestamp}.{$format}";

            // Export based on format
            switch ($format) {
                case 'txt':
                    $this->exportAsTXT($exportData, $filename);
                    break;
                case 'csv':
                    $this->exportAsCSV($exportData, $filename);
                    break;
                case 'xlsx':
                    $this->exportAsXLSX($exportData, $filename);
                    break;
            }

        } catch (Exception $e) {
            http_response_code(500);
            echo json_encode(['error' => 'Export failed: ' . $e->getMessage()]);
        }
    }

    private function getExportData($sessionIds) {
        // Create placeholders for the IN clause
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
        ";

        $stmt = $this->connection->prepare($query);
        $stmt->execute($sessionIds);

        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    private function exportAsTXT($data, $filename) {
        // Set headers for file download
        header('Content-Type: text/plain');
        header('Content-Disposition: attachment; filename="' . $filename . '"');
        header('Cache-Control: no-cache, must-revalidate');
        header('Expires: 0');

        // Generate TXT content (exactly like Android SessionsFilesHelpers.saveDataTXT)
        $output = "";

        // Header (like Android version)
        $output .= "-----------------------------------------\n";
        $output .= "Capture file: " . $filename . "\n";
        $output .= "Created the: " . date('l, F j, Y H:i:s') . "\n";
        $output .= "-----------------------------------------\n";

        // Export each barcode entry
        foreach ($data as $barcode) {
            $scanDate = $barcode['scan_date'] ? date('l, F j, Y H:i:s', strtotime($barcode['scan_date'])) : date('l, F j, Y H:i:s');

            $output .= "Value:" . $barcode['barcode_value'] . "\n";
            $output .= "Symbology:" . $barcode['symbology_name'] . "\n";
            $output .= "Quantity:" . $barcode['quantity'] . "\n";
            $output .= "Capture Date:" . $scanDate . "\n";
            $output .= "-----------------------------------------\n";
        }

        echo $output;
    }

    private function exportAsCSV($data, $filename) {
        // Set headers for file download
        header('Content-Type: text/csv');
        header('Content-Disposition: attachment; filename="' . $filename . '"');
        header('Cache-Control: no-cache, must-revalidate');
        header('Expires: 0');

        // Create file handle for output
        $output = fopen('php://output', 'w');

        // CSV Header (like Android version: Date;Symbology;Data;Quantity)
        fputcsv($output, ['Date', 'Symbology', 'Data', 'Quantity'], ';');

        // Export each barcode entry
        foreach ($data as $barcode) {
            $scanTime = $barcode['scan_date'] ? date('H:i:s', strtotime($barcode['scan_date'])) : date('H:i:s');

            fputcsv($output, [
                $scanTime,
                $barcode['symbology_name'],
                $barcode['barcode_value'],
                $barcode['quantity']
            ], ';');
        }

        fclose($output);
    }

    private function exportAsXLSX($data, $filename) {
        try {
            // Create XLSX writer instance
            $xlsx = new SimpleXLSXWriter($filename);

            // Add header row (like Android version: Date;Symbology;Data;Quantity)
            $xlsx->addRow(['Date', 'Symbology', 'Data', 'Quantity']);

            // Add data rows
            foreach ($data as $barcode) {
                $scanTime = $barcode['scan_date'] ? date('H:i:s', strtotime($barcode['scan_date'])) : date('H:i:s');

                $xlsx->addRow([
                    $scanTime,
                    $barcode['symbology_name'],
                    $barcode['barcode_value'],
                    $barcode['quantity']
                ]);
            }

            // Output the XLSX file
            $xlsx->output();

        } catch (Exception $e) {
            // Fallback to CSV if XLSX generation fails
            error_log("XLSX export failed: " . $e->getMessage());

            // Change filename to CSV
            $csvFilename = str_replace('.xlsx', '.csv', $filename);
            $this->exportAsCSV($data, $csvFilename);
        }
    }

}

// Handle the request
$api = new ExportAPI();
$api->handleRequest();
?>
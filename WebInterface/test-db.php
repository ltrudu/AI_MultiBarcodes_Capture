<?php
echo "Testing database connection...\n";
echo "DB_HOST: " . ($_ENV['DB_HOST'] ?? 'not set') . "\n";
echo "DB_NAME: " . ($_ENV['DB_NAME'] ?? 'not set') . "\n";
echo "DB_USER: " . ($_ENV['DB_USER'] ?? 'not set') . "\n";

try {
    $dsn = "mysql:host=" . ($_ENV['DB_HOST'] ?? 'localhost') . ";port=3306;dbname=" . ($_ENV['DB_NAME'] ?? 'barcode_wms') . ";charset=utf8mb4";
    echo "DSN: " . $dsn . "\n";
    $pdo = new PDO($dsn, $_ENV['DB_USER'] ?? 'wms_user', $_ENV['DB_PASS'] ?? 'wms_password');
    echo "✅ Connection successful!\n";

    $result = $pdo->query("SELECT COUNT(*) as count FROM capture_sessions");
    $row = $result->fetch();
    echo "Sessions count: " . $row['count'] . "\n";

} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}
?>
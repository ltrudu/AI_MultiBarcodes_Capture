<?php
class Database {
    private $host;
    private $db_name;
    private $username;
    private $password;
    private $connection;

    public function __construct() {
        $this->host = $_ENV['DB_HOST'] ?? 'localhost';
        $this->db_name = $_ENV['DB_NAME'] ?? 'barcode_wms';
        $this->username = $_ENV['DB_USER'] ?? 'wms_user';
        $this->password = $_ENV['DB_PASS'] ?? 'wms_password';
    }

    public function getConnection() {
        $this->connection = null;

        try {
            $dsn = "mysql:host=" . $this->host . ";dbname=" . $this->db_name . ";charset=utf8mb4";
            $this->connection = new PDO($dsn, $this->username, $this->password);
            $this->connection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->connection->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
        } catch (PDOException $exception) {
            error_log("Connection error: " . $exception->getMessage());
            throw new Exception("Database connection failed");
        }

        return $this->connection;
    }

    public function closeConnection() {
        $this->connection = null;
    }
}
?>
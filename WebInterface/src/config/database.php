<?php
class Database {
    private $host;
    private $db_name;
    private $username;
    private $password;
    private $connection;

    public function __construct() {
        $this->host = '127.0.0.1';
        $this->db_name = 'barcode_wms';
        $this->username = 'wms_user';
        $this->password = 'wms_password';
    }

    public function getConnection() {
        $this->connection = null;

        try {
            $dsn = "mysql:host=" . $this->host . ";port=3306;dbname=" . $this->db_name . ";charset=utf8mb4";
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
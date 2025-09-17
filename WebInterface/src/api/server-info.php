<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

function getServerIPs() {
    $ips = array();

    // Get local network IP addresses
    $localIPs = array();

    // Method 0: Check if HOST_IP is provided via environment variable
    $hostIP = getenv('HOST_IP');
    if ($hostIP && filter_var($hostIP, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
        $localIPs[] = $hostIP;
    }

    // Method 1: Use hostname -I command (Linux/Unix)
    $output = shell_exec('hostname -I 2>/dev/null');
    if ($output) {
        $addresses = explode(' ', trim($output));
        foreach ($addresses as $ip) {
            if (filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4 | FILTER_FLAG_NO_PRIV_RANGE | FILTER_FLAG_NO_RES_RANGE)) {
                $localIPs[] = $ip;
            }
        }
    }

    // Method 2: Use ifconfig command
    if (empty($localIPs)) {
        $output = shell_exec('ifconfig 2>/dev/null | grep -oP "inet \K(\d+\.\d+\.\d+\.\d+)" | grep -v 127.0.0.1');
        if ($output) {
            $addresses = explode("\n", trim($output));
            foreach ($addresses as $ip) {
                if (filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4) && !preg_match('/^127\./', $ip) && !preg_match('/^169\.254\./', $ip)) {
                    $localIPs[] = $ip;
                }
            }
        }
    }

    // Method 3: Use ip command (modern Linux) - get host IP via route
    if (empty($localIPs)) {
        $output = shell_exec("ip route get 8.8.8.8 2>/dev/null | awk '{for(i=1;i<=NF;i++) if(\$i==\"src\") print \$(i+1)}'");
        if ($output) {
            $ip = trim($output);
            if (filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
                $localIPs[] = $ip;
            }
        }
    }

    // Method 3.5: Get host IP from Docker gateway
    if (empty($localIPs)) {
        $output = shell_exec("ip route | grep default | awk '{print \$3}' 2>/dev/null");
        if ($output) {
            $gateway = trim($output);
            if (filter_var($gateway, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
                // Try to resolve the local network from the gateway
                $networkBase = implode('.', array_slice(explode('.', $gateway), 0, 3));
                for ($i = 1; $i <= 254; $i++) {
                    $testIP = $networkBase . '.' . $i;
                    if ($testIP !== $gateway) {
                        // Check if this IP responds (simplified check)
                        $localIPs[] = $gateway; // Use gateway as fallback
                        break;
                    }
                }
            }
        }
    }

    // Method 4: Fallback - get server's hostname and resolve it
    if (empty($localIPs)) {
        $hostname = gethostname();
        $ip = gethostbyname($hostname);
        if ($ip !== $hostname && filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
            $localIPs[] = $ip;
        }
    }

    $ips['local'] = !empty($localIPs) ? $localIPs[0] : $_SERVER['SERVER_ADDR'] ?? '127.0.0.1';

    // Get external IP
    $externalIP = getExternalIP();
    $ips['external'] = $externalIP;

    return $ips;
}

function getExternalIP() {
    $services = [
        'http://ipinfo.io/ip',
        'http://icanhazip.com',
        'http://ident.me',
        'http://whatismyipaddress.com/api'
    ];

    $context = stream_context_create([
        'http' => [
            'timeout' => 10,
            'user_agent' => 'Mozilla/5.0 (compatible; ServerInfoBot/1.0)'
        ]
    ]);

    foreach ($services as $service) {
        try {
            $ip = @file_get_contents($service, false, $context);
            if ($ip) {
                $ip = trim($ip);
                if (filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
                    return $ip;
                }
            }
        } catch (Exception $e) {
            continue;
        }
    }

    return 'Unable to detect';
}

try {
    $serverIPs = getServerIPs();

    echo json_encode([
        'success' => true,
        'local_ip' => $serverIPs['local'],
        'external_ip' => $serverIPs['external'],
        'hostname' => gethostname(),
        'server_addr' => $_SERVER['SERVER_ADDR'] ?? 'unknown'
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ], JSON_PRETTY_PRINT);
}
?>
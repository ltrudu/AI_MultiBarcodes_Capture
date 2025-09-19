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

    // Helper function to check if IP is a Docker container IP
    function isDockerContainerIP($ip) {
        return preg_match('/^172\.(1[6-9]|2[0-9]|3[0-1])\./', $ip) ||
               preg_match('/^10\./', $ip);
    }

    // Helper function to check if IP is valid local network IP
    function isValidLocalIP($ip) {
        return filter_var($ip, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4) &&
               !preg_match('/^127\./', $ip) &&
               !preg_match('/^169\.254\./', $ip) &&
               !isDockerContainerIP($ip);
    }

    // Method 0: Check if HOST_IP is provided via environment variable (highest priority)
    $hostIP = getenv('HOST_IP');
    if ($hostIP && filter_var($hostIP, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
        $localIPs[] = $hostIP;
    }

    // Method 1: Try to get the host IP from Docker gateway route
    if (empty($localIPs)) {
        $output = shell_exec("ip route | grep default | awk '{print \$3}' 2>/dev/null");
        if ($output) {
            $gateway = trim($output);
            if (filter_var($gateway, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4)) {
                // The gateway IP is typically the host IP in Docker
                if (preg_match('/^192\.168\./', $gateway) || preg_match('/^10\./', $gateway)) {
                    $localIPs[] = $gateway;
                }
            }
        }
    }

    // Method 2: Use hostname -I command but filter out Docker IPs
    if (empty($localIPs)) {
        $output = shell_exec('hostname -I 2>/dev/null');
        if ($output) {
            $addresses = explode(' ', trim($output));
            foreach ($addresses as $ip) {
                $ip = trim($ip);
                if (isValidLocalIP($ip)) {
                    // Prioritize 192.168.x.x addresses (typical home/office networks)
                    if (preg_match('/^192\.168\./', $ip)) {
                        array_unshift($localIPs, $ip);
                    } else {
                        $localIPs[] = $ip;
                    }
                }
            }
        }
    }

    // Method 3: Use ifconfig command but filter out Docker IPs
    if (empty($localIPs)) {
        $output = shell_exec('ifconfig 2>/dev/null | grep -oP "inet \K(\d+\.\d+\.\d+\.\d+)" | grep -v 127.0.0.1');
        if ($output) {
            $addresses = explode("\n", trim($output));
            foreach ($addresses as $ip) {
                $ip = trim($ip);
                if (isValidLocalIP($ip)) {
                    // Prioritize 192.168.x.x addresses
                    if (preg_match('/^192\.168\./', $ip)) {
                        array_unshift($localIPs, $ip);
                    } else {
                        $localIPs[] = $ip;
                    }
                }
            }
        }
    }

    // Method 4: Use ip command to get source IP for external route (filtered)
    if (empty($localIPs)) {
        $output = shell_exec("ip route get 8.8.8.8 2>/dev/null | awk '{for(i=1;i<=NF;i++) if(\$i==\"src\") print \$(i+1)}'");
        if ($output) {
            $ip = trim($output);
            if (isValidLocalIP($ip)) {
                $localIPs[] = $ip;
            }
        }
    }

    // Method 5: Fallback - get server's hostname and resolve it
    if (empty($localIPs)) {
        $hostname = gethostname();
        $ip = gethostbyname($hostname);
        if ($ip !== $hostname && isValidLocalIP($ip)) {
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
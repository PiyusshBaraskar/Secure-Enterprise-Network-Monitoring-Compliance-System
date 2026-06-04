package com.enterprise.security.monitor.service;

import com.enterprise.security.monitor.entity.Device;
import com.enterprise.security.monitor.entity.NetworkScanResult;
import com.enterprise.security.monitor.repository.DeviceRepository;
import com.enterprise.security.monitor.repository.ScanResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
public class ScanService {

    @Autowired
    private ScanResultRepository scanResultRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LogService logService;

    // Strict validation to allow ONLY valid IPv4 addresses or CIDR blocks (e.g., 192.168.1.1 or 192.168.1.0/24)
    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])" +
            "(/([0-9]|[1-2][0-9]|3[0-2]))?$"
    );

    // List of simulated log outputs to show in terminal
    private final List<String> terminalOutputLogs = new ArrayList<>();
    private boolean isScanning = false;
    private int scanProgress = 0;

    public List<NetworkScanResult> getAllScanResults() {
        return scanResultRepository.findAllByOrderByScanTimeDesc();
    }

    public List<String> getTerminalLogs() {
        synchronized (terminalOutputLogs) {
            return new ArrayList<>(terminalOutputLogs);
        }
    }

    public boolean isScanning() {
        return this.isScanning;
    }

    public int getScanProgress() {
        return this.scanProgress;
    }

    public void clearScanState() {
        synchronized (terminalOutputLogs) {
            terminalOutputLogs.clear();
        }
        scanProgress = 0;
        isScanning = false;
    }

    public List<NetworkScanResult> runScan(String targetRange) {
        if (targetRange == null || !RANGE_PATTERN.matcher(targetRange.trim()).matches()) {
            throw new IllegalArgumentException("Invalid scan target format. Must be an IPv4 IP or CIDR range (e.g. 192.168.1.0/24).");
        }

        String target = targetRange.trim();
        this.isScanning = true;
        this.scanProgress = 10;
        
        synchronized (terminalOutputLogs) {
            terminalOutputLogs.clear();
            terminalOutputLogs.add("[*] SECURE DISCOVERY ENGINE: Starting network discovery scan on: " + target);
            terminalOutputLogs.add("[*] Checking system environment for native security tools...");
        }

        logService.writeLog("APPLICATION", "INFO", "Network scan requested on range: " + target, null);

        // Check if Nmap is installed and execute natively, otherwise fall back to detailed simulation
        if (isNmapAvailable()) {
            return runNativeNmapScan(target);
        } else {
            return runSimulatedScan(target);
        }
    }

    private boolean isNmapAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("nmap --version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private List<NetworkScanResult> runNativeNmapScan(String target) {
        List<NetworkScanResult> results = new ArrayList<>();
        synchronized (terminalOutputLogs) {
            terminalOutputLogs.add("[+] Native Nmap executable found! Executing safe active discovery scan...");
        }
        this.scanProgress = 30;

        try {
            // Safe execution: Only target parameter is appended, which has been strictly validated
            // -F performs a fast scan (top 100 ports), -O checks for OS (requires root/admin)
            // Let's run a standard safe scan: nmap -sV -F target
            ProcessBuilder pb = new ProcessBuilder("nmap", "-sV", "-F", target);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder nmapRawOutput = new StringBuilder();
            
            // Simple custom parsing of Nmap standard output
            String currentIp = null;
            String currentHostname = null;
            String currentOS = "Unknown OS";
            StringBuilder ports = new StringBuilder();
            StringBuilder services = new StringBuilder();

            this.scanProgress = 50;

            while ((line = reader.readLine()) != null) {
                nmapRawOutput.append(line).append("\n");
                synchronized (terminalOutputLogs) {
                    terminalOutputLogs.add(line);
                }

                if (line.startsWith("Nmap scan report for")) {
                    // Save previous host if exists
                    if (currentIp != null) {
                        NetworkScanResult res = saveScanResult(currentIp, currentHostname, currentOS, ports.toString(), services.toString());
                        results.add(res);
                        ports = new StringBuilder();
                        services = new StringBuilder();
                        currentOS = "Unknown OS";
                    }

                    // Parse IP and Hostname
                    // e.g. "Nmap scan report for core-router (192.168.1.1)" or "Nmap scan report for 192.168.1.1"
                    String details = line.substring(21).trim();
                    if (details.contains("(") && details.contains(")")) {
                        int idxOpen = details.indexOf("(");
                        int idxClose = details.indexOf(")");
                        currentHostname = details.substring(0, idxOpen).trim();
                        currentIp = details.substring(idxOpen + 1, idxClose).trim();
                    } else {
                        currentIp = details;
                        currentHostname = "Unknown Host";
                    }
                } else if (line.contains("/tcp") && line.contains("open")) {
                    // Parse ports and services
                    // e.g. "22/tcp  open  ssh" or "80/tcp  open  http  Apache httpd 2.4.41"
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 3) {
                        String portProto = tokens[0];
                        String port = portProto.split("/")[0];
                        String state = tokens[1];
                        String serviceName = tokens[2];
                        
                        if (ports.length() > 0) ports.append(",");
                        ports.append(port);

                        if (services.length() > 0) services.append(", ");
                        services.append(port).append(":").append(serviceName);
                    }
                } else if (line.startsWith("OS details:") || line.startsWith("Service Info: OS:")) {
                    currentOS = line.substring(line.indexOf(":") + 1).trim();
                }
            }

            // Save final host
            if (currentIp != null) {
                NetworkScanResult res = saveScanResult(currentIp, currentHostname, currentOS, ports.toString(), services.toString());
                results.add(res);
            }

            process.waitFor();
            this.scanProgress = 100;
            this.isScanning = false;
            
            logService.writeLog("APPLICATION", "INFO", "Native Nmap scan completed on " + target + ". Discovered " + results.size() + " hosts.", null);

        } catch (Exception e) {
            synchronized (terminalOutputLogs) {
                terminalOutputLogs.add("[!] Error executing native Nmap: " + e.getMessage());
                terminalOutputLogs.add("[*] Falling back to Security Scanner simulation...");
            }
            return runSimulatedScan(target);
        }

        return results;
    }

    private List<NetworkScanResult> runSimulatedScan(String target) {
        List<NetworkScanResult> results = new ArrayList<>();
        synchronized (terminalOutputLogs) {
            terminalOutputLogs.add("[!] Native Nmap not found. Initializing SOC Discovery Simulator...");
        }
        
        sleep(500);
        this.scanProgress = 20;

        synchronized (terminalOutputLogs) {
            terminalOutputLogs.add("Starting Nmap 7.92 ( https://nmap.org ) at 2026-06-02 23:44 GMT");
            terminalOutputLogs.add("Nmap scan report for range " + target);
            terminalOutputLogs.add("Host discovery initiated. Scanning subnet IPs...");
        }

        sleep(800);
        this.scanProgress = 40;

        // Extract base subnet from target
        String baseIp = target;
        if (target.contains("/")) {
            baseIp = target.substring(0, target.lastIndexOf("."));
        } else {
            baseIp = target.substring(0, target.lastIndexOf("."));
        }

        // Mock discovery based on common subnet layout
        // Let's check existing database devices in this range and overlay them!
        List<Device> dbDevices = deviceRepository.findAll();
        Map<String, Device> deviceMap = new HashMap<>();
        for (Device d : dbDevices) {
            deviceMap.put(d.getIpAddress(), d);
        }

        // IPs we will "discover"
        String[] hostIps = {
            baseIp + ".1",    // Router
            baseIp + ".5",    // Access Point
            baseIp + ".10",   // DC Server
            baseIp + ".20",   // HR DB Server
            baseIp + ".30",   // Finance Server
            baseIp + ".99",   // Analyst Laptop
            baseIp + ".101",  // Dev Workstation
            baseIp + ".105"   // Unmanaged attacker host! (trigger alert correlation)
        };

        int count = 0;
        for (String ip : hostIps) {
            if (results.size() >= 5) break; // Limit size of scan result output
            
            count++;
            this.scanProgress = 40 + (count * 10);
            
            Device matchingDevice = deviceMap.get(ip);
            String hostname = "Host-" + ip.substring(ip.lastIndexOf(".") + 1);
            String os = "Unknown OS";
            String ports = "";
            String services = "";

            if (matchingDevice != null) {
                hostname = matchingDevice.getName();
                os = matchingDevice.getOperatingSystem() != null ? matchingDevice.getOperatingSystem() : "Linux Kernel 4.X";
                
                // Assign realistic ports based on type
                if ("ROUTER".equalsIgnoreCase(matchingDevice.getDeviceType())) {
                    ports = "22,23,80,443";
                    services = "22:SSH, 23:Telnet, 80:HTTP, 443:HTTPS";
                } else if ("FIREWALL".equalsIgnoreCase(matchingDevice.getDeviceType())) {
                    ports = "80,443,8443";
                    services = "80:HTTP, 443:HTTPS, 8443:pfSense-WebGUI";
                } else if ("SERVER".equalsIgnoreCase(matchingDevice.getDeviceType())) {
                    if (matchingDevice.getName().contains("Active-Directory")) {
                        ports = "53,88,135,139,389,445,3389";
                        services = "53:DNS, 88:Kerberos, 135:RPC, 139:NetBIOS, 389:LDAP, 445:SMB, 3389:RDP";
                    } else if (matchingDevice.getName().contains("Database")) {
                        ports = "22,3306";
                        services = "22:SSH, 3306:MySQL";
                    } else {
                        ports = "22,80,443,8080";
                        services = "22:SSH, 80:HTTP, 443:HTTPS, 8080:HTTP-Proxy";
                    }
                } else if ("WORKSTATION".equalsIgnoreCase(matchingDevice.getDeviceType())) {
                    ports = "135,445,3389";
                    services = "135:RPC, 445:SMB, 3389:RDP";
                } else {
                    ports = "80,443";
                    services = "80:HTTP, 443:HTTPS";
                }
            } else {
                // Discover a rogue unmanaged device
                if (ip.endsWith(".105")) {
                    hostname = "rogue-kali-host";
                    os = "Kali Linux (Rolling Edition)";
                    ports = "22,80,443,8080";
                    services = "22:SSH, 80:HTTP, 443:HTTPS, 8080:Metasploit-Web";
                } else {
                    ports = "22,80";
                    services = "22:SSH, 80:HTTP";
                }
            }

            synchronized (terminalOutputLogs) {
                terminalOutputLogs.add("----------------------------------------------------------------------");
                terminalOutputLogs.add("Nmap scan report for " + hostname + " (" + ip + ")");
                terminalOutputLogs.add("Host is up (0.0024s latency).");
                terminalOutputLogs.add("Not shown: 96 closed tcp ports (reset)");
                terminalOutputLogs.add("PORT     STATE SERVICE VERSION");
                for (String serviceDetail : services.split(", ")) {
                    String[] parts = serviceDetail.split(":");
                    String p = parts[0] + "/tcp";
                    String svc = parts[1];
                    terminalOutputLogs.add(String.format("%-8s %-5s %-7s %s", p, "open", svc, "v2.0_simulated"));
                }
                terminalOutputLogs.add("MAC Address: " + (matchingDevice != null ? matchingDevice.getMacAddress() : "00:0C:29:" + ip.substring(ip.lastIndexOf(".") + 1) + ":FF:EE"));
                terminalOutputLogs.add("Device Type: " + (matchingDevice != null ? matchingDevice.getDeviceType() : "WORKSTATION"));
                terminalOutputLogs.add("OS details: " + os);
            }

            // Save to database
            NetworkScanResult res = saveScanResult(ip, hostname, os, ports, services);
            results.add(res);

            sleep(600);
        }

        this.scanProgress = 95;
        synchronized (terminalOutputLogs) {
            terminalOutputLogs.add("----------------------------------------------------------------------");
            terminalOutputLogs.add("Nmap scan report for target range: " + target);
            terminalOutputLogs.add("OS and Service detection performed. Discovered " + results.size() + " active devices.");
            terminalOutputLogs.add("Nmap done: 1 IP range (256 hosts scanned) -- " + results.size() + " hosts up.");
            terminalOutputLogs.add("[+] Network Discovery successfully completed! Results synced with inventory.");
        }

        this.scanProgress = 100;
        this.isScanning = false;
        
        logService.writeLog("APPLICATION", "INFO", "Simulated scan completed on " + target + ". Discovered " + results.size() + " hosts.", null);
        return results;
    }

    private NetworkScanResult saveScanResult(String ipAddress, String hostname, String os, String ports, String services) {
        NetworkScanResult result = new NetworkScanResult(ipAddress, hostname, os, ports, services);
        return scanResultRepository.save(result);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

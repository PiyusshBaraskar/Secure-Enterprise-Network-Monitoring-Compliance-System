package com.enterprise.security.monitor.service;

import com.enterprise.security.monitor.entity.SecurityAlert;
import com.enterprise.security.monitor.entity.SystemLog;
import com.enterprise.security.monitor.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@EnableScheduling
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private LogService logService;

    private final Random random = new Random();

    public List<SecurityAlert> getAllAlerts() {
        return alertRepository.findAllByOrderByTimestampDesc();
    }

    public List<SecurityAlert> getRecentAlerts() {
        return alertRepository.findTop10ByOrderByTimestampDesc();
    }

    public Optional<SecurityAlert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    public SecurityAlert saveAlert(SecurityAlert alert) {
        if (alert.getTimestamp() == null) {
            alert.setTimestamp(new Date());
        }
        return alertRepository.save(alert);
    }

    public void assignAlert(Long alertId, String username) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setAssignedTo(username);
        alert.setStatus("INVESTIGATING");
        alertRepository.save(alert);
        logService.writeLog("APPLICATION", "INFO", "Alert ID " + alertId + " assigned to user: " + username, null);
    }

    public void resolveAlert(Long alertId, String username) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus("RESOLVED");
        alert.setAssignedTo(username);
        alertRepository.save(alert);
        logService.writeLog("APPLICATION", "INFO", "Alert ID " + alertId + " resolved by user: " + username, null);
    }

    public void dismissAlert(Long alertId, String username) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus("DISMISSED");
        alert.setAssignedTo(username);
        alertRepository.save(alert);
        logService.writeLog("APPLICATION", "INFO", "Alert ID " + alertId + " dismissed by user: " + username, null);
    }

    public long getCriticalAlertCount() {
        return alertRepository.countBySeverity("CRITICAL");
    }

    public long countBySeverity(String severity) {
        return alertRepository.countBySeverity(severity);
    }

    public long countByStatus(String status) {
        return alertRepository.countByStatus(status);
    }

    // Background Scheduler - Simulates Snort/IDS alerts arriving in the SOC every 60 seconds
    @Scheduled(initialDelay = 30000, fixedRate = 60000)
    public void generateSimulatedSecurityEvents() {
        String[] alertTypes = {"PORT_SCAN", "BRUTE_FORCE", "SUSPICIOUS_LOGIN", "MALWARE_ACTIVITY", "FIREWALL_VIOLATION"};
        String alertType = alertTypes[random.nextInt(alertTypes.length)];
        
        SecurityAlert alert;
        String srcIp = "192.168.1." + (100 + random.nextInt(50));
        String destIp = "192.168.1." + (random.nextBoolean() ? "20" : "10"); // DC or DB server
        
        switch (alertType) {
            case "PORT_SCAN":
                alert = new SecurityAlert(
                        "PORT_SCAN", 
                        "Snort IDS: Nmap TCP SYN scan sweep detected from unmanaged host.",
                        srcIp, destIp, "HIGH", "NEW"
                );
                logService.writeLog("FIREWALL", "WARN", "SYN Flood / Port Scan attempt detected from " + srcIp + " on " + destIp, srcIp);
                break;
            case "BRUTE_FORCE":
                alert = new SecurityAlert(
                        "BRUTE_FORCE", 
                        "Fail2Ban: 10+ failed SSH login attempts detected on HR-Database-Server.",
                        srcIp, destIp, "CRITICAL", "NEW"
                );
                logService.writeLog("LINUX", "ERROR", "sshd[5891]: Failed password for invalid user root from " + srcIp + " port 49182 ssh2", destIp);
                break;
            case "SUSPICIOUS_LOGIN":
                alert = new SecurityAlert(
                        "SUSPICIOUS_LOGIN", 
                        "Windows Event Log: Multiple RDP connection failures followed by successful login outside work hours.",
                        srcIp, "192.168.1.10", "MEDIUM", "NEW"
                );
                logService.writeLog("WINDOWS", "WARN", "Event ID 4625: Audit Failure - User: Administrator, Logon: RDP from " + srcIp, "192.168.1.10");
                break;
            case "MALWARE_ACTIVITY":
                String wanIp = "198.51.100." + random.nextInt(254);
                alert = new SecurityAlert(
                        "MALWARE_ACTIVITY", 
                        "Snort IPS: Domain block - Outbound traffic to malicious Command & Control server detected.",
                        "192.168.1.101", wanIp, "CRITICAL", "NEW"
                );
                logService.writeLog("FIREWALL", "ERROR", "C2 Callout blocked: 192.168.1.101:50119 -> " + wanIp + ":443 (HTTPs)", "192.168.1.254");
                break;
            case "FIREWALL_VIOLATION":
            default:
                alert = new SecurityAlert(
                        "FIREWALL_VIOLATION", 
                        "pfSense: Firewall policy violated - Inbound traffic on sensitive database port blocked.",
                        "203.0.113." + random.nextInt(254), destIp, "MEDIUM", "NEW"
                );
                logService.writeLog("FIREWALL", "WARN", "Default Deny policy block: TCP 203.0.113.44:31294 -> " + destIp + ":3306 (MySQL)", "192.168.1.254");
                break;
        }

        alertRepository.save(alert);
    }
}

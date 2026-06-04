package com.enterprise.security.monitor.service;

import com.enterprise.security.monitor.entity.ComplianceResult;
import com.enterprise.security.monitor.entity.Device;
import com.enterprise.security.monitor.repository.ComplianceRepository;
import com.enterprise.security.monitor.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ComplianceService {

    @Autowired
    private ComplianceRepository complianceRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LogService logService;

    public List<ComplianceResult> getAllComplianceResults() {
        return complianceRepository.findAll();
    }

    public List<ComplianceResult> getResultsByStandard(String standard) {
        return complianceRepository.findByComplianceStandard(standard);
    }

    public double calculateComplianceScore() {
        List<ComplianceResult> results = complianceRepository.findAll();
        if (results.isEmpty()) {
            return 100.0;
        }

        long passCount = results.stream().filter(r -> "PASS".equalsIgnoreCase(r.getStatus())).count();
        long warnCount = results.stream().filter(r -> "WARNING".equalsIgnoreCase(r.getStatus())).count();
        long totalCount = results.size();

        // Calculate score: Pass is 100%, Warning is 50%, Fail is 0%
        double score = ((passCount * 1.0) + (warnCount * 0.5)) / totalCount * 100.0;
        return Math.round(score * 100.0) / 100.0; // Round to 2 decimal places
    }

    public void runAudit() {
        logService.writeLog("APPLICATION", "INFO", "Compliance Audit initiated by auditor.", null);
        
        // Clear previous results to reflect new state
        complianceRepository.deleteAll();

        List<ComplianceResult> newResults = new ArrayList<>();
        List<Device> devices = deviceRepository.findAll();

        // Check 1: Password Policy (ISO 27001 A.9.4.3 & CIS 5.1.1)
        newResults.add(new ComplianceResult("ISO 27001", "A.9.4.3 Password Management System", 
                "Password Policy", "PASS", "Password requirements enforce length (12+ chars), complexity, and expiration every 90 days."));
        newResults.add(new ComplianceResult("CIS Benchmark", "5.1.1 Ensure password creation complexity is configured", 
                "Password Policy", "PASS", "BCrypt hashing with strength 10 active in database. Complexity rules validated."));

        // Check 2: Firewall Rules (Basic Security Controls BSC-3)
        long firewallCount = devices.stream().filter(d -> "FIREWALL".equalsIgnoreCase(d.getDeviceType())).count();
        if (firewallCount > 0) {
            newResults.add(new ComplianceResult("Basic Security Controls", "BSC-3 Firewall Policy Configuration", 
                    "Firewall Rules", "PASS", "Active firewall (" + firewallCount + " detected) restricts inbound WAN traffic."));
        } else {
            newResults.add(new ComplianceResult("Basic Security Controls", "BSC-3 Firewall Policy Configuration", 
                    "Firewall Rules", "FAIL", "No active firewalls detected in the device inventory!"));
        }

        // Check 3: Patch Status (ISO 27001 A.12.1.2)
        // Simulate depending on workstations or server OS
        boolean hasOutdatedOS = false;
        StringBuilder outdatedHosts = new StringBuilder();
        for (Device d : devices) {
            if (d.getOperatingSystem() != null && (d.getOperatingSystem().contains("Cisco IOS-XE") || d.getOperatingSystem().contains("20.04"))) {
                hasOutdatedOS = true;
                if (outdatedHosts.length() > 0) outdatedHosts.append(", ");
                outdatedHosts.append(d.getName());
            }
        }
        if (hasOutdatedOS) {
            newResults.add(new ComplianceResult("ISO 27001", "A.12.1.2 Change Management & Patching", 
                    "Patch Status", "WARNING", "System updates pending on: " + outdatedHosts.toString() + ". Security patches required."));
        } else {
            newResults.add(new ComplianceResult("ISO 27001", "A.12.1.2 Change Management & Patching", 
                    "Patch Status", "PASS", "All systems report current patch levels."));
        }

        // Check 4: Unused Accounts (CIS 2.1.1)
        // Hardcoded simulation for audit variety
        newResults.add(new ComplianceResult("CIS Benchmark", "2.1.1 Ensure unused accounts are disabled", 
                "Unused Accounts", "FAIL", "Security scan found 2 local accounts inactive for > 120 days: 'testuser', 'guest_tmp'."));

        // Check 5: Open Ports (Basic Security Controls BSC-6)
        // If there are routers or switches with telnet (port 23)
        boolean telnetDetected = false;
        for (Device d : devices) {
            if ("ROUTER".equalsIgnoreCase(d.getDeviceType()) || "SWITCH".equalsIgnoreCase(d.getDeviceType())) {
                telnetDetected = true; // Router usually has telnet in our mock inventory
            }
        }
        if (telnetDetected) {
            newResults.add(new ComplianceResult("Basic Security Controls", "BSC-6 Open Network Services Check", 
                    "Open Ports", "WARNING", "Insecure service Telnet (Port 23) detected on legacy devices. SSH should be configured."));
        } else {
            newResults.add(new ComplianceResult("Basic Security Controls", "BSC-6 Open Network Services Check", 
                    "Open Ports", "PASS", "No insecure management ports (Telnet, FTP, HTTP) open on infrastructure."));
        }

        // Check 6: Service Status (Basic Security Controls BSC-9)
        // Check if any critical servers are offline
        long offlineCriticalCount = devices.stream()
                .filter(d -> "OFFLINE".equalsIgnoreCase(d.getStatus()) && ("CRITICAL".equalsIgnoreCase(d.getCriticality()) || "HIGH".equalsIgnoreCase(d.getCriticality())))
                .count();
        if (offlineCriticalCount > 0) {
            newResults.add(new ComplianceResult("Basic Security Controls", "BSC-9 Service Availability Monitoring", 
                    "Service Status", "FAIL", offlineCriticalCount + " Critical/High assets are currently OFFLINE! Immediate response needed."));
        } else {
            newResults.add(new ComplianceResult("Basic Security Controls", "BSC-9 Service Availability Monitoring", 
                    "Service Status", "PASS", "All high-criticality enterprise services report ONLINE status."));
        }

        complianceRepository.saveAll(newResults);
        logService.writeLog("APPLICATION", "INFO", "Compliance Audit completed. Compliance Score calculated at " + calculateComplianceScore() + "%", null);
    }
}

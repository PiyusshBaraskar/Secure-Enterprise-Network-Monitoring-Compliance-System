package com.enterprise.security.monitor.config;

import com.enterprise.security.monitor.entity.*;
import com.enterprise.security.monitor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private ComplianceRepository complianceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedSecurityData();
        seedDevices();
        seedAlerts();
        seedLogs();
        seedComplianceResults();
    }

    private void seedSecurityData() {
        if (permissionRepository.count() > 0) {
            return; // Data already seeded
        }

        // 1. Create Permissions
        Permission readDashboard = permissionRepository.save(new Permission("READ_DASHBOARD", "View the operations dashboard"));
        Permission manageDevices = permissionRepository.save(new Permission("MANAGE_DEVICES", "Add, edit, or delete devices in inventory"));
        Permission runScans = permissionRepository.save(new Permission("RUN_SCANS", "Trigger network discovery scans"));
        Permission viewAlerts = permissionRepository.save(new Permission("VIEW_ALERTS", "View and manage security alerts"));
        Permission viewLogs = permissionRepository.save(new Permission("VIEW_LOGS", "View and export system logs"));
        Permission runAudits = permissionRepository.save(new Permission("RUN_AUDITS", "Execute compliance checks"));
        Permission generateReports = permissionRepository.save(new Permission("GENERATE_REPORTS", "Create and download reports"));
        Permission manageUsers = permissionRepository.save(new Permission("MANAGE_USERS", "Administrative user management"));

        // 2. Create Roles
        Role adminRole = new Role("ROLE_ADMIN", "Enterprise Administrator with full systems access");
        adminRole.getPermissions().addAll(Arrays.asList(
                readDashboard, manageDevices, runScans, viewAlerts, viewLogs, runAudits, generateReports, manageUsers
        ));
        roleRepository.save(adminRole);

        Role analystRole = new Role("ROLE_ANALYST", "Security Analyst focused on security operations");
        analystRole.getPermissions().addAll(Arrays.asList(
                readDashboard, manageDevices, runScans, viewAlerts, viewLogs
        ));
        roleRepository.save(analystRole);

        Role auditorRole = new Role("ROLE_AUDITOR", "Compliance Auditor with auditing access");
        auditorRole.getPermissions().addAll(Arrays.asList(
                readDashboard, viewLogs, runAudits, generateReports
        ));
        roleRepository.save(auditorRole);

        // 3. Create Users
        User adminUser = new User("admin", passwordEncoder.encode("password123"), "admin@enterprise.com", true);
        adminUser.getRoles().add(adminRole);
        userRepository.save(adminUser);

        User analystUser = new User("analyst", passwordEncoder.encode("password123"), "analyst@enterprise.com", true);
        analystUser.getRoles().add(analystRole);
        userRepository.save(analystUser);

        User auditorUser = new User("auditor", passwordEncoder.encode("password123"), "auditor@enterprise.com", true);
        auditorUser.getRoles().add(auditorRole);
        userRepository.save(auditorUser);
    }

    private void seedDevices() {
        if (deviceRepository.count() > 0) {
            return;
        }

        List<Device> devices = new ArrayList<>();
        devices.add(new Device("Core-Router-01", "192.168.1.1", "00:1A:2B:3C:4D:5E", "ROUTER", "Cisco IOS-XE", "HQ Server Room Rack A", "Network Ops Team", "CRITICAL", "ONLINE", new Date()));
        devices.add(new Device("HQ-Edge-Firewall", "192.168.1.254", "00:11:22:33:44:55", "FIREWALL", "pfSense 2.6", "HQ Server Room Rack A", "Security Ops Team", "CRITICAL", "ONLINE", new Date()));
        devices.add(new Device("Active-Directory-DC", "192.168.1.10", "00:50:56:A1:B2:C3", "SERVER", "Windows Server 2022", "HQ Virtual Host 1", "IT Admin Team", "CRITICAL", "ONLINE", new Date()));
        devices.add(new Device("HR-Database-Server", "192.168.1.20", "00:50:56:B2:C3:D4", "SERVER", "Ubuntu Server 20.04 LTS", "HQ Virtual Host 2", "HR Department", "HIGH", "ONLINE", new Date()));
        devices.add(new Device("Developer-Workstation-01", "192.168.1.101", "A0:B1:C2:D3:E4:F5", "WORKSTATION", "Windows 11 Enterprise", "HQ 2nd Floor Cubicle 12", "John Doe (Dev)", "LOW", "ONLINE", new Date()));
        devices.add(new Device("Security-Analyst-Laptop", "192.168.1.99", "F0:E1:D2:C3:B2:A1", "WORKSTATION", "Kali Linux 2023.3", "HQ Security Lab", "Jane Smith (SOC)", "MEDIUM", "ONLINE", new Date()));
        devices.add(new Device("Corp-WiFi-AP-01", "192.168.1.5", "10:D0:AB:CD:EF:12", "ACCESS_POINT", "Ubiquiti UniFi AP-AC-Pro", "HQ 1st Floor Ceiling", "Network Ops Team", "MEDIUM", "ONLINE", new Date()));
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        devices.add(new Device("Finance-App-Server", "192.168.1.30", "00:50:56:C4:D5:E6", "SERVER", "Red Hat Enterprise Linux 8", "HQ Virtual Host 2", "Finance Admin", "HIGH", "OFFLINE", cal.getTime()));

        deviceRepository.saveAll(devices);
    }

    private void seedAlerts() {
        if (alertRepository.count() > 0) {
            return;
        }

        List<SecurityAlert> alerts = new ArrayList<>();
        alerts.add(new SecurityAlert("PORT_SCAN", "Rapid connection attempts detected on multiple ports from an unmanaged host.", "192.168.1.105", "192.168.1.20", "HIGH", "NEW"));
        alerts.add(new SecurityAlert("BRUTE_FORCE", "Multiple failed SSH login attempts for user root on HR-Database-Server within 10 seconds.", "192.168.1.105", "192.168.1.20", "CRITICAL", "INVESTIGATING"));
        
        SecurityAlert a3 = new SecurityAlert("FIREWALL_VIOLATION", "HQ-Edge-Firewall blocked traffic attempting to access internal database on port 3306 directly from WAN.", "203.0.113.45", "192.168.1.20", "MEDIUM", "RESOLVED");
        a3.setAssignedTo("admin");
        alerts.add(a3);
        
        alerts.add(new SecurityAlert("SUSPICIOUS_LOGIN", "Administrator logged in from an unusual IP address outside business hours.", "192.168.1.101", "192.168.1.10", "MEDIUM", "NEW"));
        alerts.add(new SecurityAlert("MALWARE_ACTIVITY", "Inbound connection to known malicious Command and Control (C2) IP domain blocked.", "192.168.1.101", "198.51.100.12", "CRITICAL", "NEW"));

        alertRepository.saveAll(alerts);
    }

    private void seedLogs() {
        if (logRepository.count() > 0) {
            return;
        }

        List<SystemLog> logs = new ArrayList<>();
        logs.add(new SystemLog("FIREWALL", "INFO", "Rule ALLOW_LAN_WAN: TCP 192.168.1.101:53214 -> 8.8.8.8:443 established.", "192.168.1.254"));
        logs.add(new SystemLog("FIREWALL", "WARN", "Rule BLOCK_WAN_LAN: TCP 203.0.113.88:42139 -> 192.168.1.10:3389 (RDP) blocked.", "192.168.1.254"));
        logs.add(new SystemLog("LINUX", "INFO", "sshd[41209]: Connection closed by authenticating user root 192.168.1.105 port 51239 [preauth]", "192.168.1.20"));
        logs.add(new SystemLog("LINUX", "ERROR", "sshd[41212]: pam_unix(sshd:auth): authentication failure; logname= uid=0 euid=0 tty=ssh ruser= rhost=192.168.1.105 user=root", "192.168.1.20"));
        logs.add(new SystemLog("WINDOWS", "INFO", "Event ID 4624: An account was successfully logged on. Account Name: Administrator, Logon Type: 3.", "192.168.1.10"));
        logs.add(new SystemLog("WINDOWS", "WARN", "Event ID 4625: An account failed to log on. Account Name: Guest, Failure Reason: Unknown user name or bad password.", "192.168.1.10"));
        logs.add(new SystemLog("APPLICATION", "INFO", "Spring Boot Security context initialized. Active Directory provider verified.", "192.168.1.10"));
        logs.add(new SystemLog("APPLICATION", "ERROR", "Database connection pool timed out trying to connect to Finance DB Server.", "192.168.1.30"));

        logRepository.saveAll(logs);
    }

    private void seedComplianceResults() {
        if (complianceRepository.count() > 0) {
            return;
        }

        List<ComplianceResult> results = new ArrayList<>();
        results.add(new ComplianceResult("ISO 27001", "A.9.4.3 Password Management System", "Password Policy", "PASS", "Password requirements enforce length (12+ chars), complexity, and expiration every 90 days."));
        results.add(new ComplianceResult("ISO 27001", "A.12.1.2 Change Management", "Patch Status", "WARNING", "HR-Database-Server has 4 security patches pending. Core systems up to date."));
        results.add(new ComplianceResult("CIS Benchmark", "5.1.1 Ensure password creation complexity is configured", "Password Policy", "PASS", "Strong password hashing using BCrypt and password requirements verified."));
        results.add(new ComplianceResult("CIS Benchmark", "2.1.1 Ensure unused accounts are disabled", "Unused Accounts", "FAIL", "Found 2 accounts inactive for > 120 days: testuser, guest_tmp."));
        results.add(new ComplianceResult("Basic Security Controls", "BSC-3 Firewall Policy Configuration", "Firewall Rules", "PASS", "Edge firewall denies all inbound WAN traffic by default. Port forwarding restricted to SSL/VPN."));
        results.add(new ComplianceResult("Basic Security Controls", "BSC-6 Open Network Services Check", "Open Ports", "WARNING", "Telnet (Port 23) detected open on legacy switch 192.168.1.55. SSH should be configured."));

        complianceRepository.saveAll(results);
    }
}

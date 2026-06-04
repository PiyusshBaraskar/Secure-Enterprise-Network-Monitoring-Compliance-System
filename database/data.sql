-- ===================================================================
-- SECURE ENTERPRISE NETWORK MONITORING AND COMPLIANCE MANAGEMENT SYSTEM
-- REFERENCE SAMPLE DATA SCRIPT (MYSQL)
-- ===================================================================

USE security_monitor_db;

-- 1. Insert Permissions
INSERT INTO permissions (name, description) VALUES
('READ_DASHBOARD', 'View the operations dashboard'),
('MANAGE_DEVICES', 'Add, edit, or delete devices in inventory'),
('RUN_SCANS', 'Trigger network discovery scans'),
('VIEW_ALERTS', 'View and manage security alerts'),
('VIEW_LOGS', 'View and export system logs'),
('RUN_AUDITS', 'Execute compliance checks'),
('GENERATE_REPORTS', 'Create and download reports'),
('MANAGE_USERS', 'Administrative user management')
ON DUPLICATE KEY UPDATE name=name;

-- 2. Insert Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Enterprise Administrator with full systems access'),
('ROLE_ANALYST', 'Security Analyst focused on device management, scanning, alerts, and logs'),
('ROLE_AUDITOR', 'Compliance Auditor with access to audits, logs, and reporting')
ON DUPLICATE KEY UPDATE name=name;

-- 3. Link Roles and Permissions
-- Admin (All permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- Security Analyst (Dashboard, Devices, Scans, Alerts, Logs)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'ROLE_ANALYST' AND p.name IN ('READ_DASHBOARD', 'MANAGE_DEVICES', 'RUN_SCANS', 'VIEW_ALERTS', 'VIEW_LOGS')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- Auditor (Dashboard, Logs, Audits, Reports)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p 
WHERE r.name = 'ROLE_AUDITOR' AND p.name IN ('READ_DASHBOARD', 'VIEW_LOGS', 'RUN_AUDITS', 'GENERATE_REPORTS')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 4. Insert Default Users
-- All default passwords are 'password123'
-- BCrypt hash for 'password123': $2a$10$eM4XQ9pC4H5T8J9QzXmB/O1uT.mHqV/B/BvLmWm3.yF4Wp.x1R1aC
INSERT INTO users (username, password, email, enabled) VALUES
('admin', '$2a$10$eM4XQ9pC4H5T8J9QzXmB/O1uT.mHqV/B/BvLmWm3.yF4Wp.x1R1aC', 'admin@enterprise.com', true),
('analyst', '$2a$10$eM4XQ9pC4H5T8J9QzXmB/O1uT.mHqV/B/BvLmWm3.yF4Wp.x1R1aC', 'analyst@enterprise.com', true),
('auditor', '$2a$10$eM4XQ9pC4H5T8J9QzXmB/O1uT.mHqV/B/BvLmWm3.yF4Wp.x1R1aC', 'auditor@enterprise.com', true)
ON DUPLICATE KEY UPDATE username=username;

-- Link Users to Roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_ADMIN' WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id=user_id;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_ANALYST' WHERE u.username = 'analyst'
ON DUPLICATE KEY UPDATE user_id=user_id;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'ROLE_AUDITOR' WHERE u.username = 'auditor'
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 5. Insert Sample Devices (Asset Inventory)
INSERT INTO devices (name, ip_address, mac_address, device_type, operating_system, location, owner, criticality, status, last_seen) VALUES
('Core-Router-01', '192.168.1.1', '00:1A:2B:3C:4D:5E', 'ROUTER', 'Cisco IOS-XE', 'HQ Server Room Rack A', 'Network Ops Team', 'CRITICAL', 'ONLINE', NOW()),
('HQ-Edge-Firewall', '192.168.1.254', '00:11:22:33:44:55', 'FIREWALL', 'pfSense 2.6', 'HQ Server Room Rack A', 'Security Ops Team', 'CRITICAL', 'ONLINE', NOW()),
('Active-Directory-DC', '192.168.1.10', '00:50:56:A1:B2:C3', 'SERVER', 'Windows Server 2022', 'HQ Virtual Host 1', 'IT Admin Team', 'CRITICAL', 'ONLINE', NOW()),
('HR-Database-Server', '192.168.1.20', '00:50:56:B2:C3:D4', 'SERVER', 'Ubuntu Server 20.04 LTS', 'HQ Virtual Host 2', 'HR Department', 'HIGH', 'ONLINE', NOW()),
('Developer-Workstation-01', '192.168.1.101', 'A0:B1:C2:D3:E4:F5', 'WORKSTATION', 'Windows 11 Enterprise', 'HQ 2nd Floor Cubicle 12', 'John Doe (Dev)', 'LOW', 'ONLINE', NOW()),
('Security-Analyst-Laptop', '192.168.1.99', 'F0:E1:D2:C3:B2:A1', 'WORKSTATION', 'Kali Linux 2023.3', 'HQ Security Lab', 'Jane Smith (SOC)', 'MEDIUM', 'ONLINE', NOW()),
('Corp-WiFi-AP-01', '192.168.1.5', '10:D0:AB:CD:EF:12', 'ACCESS_POINT', 'Ubiquiti UniFi AP-AC-Pro', 'HQ 1st Floor Ceiling', 'Network Ops Team', 'MEDIUM', 'ONLINE', NOW()),
('Finance-App-Server', '192.168.1.30', '00:50:56:C4:D5:E6', 'SERVER', 'Red Hat Enterprise Linux 8', 'HQ Virtual Host 2', 'Finance Admin', 'HIGH', 'OFFLINE', NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE ip_address=ip_address;

-- 6. Insert Sample Security Alerts
INSERT INTO security_alerts (alert_type, description, source_ip, destination_ip, severity, status, assigned_to) VALUES
('PORT_SCAN', 'Rapid connection attempts detected on multiple ports from an unmanaged host.', '192.168.1.105', '192.168.1.20', 'HIGH', 'NEW', NULL),
('BRUTE_FORCE', 'Multiple failed SSH login attempts for user root on HR-Database-Server within 10 seconds.', '192.168.1.105', '192.168.1.20', 'CRITICAL', 'INVESTIGATING', 'analyst'),
('FIREWALL_VIOLATION', 'HQ-Edge-Firewall blocked traffic attempting to access internal database on port 3306 directly from WAN.', '203.0.113.45', '192.168.1.20', 'MEDIUM', 'RESOLVED', 'admin'),
('SUSPICIOUS_LOGIN', 'Administrator logged in from an unusual IP address outside business hours.', '192.168.1.101', '192.168.1.10', 'MEDIUM', 'NEW', NULL),
('MALWARE_ACTIVITY', 'Inbound connection to known malicious Command and Control (C2) IP domain blocked.', '192.168.1.101', '198.51.100.12', 'CRITICAL', 'NEW', NULL)
ON DUPLICATE KEY UPDATE id=id;

-- 7. Insert Sample Logs
INSERT INTO system_logs (log_source, log_level, message, ip_address) VALUES
('FIREWALL', 'INFO', 'Rule ALLOW_LAN_WAN: TCP 192.168.1.101:53214 -> 8.8.8.8:443 established.', '192.168.1.254'),
('FIREWALL', 'WARN', 'Rule BLOCK_WAN_LAN: TCP 203.0.113.88:42139 -> 192.168.1.10:3389 (RDP) blocked.', '192.168.1.254'),
('LINUX', 'INFO', 'sshd[41209]: Connection closed by authenticating user root 192.168.1.105 port 51239 [preauth]', '192.168.1.20'),
('LINUX', 'ERROR', 'sshd[41212]: pam_unix(sshd:auth): authentication failure; logname= uid=0 euid=0 tty=ssh ruser= rhost=192.168.1.105  user=root', '192.168.1.20'),
('WINDOWS', 'INFO', 'Event ID 4624: An account was successfully logged on. Account Name: Administrator, Logon Type: 3.', '192.168.1.10'),
('WINDOWS', 'WARN', 'Event ID 4625: An account failed to log on. Account Name: Guest, Failure Reason: Unknown user name or bad password.', '192.168.1.10'),
('APPLICATION', 'INFO', 'Spring Boot Security context initialized. Active Directory provider verified.', '192.168.1.10'),
('APPLICATION', 'ERROR', 'Database connection pool timed out trying to connect to Finance DB Server.', '192.168.1.30')
ON DUPLICATE KEY UPDATE id=id;

-- 8. Insert Sample Compliance Audit Results
INSERT INTO compliance_results (compliance_standard, check_name, check_category, status, details) VALUES
('ISO 27001', 'A.9.4.3 Password Management System', 'Password Policy', 'PASS', 'Password requirements enforce length (12+ chars), complexity, and expiration every 90 days.'),
('ISO 27001', 'A.12.1.2 Change Management', 'Patch Status', 'WARNING', 'HR-Database-Server has 4 security patches pending. Core systems up to date.'),
('CIS Benchmark', '5.1.1 Ensure password creation complexity is configured', 'Password Policy', 'PASS', 'Strong password hashing using BCrypt and password requirements verified.'),
('CIS Benchmark', '2.1.1 Ensure unused accounts are disabled', 'Unused Accounts', 'FAIL', 'Found 2 accounts inactive for > 120 days: testuser, guest_tmp.'),
('Basic Security Controls', 'BSC-3 Firewall Policy Configuration', 'Firewall Rules', 'PASS', 'Edge firewall denies all inbound WAN traffic by default. Port forwarding restricted to SSL/VPN.'),
('Basic Security Controls', 'BSC-6 Open Network Services Check', 'Open Ports', 'WARNING', 'Telnet (Port 23) detected open on legacy switch 192.168.1.55. SSH should be configured.')
ON DUPLICATE KEY UPDATE id=id;

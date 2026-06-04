# Secure Enterprise Network Monitoring and Compliance Management System

A comprehensive enterprise-grade security portal simulating operations of a Security Operations Center (SOC), Network Operations Center (NOC), and Compliance Audit Team. Developed using Java 8, Spring Boot 2.7.x, Maven, Thymeleaf, and Bootstrap 5.

---

## Key Features

1. **Granular RBAC Security**: Admin, Security Analyst, and Compliance Auditor roles linked to specific, fine-grained access permissions.
2. **Device Inventory Management**: Add, update, search, delete, and classify devices by type (Router, Firewall, Server, Workstation, Switch, Access Point) and criticality level (Critical, High, Medium, Low).
3. **Interactive Network Discovery**: A safe discovery engine. Integrates Nmap command invocation (with input sanitization against command injection) or falls back to a realistic SOC scanning simulation that outputs step-by-step terminal logs.
4. **Security Alerts Queue**: Incident management system featuring color-coded severities (Critical, High, Medium, Low). Supports assigning handler ownership, resolving, or dismissing security alerts. Contains an automated background scheduler that generates simulated IDS/IPS security events.
5. **Telemetry Log Management**: Centralized log storage for Windows Event logs, Linux Syslogs, Firewall logs, and Application logs. Offers robust searching, level filters, and live CSV data downloads.
6. **Compliance Standards Auditor**: Scans inventory configurations against ISO 27001, CIS Benchmarks, and Basic Security Controls standards, compiling a compliance percentage score.
7. **Report Compiler**: Compiles executive daily/weekly/monthly reports into PDF format (using OpenPDF layout engines) and raw CSV spreadsheets.

---

## Technology Stack
* **Backend**: Java 8, Spring Boot 2.7.18, Spring Security, Spring Data JPA, Maven
* **Frontend**: HTML5, Thymeleaf, CSS3, Bootstrap 5, FontAwesome 6, Chart.js 3.9
* **Databases**: MySQL 8.0 (Primary), H2 (In-memory fallback for testing)

---

## Setup & Running Guide

### Prerequisites
* Java Development Kit (JDK) 8
* MySQL Server (optional, H2 is configured as default for zero-setup execution)
* Docker & Docker Compose (optional, for containerized run)

### Running Locally (Out of the Box / H2)
The application is pre-configured to run on an in-memory database out of the box so you can verify it instantly:
1. Open terminal in the project directory:
   `C:\Users\TUF GAMING\.gemini\antigravity-ide\scratch\secure-network-monitor`
2. Run the application using the Maven Wrapper:
   `mvnw.cmd spring-boot:run`
3. Access the portal:
   `http://localhost:8080`

### Running Locally with MySQL
To connect the application to a real MySQL instance:
1. Create a schema named `security_monitor_db` in MySQL or run the schema script located in `database/schema.sql`.
2. Edit `src/main/resources/application-mysql.properties` to specify your MySQL port, username, and password:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_secure_password
   ```
3. Run the application with the `mysql` profile:
   `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql`

### Running via Docker Compose
To deploy both MySQL 8 and the Spring Boot application inside container network:
1. Ensure Docker Desktop is running.
2. In the project directory, run:
   `docker-compose up --build -d`
3. Access the application on `http://localhost:8080`.

---

## Demo Accounts (Passwords: `password123`)

* **Administrator (admin)**: Has access to all modules, including device inventory editing, Nmap scans, security event management, logs, compliance audits, reports, and H2 developer console.
* **Security Analyst (analyst)**: Focuses on operations. Can manage devices, trigger discovery scans, and assign/resolve security alerts. Restricted from running compliance audits or downloading audit reports.
* **Compliance Auditor (auditor)**: Has access to read dashboards, system logs, trigger compliance audits, and generate/download reports. Restricted from registering devices or running network scans.

---

## Directory Layout
* `database/`: Reference DDL schema and DML sample insert scripts.
* `src/main/java/com/enterprise/security/monitor/`: Backend source code.
  * `config/`: Spring Security configurations and Database Seeder.
  * `controller/`: MVC View controllers and REST endpoints.
  * `entity/`: JPA entities representing database tables.
  * `repository/`: Spring Data JPA interfaces.
  * `service/`: Core business logic services.
* `src/main/resources/templates/`: Front-end HTML templates utilizing Thymeleaf fragment nesting.
* `src/main/resources/static/`: Stylesheets and Chart.js frontend scripts.

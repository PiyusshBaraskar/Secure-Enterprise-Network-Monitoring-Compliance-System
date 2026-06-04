# REST API Documentation - Secure Enterprise Network Monitor

This document details the HTTP REST API endpoints exposed by the backend of the Secure Enterprise Network Monitoring and Compliance Management System.

All request and response bodies use standard JSON structure (except where file downloads or form submissions are explicitly noted).

---

## 1. Authentication

### Submit Login (Spring Security)
Processes credentials and creates a session cookie.
* **URL:** `/login`
* **Method:** `POST`
* **Content-Type:** `application/x-www-form-urlencoded`
* **Form Parameters:**
  * `username` (string)
  * `password` (string)
* **Success Response:** Redirection to `/dashboard` (Status: `302 Found` with cookie `JSESSIONID`).
* **Failure Response:** Redirection to `/login?error=true` (Status: `302 Found`).

### Log Out
Destroys active user session.
* **URL:** `/logout`
* **Method:** `POST`
* **Success Response:** Redirection to `/login?logout=true`.

---

## 2. Device Inventory Management

### Get Device for Edit
Retrieve details of a single device. Used by the editing modal.
* **URL:** `/devices/edit/{id}`
* **Method:** `GET`
* **Headers:** Required authenticated session.
* **Success Response (200 OK):**
  ```json
  {
    "id": 4,
    "name": "HR-Database-Server",
    "ipAddress": "192.168.1.20",
    "macAddress": "00:50:56:B2:C3:D4",
    "deviceType": "SERVER",
    "operatingSystem": "Ubuntu Server 20.04 LTS",
    "location": "HQ Virtual Host 2",
    "owner": "HR Department",
    "criticality": "HIGH",
    "status": "ONLINE",
    "lastSeen": "2026-06-02T18:13:00.000+00:00"
  }
  ```
* **Error Response (404 Not Found):** If ID doesn't exist.

---

## 3. Network Discovery

### Trigger Scan
Spawns a network scan thread on a specified IP address or CIDR range.
* **URL:** `/scans/run`
* **Method:** `POST`
* **Content-Type:** `application/x-www-form-urlencoded`
* **Required Authority:** `RUN_SCANS`
* **Form Parameters:**
  * `targetRange` (string, e.g. `192.168.1.0/24`)
* **Success Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Scan started for target: 192.168.1.0/24"
  }
  ```
* **Failure Response (400 Bad Request):** If target targetRange violates security filters or if a scan is already running.

### Get Scan Status
Polls the active scan engine. Allows terminal log stream and progress bar updates.
* **URL:** `/scans/status`
* **Method:** `GET`
* **Success Response (200 OK):**
  ```json
  {
    "isScanning": true,
    "progress": 40,
    "logs": [
      "[*] SECURE DISCOVERY ENGINE: Starting network discovery scan...",
      "Starting Nmap 7.92 ( https://nmap.org ) at 2026-06-02 23:44 GMT",
      "Nmap scan report for range 192.168.1.0/24",
      "Host discovery initiated. Scanning subnet IPs..."
    ]
  }
  ```

---

## 4. Security Event Management

### Assign Alert
Assigns an unassigned security alert to the logged-in analyst.
* **URL:** `/alerts/assign/{id}`
* **Method:** `GET`
* **Required Authority:** `VIEW_ALERTS`
* **Success Response:** Redirect to `/alerts` (Updates status to `INVESTIGATING` and assigns user).

### Resolve Alert
Closes a security incident alert.
* **URL:** `/alerts/resolve/{id}`
* **Method:** `GET`
* **Required Authority:** `VIEW_ALERTS`
* **Success Response:** Redirect to `/alerts` (Updates status to `RESOLVED`).

### Dismiss Alert
Marks alert as false positive or dismisses it.
* **URL:** `/alerts/dismiss/{id}`
* **Method:** `GET`
* **Required Authority:** `VIEW_ALERTS`
* **Success Response:** Redirect to `/alerts` (Updates status to `DISMISSED`).

---

## 5. Log Management

### Export Logs (CSV)
Streams a CSV file containing logs matching the supplied filters directly to the client.
* **URL:** `/logs/export`
* **Method:** `GET`
* **Required Authority:** `VIEW_LOGS`
* **Query Parameters (Optional):**
  * `source` (string, e.g. `WINDOWS`)
  * `level` (string, e.g. `ERROR`)
  * `ipAddress` (string)
  * `message` (string)
* **Success Response (200 OK):** Binary file stream with header `Content-Disposition: attachment; filename="System_Logs_*.csv"` and Content-Type `text/csv`.

---

## 6. Report Generation

### Download Report File
Streams the compiled report (PDF or CSV) matching the database ID from the server disk.
* **URL:** `/reports/download/{id}`
* **Method:** `GET`
* **Required Authority:** `GENERATE_REPORTS`
* **Success Response (200 OK):** Binary stream containing file contents, content-length, and headers `Content-Disposition: attachment; filename="..."` and Content-Type (`application/pdf` or `text/csv`).
* **Error Response (404 Not Found):** If report record or physical file is missing.

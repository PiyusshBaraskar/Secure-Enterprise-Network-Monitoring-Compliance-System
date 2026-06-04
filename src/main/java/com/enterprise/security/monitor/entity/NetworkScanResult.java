package com.enterprise.security.monitor.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "network_scan_results")
public class NetworkScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String hostname;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ports; // Comma-separated list of ports e.g. "22,80,443"

    @Lob
    @Column(columnDefinition = "TEXT")
    private String services; // Services descriptions e.g. "22:SSH, 80:HTTP, 443:HTTPS"

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scan_time")
    private Date scanTime = new Date();

    public NetworkScanResult() {
    }

    public NetworkScanResult(String ipAddress, String hostname, String operatingSystem, String ports, String services) {
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.operatingSystem = operatingSystem;
        this.ports = ports;
        this.services = services;
        this.scanTime = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public Date getScanTime() {
        return scanTime;
    }

    public void setScanTime(Date scanTime) {
        this.scanTime = scanTime;
    }
}

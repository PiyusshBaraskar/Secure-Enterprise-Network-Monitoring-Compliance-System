package com.enterprise.security.monitor.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType; // PORT_SCAN, BRUTE_FORCE, SUSPICIOUS_LOGIN, MALWARE_ACTIVITY, FIREWALL_VIOLATION

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "destination_ip", length = 45)
    private String destinationIp;

    @Column(nullable = false, length = 20)
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date timestamp = new Date();

    @Column(nullable = false, length = 20)
    private String status = "NEW"; // NEW, INVESTIGATING, RESOLVED, DISMISSED

    @Column(name = "assigned_to", length = 50)
    private String assignedTo;

    public SecurityAlert() {
    }

    public SecurityAlert(String alertType, String description, String sourceIp, String destinationIp, String severity, String status) {
        this.alertType = alertType;
        this.description = description;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.severity = severity;
        this.status = status;
        this.timestamp = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}

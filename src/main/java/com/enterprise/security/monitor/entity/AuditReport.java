package com.enterprise.security.monitor.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "audit_reports")
public class AuditReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType; // DAILY, WEEKLY, MONTHLY

    @Column(nullable = false, length = 10)
    private String format; // PDF, CSV

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    @Column(name = "file_path", nullable = false, length = 255)
    private String filePath;

    @Column(name = "compliance_score", nullable = false)
    private double complianceScore;

    @Column(name = "critical_vulnerabilities", nullable = false)
    private int criticalVulnerabilities;

    @Column(name = "total_devices", nullable = false)
    private int totalDevices;

    public AuditReport() {
    }

    public AuditReport(String name, String reportType, String format, String createdBy, String filePath, 
                       double complianceScore, int criticalVulnerabilities, int totalDevices) {
        this.name = name;
        this.reportType = reportType;
        this.format = format;
        this.createdBy = createdBy;
        this.filePath = filePath;
        this.complianceScore = complianceScore;
        this.criticalVulnerabilities = criticalVulnerabilities;
        this.totalDevices = totalDevices;
        this.createdAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public double getComplianceScore() {
        return complianceScore;
    }

    public void setComplianceScore(double complianceScore) {
        this.complianceScore = complianceScore;
    }

    public int getCriticalVulnerabilities() {
        return criticalVulnerabilities;
    }

    public void setCriticalVulnerabilities(int criticalVulnerabilities) {
        this.criticalVulnerabilities = criticalVulnerabilities;
    }

    public int getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(int totalDevices) {
        this.totalDevices = totalDevices;
    }
}

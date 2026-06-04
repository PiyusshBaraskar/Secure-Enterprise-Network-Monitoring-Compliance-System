package com.enterprise.security.monitor.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "compliance_results")
public class ComplianceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compliance_standard", nullable = false, length = 50)
    private String complianceStandard; // ISO 27001, CIS Benchmark, Basic Security Controls

    @Column(name = "check_name", nullable = false, length = 100)
    private String checkName;

    @Column(name = "check_category", nullable = false, length = 50)
    private String checkCategory; // Password Policy, Firewall Rules, Patch Status, Unused Accounts, Open Ports, Service Status

    @Column(nullable = false, length = 20)
    private String status; // PASS, FAIL, WARNING

    @Column(length = 500)
    private String details;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "checked_at")
    private Date checkedAt = new Date();

    public ComplianceResult() {
    }

    public ComplianceResult(String complianceStandard, String checkName, String checkCategory, String status, String details) {
        this.complianceStandard = complianceStandard;
        this.checkName = checkName;
        this.checkCategory = checkCategory;
        this.status = status;
        this.details = details;
        this.checkedAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComplianceStandard() {
        return complianceStandard;
    }

    public void setComplianceStandard(String complianceStandard) {
        this.complianceStandard = complianceStandard;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public String getCheckCategory() {
        return checkCategory;
    }

    public void setCheckCategory(String checkCategory) {
        this.checkCategory = checkCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(Date checkedAt) {
        this.checkedAt = checkedAt;
    }
}

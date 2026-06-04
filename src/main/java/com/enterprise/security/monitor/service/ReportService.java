package com.enterprise.security.monitor.service;

import com.enterprise.security.monitor.entity.AuditReport;
import com.enterprise.security.monitor.entity.ComplianceResult;
import com.enterprise.security.monitor.entity.Device;
import com.enterprise.security.monitor.entity.SecurityAlert;
import com.enterprise.security.monitor.repository.ReportRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ComplianceService complianceService;

    @Autowired
    private LogService logService;

    @Value("${app.reports.dir:C:/Users/TUF GAMING/.gemini/antigravity-ide/scratch/secure-network-monitor/reports}")
    private String reportsDir;

    public List<AuditReport> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    public AuditReport generateReport(String type, String format, String createdBy) {
        logService.writeLog("APPLICATION", "INFO", "Generating " + type + " report in " + format + " format by " + createdBy, null);

        // Ensure directories exist
        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Gather metrics
        int totalDevices = (int) deviceService.getTotalCount();
        double complianceScore = complianceService.calculateComplianceScore();
        int criticalAlerts = (int) alertService.countBySeverity("CRITICAL");
        
        List<ComplianceResult> failedChecks = new ArrayList<>();
        for (ComplianceResult cr : complianceService.getAllComplianceResults()) {
            if ("FAIL".equalsIgnoreCase(cr.getStatus())) {
                failedChecks.add(cr);
            }
        }
        int totalCriticalVulnerabilities = criticalAlerts + failedChecks.size();

        // Unique filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "Security_Report_" + type.toUpperCase() + "_" + timestamp + "." + format.toLowerCase();
        File reportFile = new File(dir, filename);
        String absolutePath = reportFile.getAbsolutePath();

        try {
            if ("PDF".equalsIgnoreCase(format)) {
                writePdfReport(reportFile, type, createdBy, totalDevices, complianceScore, totalCriticalVulnerabilities, failedChecks);
            } else if ("CSV".equalsIgnoreCase(format)) {
                writeCsvReport(reportFile, type, createdBy, totalDevices, complianceScore, totalCriticalVulnerabilities);
            } else {
                throw new IllegalArgumentException("Unsupported report format: " + format);
            }
        } catch (Exception e) {
            logService.writeLog("APPLICATION", "ERROR", "Failed to write report file: " + e.getMessage(), null);
            throw new RuntimeException("Report compilation error: " + e.getMessage(), e);
        }

        // Save report metadata
        AuditReport report = new AuditReport(
                type.toUpperCase() + " Audit Report (" + format.toUpperCase() + ")",
                type.toUpperCase(),
                format.toUpperCase(),
                createdBy,
                absolutePath,
                complianceScore,
                totalCriticalVulnerabilities,
                totalDevices
        );
        
        logService.writeLog("APPLICATION", "INFO", "Report saved: " + filename, null);
        return reportRepository.save(report);
    }

    private void writePdfReport(File file, String type, String author, int totalDevices, double complianceScore, 
                                int criticalVulnerabilities, List<ComplianceResult> failedChecks) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        
        // Add footer handler
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                cb.beginText();
                try {
                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 8);
                } catch (Exception e) {
                    // fall back
                }
                cb.setColorFill(Color.GRAY);
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "SECURE ENTERPRISE NETWORK MONITOR & COMPLIANCE - Page " + writer.getPageNumber(), 
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 15, 0);
                cb.endText();
                cb.restoreState();
            }
        });

        document.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(13, 14, 18));
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.ITALIC, new Color(108, 117, 125));
        Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(33, 37, 41));
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font redFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.RED);

        // Header Title
        Paragraph title = new Paragraph("ENTERPRISE SECURITY & COMPLIANCE REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(type.toUpperCase() + " AUDIT ANALYSIS - Generated on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Metadata Table
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setSpacingAfter(20);
        metaTable.addCell(new PdfPCell(new Phrase("Auditor/Analyst:", boldFont)));
        metaTable.addCell(new PdfPCell(new Phrase(author, normalFont)));
        metaTable.addCell(new PdfPCell(new Phrase("Compliance Standard Scope:", boldFont)));
        metaTable.addCell(new PdfPCell(new Phrase("ISO 27001, CIS Benchmarks, Basic Security Controls", normalFont)));
        document.add(metaTable);

        // Summary KPI Section
        Paragraph summaryTitle = new Paragraph("Executive Summary Metrics", sectionFont);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingAfter(25);

        // Style cells
        PdfPCell c1 = new PdfPCell(new Phrase("Scope Devices", boldFont));
        c1.setBackgroundColor(new Color(240, 240, 240));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        PdfPCell c2 = new PdfPCell(new Phrase("Compliance Score", boldFont));
        c2.setBackgroundColor(new Color(240, 240, 240));
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell c3 = new PdfPCell(new Phrase("Critical Incidents", boldFont));
        c3.setBackgroundColor(new Color(240, 240, 240));
        c3.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell c4 = new PdfPCell(new Phrase("Status Summary", boldFont));
        c4.setBackgroundColor(new Color(240, 240, 240));
        c4.setHorizontalAlignment(Element.ALIGN_CENTER);

        kpiTable.addCell(c1);
        kpiTable.addCell(c2);
        kpiTable.addCell(c3);
        kpiTable.addCell(c4);

        kpiTable.addCell(createCenterCell(String.valueOf(totalDevices), normalFont));
        
        Color scoreColor = complianceScore >= 80.0 ? new Color(0, 150, 0) : Color.ORANGE;
        Font scoreFont = new Font(Font.HELVETICA, 10, Font.BOLD, scoreColor);
        kpiTable.addCell(createCenterCell(complianceScore + "%", scoreFont));

        Font critFont = criticalVulnerabilities > 0 ? redFont : normalFont;
        kpiTable.addCell(createCenterCell(String.valueOf(criticalVulnerabilities), critFont));

        String healthStatus = complianceScore >= 90.0 ? "SECURE" : (complianceScore >= 70.0 ? "WARNING" : "CRITICAL RISK");
        Font statusFont = new Font(Font.HELVETICA, 10, Font.BOLD, complianceScore >= 90.0 ? new Color(0, 150, 0) : (complianceScore >= 70.0 ? Color.ORANGE : Color.RED));
        kpiTable.addCell(createCenterCell(healthStatus, statusFont));

        document.add(kpiTable);

        // Security Alerts Section
        Paragraph alertTitle = new Paragraph("Active Security Incidents (Top 5)", sectionFont);
        alertTitle.setSpacingAfter(10);
        document.add(alertTitle);

        PdfPTable alertTable = new PdfPTable(4);
        alertTable.setWidthPercentage(100);
        alertTable.setSpacingAfter(25);
        alertTable.setWidths(new float[]{20f, 40f, 15f, 25f});

        alertTable.addCell(new PdfPCell(new Phrase("Alert Type", boldFont)));
        alertTable.addCell(new PdfPCell(new Phrase("Description", boldFont)));
        alertTable.addCell(new PdfPCell(new Phrase("Severity", boldFont)));
        alertTable.addCell(new PdfPCell(new Phrase("Target IP", boldFont)));

        List<SecurityAlert> activeAlerts = alertService.getRecentAlerts();
        int alertCount = 0;
        for (SecurityAlert sa : activeAlerts) {
            if (alertCount >= 5) break;
            if (!"RESOLVED".equalsIgnoreCase(sa.getStatus())) {
                alertTable.addCell(new Phrase(sa.getAlertType(), normalFont));
                alertTable.addCell(new Phrase(sa.getDescription(), normalFont));
                
                Color sevColor = "CRITICAL".equalsIgnoreCase(sa.getSeverity()) ? Color.RED : 
                                 ("HIGH".equalsIgnoreCase(sa.getSeverity()) ? Color.ORANGE : Color.BLUE);
                Font sevFont = new Font(Font.HELVETICA, 10, Font.BOLD, sevColor);
                alertTable.addCell(new Phrase(sa.getSeverity(), sevFont));
                alertTable.addCell(new Phrase(sa.getDestinationIp() != null ? sa.getDestinationIp() : "N/A", normalFont));
                alertCount++;
            }
        }
        if (alertCount == 0) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No active critical security alerts detected in scope.", normalFont));
            emptyCell.setColspan(4);
            emptyCell.setPadding(10);
            alertTable.addCell(emptyCell);
        }
        document.add(alertTable);

        // Compliance Gaps Section
        Paragraph compTitle = new Paragraph("Compliance Vulnerability Details", sectionFont);
        compTitle.setSpacingAfter(10);
        document.add(compTitle);

        if (!failedChecks.isEmpty()) {
            PdfPTable failedTable = new PdfPTable(3);
            failedTable.setWidthPercentage(100);
            failedTable.setSpacingAfter(25);
            failedTable.setWidths(new float[]{30f, 20f, 50f});

            failedTable.addCell(new PdfPCell(new Phrase("Standard Check", boldFont)));
            failedTable.addCell(new PdfPCell(new Phrase("Category", boldFont)));
            failedTable.addCell(new PdfPCell(new Phrase("Details", boldFont)));

            for (ComplianceResult cr : failedChecks) {
                failedTable.addCell(new Phrase(cr.getCheckName(), normalFont));
                failedTable.addCell(new Phrase(cr.getCheckCategory(), normalFont));
                failedTable.addCell(new Phrase(cr.getDetails(), normalFont));
            }
            document.add(failedTable);
        } else {
            Paragraph noFailures = new Paragraph("No failing compliance audits found. System matches compliance benchmarks.", normalFont);
            noFailures.setSpacingAfter(25);
            document.add(noFailures);
        }

        // Recommendations Section
        Paragraph recTitle = new Paragraph("Prioritized Security Recommendations", sectionFont);
        recTitle.setSpacingAfter(10);
        document.add(recTitle);

        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        list.setListSymbol(new Chunk("\u2022 ", boldFont));
        
        if (criticalVulnerabilities > 0) {
            list.add(new com.lowagie.text.ListItem("CRITICAL: Investigate active security incidents immediately. Brute force attempts and C2 malware callouts represent imminent threats.", normalFont));
        }
        if (complianceScore < 90.0) {
            list.add(new com.lowagie.text.ListItem("HIGH: Remediate compliance gaps. Enforce password complexity policies and audit accounts inactive for more than 120 days.", normalFont));
            list.add(new com.lowagie.text.ListItem("MEDIUM: Outdated operating systems and missing security patches were detected. Schedule a patch cycle on virtual hosts.", normalFont));
        }
        list.add(new com.lowagie.text.ListItem("LOW: Replace legacy services like Telnet (Port 23) with SSH configurations on switches to secure internal traffic.", normalFont));
        list.add(new com.lowagie.text.ListItem("ROUTINE: Perform a weekly full network discovery scan to inventory newly joined enterprise devices.", normalFont));
        
        document.add(list);

        document.close();
    }

    private PdfPCell createCenterCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        return cell;
    }

    private void writeCsvReport(File file, String type, String author, int totalDevices, double complianceScore, 
                                int criticalVulnerabilities) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
        // Metadata
        writer.write("ENTERPRISE SECURITY & COMPLIANCE AUDIT CSV REPORT\n");
        writer.write("Report Type," + type.toUpperCase() + "\n");
        writer.write("Generated By," + author + "\n");
        writer.write("Generated At," + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
        writer.write("Scope,ISO 27001 / CIS Benchmarks / Basic Security Controls\n\n");

        // Summary Stats
        writer.write("METRICS SUMMARY\n");
        writer.write("Total Devices in Scope," + totalDevices + "\n");
        writer.write("Overall Compliance Score," + complianceScore + "%\n");
        writer.write("Critical Vulnerabilities Count," + criticalVulnerabilities + "\n\n");

        // Device Details
        writer.write("DEVICE INVENTORY LIST\n");
        writer.write("ID,Device Name,IP Address,MAC Address,Device Type,Operating System,Criticality,Status\n");
        List<Device> devices = deviceService.getAllDevices();
        for (Device d : devices) {
            writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                    d.getId(),
                    escapeCsv(d.getName()),
                    d.getIpAddress(),
                    d.getMacAddress(),
                    d.getDeviceType(),
                    escapeCsv(d.getOperatingSystem()),
                    d.getCriticality(),
                    d.getStatus()
            ));
        }
        writer.write("\n");

        // Compliance Check Results
        writer.write("COMPLIANCE AUDIT CHECKS\n");
        writer.write("Standard,Check Title,Category,Status,Details\n");
        List<ComplianceResult> results = complianceService.getAllComplianceResults();
        for (ComplianceResult r : results) {
            writer.write(String.format("%s,%s,%s,%s,%s\n",
                    escapeCsv(r.getComplianceStandard()),
                    escapeCsv(r.getCheckName()),
                    escapeCsv(r.getCheckCategory()),
                    r.getStatus(),
                    escapeCsv(r.getDetails())
            ));
        }

        writer.close();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}

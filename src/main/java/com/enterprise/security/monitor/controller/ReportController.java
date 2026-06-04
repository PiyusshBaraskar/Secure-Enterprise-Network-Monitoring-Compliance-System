package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.entity.AuditReport;
import com.enterprise.security.monitor.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String viewReports(Model model) {
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }

    @PostMapping("/generate")
    public String generateReport(
            @RequestParam("reportType") String reportType,
            @RequestParam("format") String format,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = authentication.getName();
            AuditReport report = reportService.generateReport(reportType, format, username);
            redirectAttributes.addFlashAttribute("successMessage", 
                    reportType + " " + format + " report compiled successfully! Download it below.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Report generation failed: " + e.getMessage());
        }
        return "redirect:/reports";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadReport(@PathVariable("id") Long id) {
        AuditReport report = reportService.getAllReports().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + id));

        File file = new File(report.getFilePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("Physical report file was deleted or is missing on the server.");
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            
            MediaType mediaType = "PDF".equalsIgnoreCase(report.getFormat()) ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(mediaType)
                    .contentLength(file.length())
                    .body(resource);
                    
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Physical report file could not be read.");
        }
    }
}

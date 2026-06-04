package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.service.ComplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/compliance")
public class ComplianceController {

    @Autowired
    private ComplianceService complianceService;

    @GetMapping
    public String viewCompliance(Model model) {
        model.addAttribute("results", complianceService.getAllComplianceResults());
        model.addAttribute("complianceScore", complianceService.calculateComplianceScore());
        model.addAttribute("isoResults", complianceService.getResultsByStandard("ISO 27001"));
        model.addAttribute("cisResults", complianceService.getResultsByStandard("CIS Benchmark"));
        model.addAttribute("bscResults", complianceService.getResultsByStandard("Basic Security Controls"));
        return "compliance";
    }

    @PostMapping("/run")
    public String runAudit(RedirectAttributes redirectAttributes) {
        try {
            complianceService.runAudit();
            redirectAttributes.addFlashAttribute("successMessage", "Compliance audit executed successfully. Scores updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to run compliance audit: " + e.getMessage());
        }
        return "redirect:/compliance";
    }
}

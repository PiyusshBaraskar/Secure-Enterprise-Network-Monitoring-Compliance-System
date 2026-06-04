package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.entity.SecurityAlert;
import com.enterprise.security.monitor.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public String viewAlerts(Model model) {
        model.addAttribute("alerts", alertService.getAllAlerts());
        model.addAttribute("criticalCount", alertService.countBySeverity("CRITICAL"));
        model.addAttribute("highCount", alertService.countBySeverity("HIGH"));
        model.addAttribute("mediumCount", alertService.countBySeverity("MEDIUM"));
        model.addAttribute("lowCount", alertService.countBySeverity("LOW"));
        return "alerts";
    }

    @GetMapping("/assign/{id}")
    public String assignAlert(@PathVariable("id") Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            alertService.assignAlert(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Alert assigned to you successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign alert: " + e.getMessage());
        }
        return "redirect:/alerts";
    }

    @GetMapping("/resolve/{id}")
    public String resolveAlert(@PathVariable("id") Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            alertService.resolveAlert(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Alert marked as RESOLVED.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to resolve alert: " + e.getMessage());
        }
        return "redirect:/alerts";
    }

    @GetMapping("/dismiss/{id}")
    public String dismissAlert(@PathVariable("id") Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            alertService.dismissAlert(id, username);
            redirectAttributes.addFlashAttribute("successMessage", "Alert marked as DISMISSED.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to dismiss alert: " + e.getMessage());
        }
        return "redirect:/alerts";
    }
}

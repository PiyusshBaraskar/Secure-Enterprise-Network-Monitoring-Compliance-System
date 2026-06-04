package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.service.AlertService;
import com.enterprise.security.monitor.service.ComplianceService;
import com.enterprise.security.monitor.service.DeviceService;
import com.enterprise.security.monitor.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private LogService logService;

    @Autowired
    private ComplianceService complianceService;

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(Model model) {
        // Main statistics
        model.addAttribute("totalDevices", deviceService.getTotalCount());
        model.addAttribute("onlineDevices", deviceService.getOnlineCount());
        model.addAttribute("offlineDevices", deviceService.getOfflineCount());
        model.addAttribute("criticalAlertsCount", alertService.getCriticalAlertCount());
        model.addAttribute("complianceScore", complianceService.calculateComplianceScore());

        // Severity list counts for Chart.js
        model.addAttribute("criticalAlerts", alertService.countBySeverity("CRITICAL"));
        model.addAttribute("highAlerts", alertService.countBySeverity("HIGH"));
        model.addAttribute("mediumAlerts", alertService.countBySeverity("MEDIUM"));
        model.addAttribute("lowAlerts", alertService.countBySeverity("LOW"));

        // Status counts for alert chart
        model.addAttribute("newAlerts", alertService.countByStatus("NEW"));
        model.addAttribute("investigatingAlerts", alertService.countByStatus("INVESTIGATING"));
        model.addAttribute("resolvedAlerts", alertService.countByStatus("RESOLVED"));

        // Recent lists
        model.addAttribute("recentLogs", logService.getRecentLogs());
        model.addAttribute("recentAlerts", alertService.getRecentAlerts());

        return "dashboard";
    }
}

package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.entity.NetworkScanResult;
import com.enterprise.security.monitor.service.ScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping("/scans")
public class ScanController {

    @Autowired
    private ScanService scanService;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping
    public String viewScans(Model model) {
        model.addAttribute("scanResults", scanService.getAllScanResults());
        model.addAttribute("isScanning", scanService.isScanning());
        model.addAttribute("scanProgress", scanService.getScanProgress());
        return "scans";
    }

    @PostMapping("/run")
    @ResponseBody
    public ResponseEntity<Map<String, String>> runDiscoveryScan(@RequestParam("targetRange") String targetRange) {
        Map<String, String> response = new HashMap<>();
        
        if (scanService.isScanning()) {
            response.put("status", "error");
            response.put("message", "A scan is already in progress.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Clear prior logs before spawning thread
            scanService.clearScanState();
            
            // Run scan in background to avoid blocking MVC response
            executorService.submit(() -> {
                try {
                    scanService.runScan(targetRange);
                } catch (Exception e) {
                    // Log fail inside service
                }
            });

            response.put("status", "success");
            response.put("message", "Scan started for target: " + targetRange);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Internal server error occurred when launching scan.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getScanStatus() {
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("isScanning", scanService.isScanning());
        statusMap.put("progress", scanService.getScanProgress());
        statusMap.put("logs", scanService.getTerminalLogs());
        return ResponseEntity.ok(statusMap);
    }
}

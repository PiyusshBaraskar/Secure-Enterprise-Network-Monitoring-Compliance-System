package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.entity.SystemLog;
import com.enterprise.security.monitor.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public String viewLogs(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "ipAddress", required = false) String ipAddress,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<SystemLog> logPage = logService.searchLogs(source, level, ipAddress, message, page, size);

        model.addAttribute("logPage", logPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalLogs", logPage.getTotalElements());

        // Keep filter variables in model to populate inputs
        model.addAttribute("selectedSource", source);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedIp", ipAddress);
        model.addAttribute("selectedMessage", message);

        return "logs";
    }

    @GetMapping("/export")
    public void exportLogsCsv(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "ipAddress", required = false) String ipAddress,
            @RequestParam(value = "message", required = false) String message,
            HttpServletResponse response) throws IOException {

        List<SystemLog> filteredLogs = logService.getLogsList(source, level, ipAddress, message);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"System_Logs_" + timestamp + ".csv\"");

        PrintWriter writer = response.getWriter();
        // Write CSV header
        writer.println("Timestamp,Log Source,Log Level,IP Address,Log Message");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SystemLog log : filteredLogs) {
            writer.println(String.format("%s,%s,%s,%s,%s",
                    dateFormat.format(log.getTimestamp()),
                    log.getLogSource(),
                    log.getLogLevel(),
                    log.getIpAddress() != null ? log.getIpAddress() : "N/A",
                    escapeCsv(log.getMessage())
            ));
        }
        writer.flush();
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

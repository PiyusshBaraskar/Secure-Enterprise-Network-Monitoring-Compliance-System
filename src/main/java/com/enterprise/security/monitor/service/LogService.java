package com.enterprise.security.monitor.service;

import com.enterprise.security.monitor.entity.SystemLog;
import com.enterprise.security.monitor.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class LogService {

    @Autowired
    private LogRepository logRepository;

    public void writeLog(String source, String level, String message, String ipAddress) {
        SystemLog log = new SystemLog(source.toUpperCase(), level.toUpperCase(), message, ipAddress);
        logRepository.save(log);
    }

    public List<SystemLog> getRecentLogs() {
        return logRepository.findTop20ByOrderByTimestampDesc();
    }

    public List<SystemLog> getAllLogs() {
        return logRepository.findAllByOrderByTimestampDesc();
    }

    public Page<SystemLog> searchLogs(String source, String level, String ipAddress, String message, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return logRepository.filterLogs(source, level, ipAddress, message, pageable);
    }

    public List<SystemLog> getLogsList(String source, String level, String ipAddress, String message) {
        return logRepository.filterLogsList(source, level, ipAddress, message);
    }

    public long getTotalLogsCount() {
        return logRepository.count();
    }
}

package com.enterprise.security.monitor.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_source", nullable = false, length = 50)
    private String logSource; // WINDOWS, LINUX, FIREWALL, APPLICATION

    @Column(name = "log_level", nullable = false, length = 20)
    private String logLevel; // INFO, WARN, ERROR, DEBUG

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date timestamp = new Date();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public SystemLog() {
    }

    public SystemLog(String logSource, String logLevel, String message, String ipAddress) {
        this.logSource = logSource;
        this.logLevel = logLevel;
        this.message = message;
        this.ipAddress = ipAddress;
        this.timestamp = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogSource() {
        return logSource;
    }

    public void setLogSource(String logSource) {
        this.logSource = logSource;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}

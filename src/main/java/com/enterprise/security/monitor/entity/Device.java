package com.enterprise.security.monitor.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "ip_address", nullable = false, unique = true, length = 45)
    private String ipAddress;

    @Column(name = "mac_address", nullable = false, unique = true, length = 17)
    private String macAddress;

    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType; // ROUTER, FIREWALL, SERVER, WORKSTATION, SWITCH, ACCESS_POINT

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String owner;

    @Column(nullable = false, length = 20)
    private String criticality; // CRITICAL, HIGH, MEDIUM, LOW

    @Column(nullable = false, length = 20)
    private String status = "OFFLINE"; // ONLINE, OFFLINE

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_seen")
    private Date lastSeen;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;

    public Device() {
    }

    public Device(String name, String ipAddress, String macAddress, String deviceType, String operatingSystem, 
                  String location, String owner, String criticality, String status, Date lastSeen) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.deviceType = deviceType;
        this.operatingSystem = operatingSystem;
        this.location = location;
        this.owner = owner;
        this.criticality = criticality;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(id, device.id) && Objects.equals(ipAddress, device.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ipAddress);
    }

    @Override
    public String toString() {
        return name + " (" + ipAddress + ")";
    }
}

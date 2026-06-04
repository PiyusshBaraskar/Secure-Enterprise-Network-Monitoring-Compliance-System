package com.enterprise.security.monitor.service;

import com.enterprise.security.monitor.entity.Device;
import com.enterprise.security.monitor.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );

    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    );

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> searchDevices(String query) {
        if (query == null || query.trim().isEmpty()) {
            return deviceRepository.findAll();
        }
        return deviceRepository.searchDevices(query.trim());
    }

    public Device saveDevice(Device device) {
        validateDevice(device);
        
        if (device.getId() == null) {
            // Set defaults for new devices
            if (device.getStatus() == null) {
                device.setStatus("ONLINE");
            }
            if (device.getLastSeen() == null) {
                device.setLastSeen(new Date());
            }
            
            // Check for duplicates
            if (deviceRepository.findByIpAddress(device.getIpAddress()).isPresent()) {
                throw new IllegalArgumentException("IP Address " + device.getIpAddress() + " is already registered.");
            }
            if (deviceRepository.findByMacAddress(device.getMacAddress()).isPresent()) {
                throw new IllegalArgumentException("MAC Address " + device.getMacAddress() + " is already registered.");
            }
        } else {
            // Updating existing device
            Device existing = deviceRepository.findById(device.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + device.getId()));
            
            // Check IP uniqueness if changed
            if (!existing.getIpAddress().equalsIgnoreCase(device.getIpAddress())) {
                if (deviceRepository.findByIpAddress(device.getIpAddress()).isPresent()) {
                    throw new IllegalArgumentException("IP Address " + device.getIpAddress() + " is already registered.");
                }
            }
            // Check MAC uniqueness if changed
            if (!existing.getMacAddress().equalsIgnoreCase(device.getMacAddress())) {
                if (deviceRepository.findByMacAddress(device.getMacAddress()).isPresent()) {
                    throw new IllegalArgumentException("MAC Address " + device.getMacAddress() + " is already registered.");
                }
            }
            
            existing.setName(device.getName());
            existing.setIpAddress(device.getIpAddress());
            existing.setMacAddress(device.getMacAddress());
            existing.setDeviceType(device.getDeviceType());
            existing.setOperatingSystem(device.getOperatingSystem());
            existing.setLocation(device.getLocation());
            existing.setOwner(device.getOwner());
            existing.setCriticality(device.getCriticality());
            if (device.getStatus() != null) {
                existing.setStatus(device.getStatus());
            }
            if (device.getLastSeen() != null) {
                existing.setLastSeen(device.getLastSeen());
            }
            return deviceRepository.save(existing);
        }
        
        return deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    public long getOnlineCount() {
        return deviceRepository.countByStatus("ONLINE");
    }

    public long getOfflineCount() {
        return deviceRepository.countByStatus("OFFLINE");
    }

    public long getTotalCount() {
        return deviceRepository.count();
    }

    private void validateDevice(Device device) {
        if (device.getName() == null || device.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Device name cannot be empty.");
        }
        if (device.getIpAddress() == null || !IP_PATTERN.matcher(device.getIpAddress()).matches()) {
            throw new IllegalArgumentException("Invalid IP Address format. Must be IPv4 format (e.g. 192.168.1.1).");
        }
        if (device.getMacAddress() == null || !MAC_PATTERN.matcher(device.getMacAddress()).matches()) {
            throw new IllegalArgumentException("Invalid MAC Address format. Must be XX:XX:XX:XX:XX:XX or XX-XX-XX-XX-XX-XX.");
        }
        if (device.getDeviceType() == null || device.getDeviceType().trim().isEmpty()) {
            throw new IllegalArgumentException("Device type must be specified.");
        }
        if (device.getCriticality() == null || device.getCriticality().trim().isEmpty()) {
            throw new IllegalArgumentException("Device criticality must be specified.");
        }
    }
}

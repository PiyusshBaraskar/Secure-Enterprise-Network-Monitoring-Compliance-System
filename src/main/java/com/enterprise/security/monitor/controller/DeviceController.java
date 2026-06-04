package com.enterprise.security.monitor.controller;

import com.enterprise.security.monitor.entity.Device;
import com.enterprise.security.monitor.service.DeviceService;
import com.enterprise.security.monitor.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private LogService logService;

    @GetMapping
    public String listDevices(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Device> devices;
        if (search != null && !search.trim().isEmpty()) {
            devices = deviceService.searchDevices(search);
            model.addAttribute("searchQuery", search);
        } else {
            devices = deviceService.getAllDevices();
        }
        model.addAttribute("devices", devices);
        model.addAttribute("newDevice", new Device()); // For add device modal
        return "devices";
    }

    @PostMapping("/save")
    public String saveDevice(@ModelAttribute("newDevice") Device device, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (device.getId() == null);
            deviceService.saveDevice(device);
            
            String action = isNew ? "Added" : "Updated";
            redirectAttributes.addFlashAttribute("successMessage", "Device " + action + " successfully!");
            
            logService.writeLog("APPLICATION", "INFO", 
                    action + " device: " + device.getName() + " (IP: " + device.getIpAddress() + ")", null);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Preserve form values in flash attributes if needed
            redirectAttributes.addFlashAttribute("failedDevice", device);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while saving the device.");
        }
        return "redirect:/devices";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Device getDeviceForEdit(@PathVariable("id") Long id) {
        return deviceService.getDeviceById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + id));
    }

    @GetMapping("/delete/{id}")
    public String deleteDevice(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Device device = deviceService.getDeviceById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + id));
            
            deviceService.deleteDevice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Device deleted successfully!");
            
            logService.writeLog("APPLICATION", "WARN", 
                    "Deleted device: " + device.getName() + " (IP: " + device.getIpAddress() + ")", null);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete device: " + e.getMessage());
        }
        return "redirect:/devices";
    }
}

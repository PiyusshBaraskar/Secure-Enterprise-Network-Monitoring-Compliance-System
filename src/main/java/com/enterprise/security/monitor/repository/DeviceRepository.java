package com.enterprise.security.monitor.repository;

import com.enterprise.security.monitor.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByIpAddress(String ipAddress);
    Optional<Device> findByMacAddress(String macAddress);
    
    @Query("SELECT d FROM Device d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR d.ipAddress LIKE CONCAT('%', :query, '%')")
    List<Device> searchDevices(@Param("query") String query);
    
    long countByStatus(String status);
}

package com.enterprise.security.monitor.repository;

import com.enterprise.security.monitor.entity.NetworkScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanResultRepository extends JpaRepository<NetworkScanResult, Long> {
    List<NetworkScanResult> findAllByOrderByScanTimeDesc();
}

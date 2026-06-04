package com.enterprise.security.monitor.repository;

import com.enterprise.security.monitor.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<SecurityAlert, Long> {
    List<SecurityAlert> findAllByOrderByTimestampDesc();
    List<SecurityAlert> findTop10ByOrderByTimestampDesc();
    long countBySeverity(String severity);
    long countByStatus(String status);
}

package com.enterprise.security.monitor.repository;

import com.enterprise.security.monitor.entity.ComplianceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRepository extends JpaRepository<ComplianceResult, Long> {
    List<ComplianceResult> findByComplianceStandard(String standard);
    List<ComplianceResult> findByCheckCategory(String category);
}

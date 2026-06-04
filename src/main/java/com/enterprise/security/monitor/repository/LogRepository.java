package com.enterprise.security.monitor.repository;

import com.enterprise.security.monitor.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findTop20ByOrderByTimestampDesc();
    List<SystemLog> findAllByOrderByTimestampDesc();
    
    @Query("SELECT l FROM SystemLog l WHERE " +
           "(:source IS NULL OR :source = '' OR l.logSource = :source) AND " +
           "(:level IS NULL OR :level = '' OR l.logLevel = :level) AND " +
           "(:ipAddress IS NULL OR :ipAddress = '' OR l.ipAddress LIKE CONCAT('%', :ipAddress, '%')) AND " +
           "(:message IS NULL OR :message = '' OR LOWER(l.message) LIKE LOWER(CONCAT('%', :message, '%')))")
    Page<SystemLog> filterLogs(@Param("source") String source,
                               @Param("level") String level,
                               @Param("ipAddress") String ipAddress,
                               @Param("message") String message,
                               Pageable pageable);

    @Query("SELECT l FROM SystemLog l WHERE " +
           "(:source IS NULL OR :source = '' OR l.logSource = :source) AND " +
           "(:level IS NULL OR :level = '' OR l.logLevel = :level) AND " +
           "(:ipAddress IS NULL OR :ipAddress = '' OR l.ipAddress LIKE CONCAT('%', :ipAddress, '%')) AND " +
           "(:message IS NULL OR :message = '' OR LOWER(l.message) LIKE LOWER(CONCAT('%', :message, '%'))) ORDER BY l.timestamp DESC")
    List<SystemLog> filterLogsList(@Param("source") String source,
                                   @Param("level") String level,
                                   @Param("ipAddress") String ipAddress,
                                   @Param("message") String message);
}

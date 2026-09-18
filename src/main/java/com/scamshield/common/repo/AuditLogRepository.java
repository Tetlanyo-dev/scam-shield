package com.scamshield.common.repo;

import com.scamshield.common.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}

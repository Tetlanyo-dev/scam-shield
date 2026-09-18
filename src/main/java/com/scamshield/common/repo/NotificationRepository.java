package com.scamshield.common.repo;

import com.scamshield.common.domain.NotificationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {
  List<NotificationRecord> findTop100ByOrderByCreatedAtDesc();

  List<NotificationRecord> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}

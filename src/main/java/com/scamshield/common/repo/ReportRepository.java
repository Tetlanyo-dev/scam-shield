package com.scamshield.common.repo;

import com.scamshield.common.domain.Report;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
  List<Report> findByPhoneNumberOrderByCreatedAtDesc(String phone);

  List<Report> findTop20ByOrderByCreatedAtDesc();

  List<Report> findByPhoneNumber(String phone);

  long countByCreatedAtAfter(LocalDateTime timestamp);
}

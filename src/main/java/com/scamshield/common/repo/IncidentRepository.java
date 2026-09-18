package com.scamshield.common.repo;

import com.scamshield.common.domain.Incident;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
  List<Incident> findAllByOrderByUpdatedAtDesc();

  Optional<Incident> findByPhoneNumberAndStatusNot(String phone, String status);
}

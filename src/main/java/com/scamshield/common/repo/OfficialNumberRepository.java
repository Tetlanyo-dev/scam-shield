package com.scamshield.common.repo;

import com.scamshield.common.domain.OfficialNumber;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialNumberRepository extends JpaRepository<OfficialNumber, Long> {
  Optional<OfficialNumber> findByPhoneNumber(String phone);

  List<OfficialNumber> findByProviderId(Long id);
}

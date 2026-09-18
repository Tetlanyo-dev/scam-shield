package com.scamshield.common.repo; import com.scamshield.common.domain.OfficialNumber; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OfficialNumberRepository extends JpaRepository<OfficialNumber,Long>{ Optional<OfficialNumber> findByPhoneNumber(String phone); List<OfficialNumber> findByProviderId(Long id); }

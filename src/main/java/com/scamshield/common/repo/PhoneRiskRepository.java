package com.scamshield.common.repo; import com.scamshield.common.domain.PhoneRisk; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PhoneRiskRepository extends JpaRepository<PhoneRisk,String>{ List<PhoneRisk> findByLevelIn(Collection<String> levels); }

package com.scamshield.common.repo; import com.scamshield.common.domain.Escalation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface EscalationRepository extends JpaRepository<Escalation,Long>{List<Escalation> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);}

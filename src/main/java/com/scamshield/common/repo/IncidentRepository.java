package com.scamshield.common.repo; import com.scamshield.common.domain.Incident; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface IncidentRepository extends JpaRepository<Incident,Long>{ List<Incident> findAllByOrderByUpdatedAtDesc(); Optional<Incident> findByPhoneNumberAndStatusNot(String phone,String status); }

package com.scamshield.common.repo; import com.scamshield.common.domain.Provider; import org.springframework.data.jpa.repository.JpaRepository;
public interface ProviderRepository extends JpaRepository<Provider,Long>{}

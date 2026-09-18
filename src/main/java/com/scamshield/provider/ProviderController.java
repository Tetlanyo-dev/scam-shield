package com.scamshield.provider;

import com.scamshield.common.repo.OfficialNumberRepository;
import com.scamshield.common.repo.ProviderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
  private final ProviderRepository providers;
  private final OfficialNumberRepository officialNumbers;

  public ProviderController(
      ProviderRepository providers,
      OfficialNumberRepository officialNumbers) {
    this.providers = providers;
    this.officialNumbers = officialNumbers;
  }

  @GetMapping
  public Object all() {
    return providers.findAll();
  }

  @GetMapping("/{id}/official-numbers")
  public Object officialNumbers(@PathVariable Long id) {
    return this.officialNumbers.findByProviderId(id);
  }
}

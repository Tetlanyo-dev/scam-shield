package com.scamshield.verification;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {
  private final VerificationService verificationService;

  public VerificationController(VerificationService verificationService) {
    this.verificationService = verificationService;
  }

  @GetMapping("/{phoneNumber}")
  public Map<String, Object> verify(@PathVariable String phoneNumber) {
    return verificationService.verify(phoneNumber);
  }
}

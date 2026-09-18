package com.scamshield.verification;

import com.scamshield.common.domain.PhoneRisk;
import com.scamshield.common.repo.OfficialNumberRepository;
import com.scamshield.common.repo.PhoneRiskRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {
  private final OfficialNumberRepository officialNumbers;
  private final PhoneRiskRepository risks;

  public VerificationService(OfficialNumberRepository officialNumbers, PhoneRiskRepository risks) {
    this.officialNumbers = officialNumbers;
    this.risks = risks;
  }

  public Map<String, Object> verify(String rawPhone) {
    String phone = normalize(rawPhone);
    var officialNumber = officialNumbers.findByPhoneNumber(phone);
    var phoneRisk = risks.findById(phone);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("phoneNumber", phone);
    response.put(
        "status",
        officialNumber.isPresent()
            ? "VERIFIED_PROVIDER_NUMBER"
            : phoneRisk.map(risk -> risk.level + "_RISK").orElse("UNKNOWN_NUMBER"));
    response.put("callerIdentityAuthenticated", false);
    officialNumber.ifPresent(number -> response.put("provider", number.provider.name));
    phoneRisk.ifPresent(risk -> addRiskDetails(response, risk));
    return response;
  }

  private String normalize(String rawPhone) {
    if (rawPhone == null) {
      throw new IllegalArgumentException("Phone number is required");
    }
    String phone = rawPhone.replaceAll("[\\s-]", "");
    if (phone.matches("^7[0-9]{7}$")) {
      phone = "+267" + phone;
    }
    if (!phone.matches("\\+267[0-9]{8}")) {
      throw new IllegalArgumentException(
          "Valid Botswana phone number required (e.g. +26771111111)");
    }
    return phone;
  }

  private void addRiskDetails(Map<String, Object> response, PhoneRisk risk) {
    response.put("riskScore", risk.score);
    response.put("level", risk.level);
    response.put("reasons", List.of(risk.reasons.split("; ")));
    response.put("reportCount", risk.reportCount);
  }
}

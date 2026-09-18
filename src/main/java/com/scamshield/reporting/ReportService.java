package com.scamshield.reporting;

import com.scamshield.common.domain.Incident;
import com.scamshield.common.domain.PhoneRisk;
import com.scamshield.common.domain.Report;
import com.scamshield.common.repo.IncidentRepository;
import com.scamshield.common.repo.PhoneRiskRepository;
import com.scamshield.common.repo.ReportRepository;
import com.scamshield.common.service.AuditService;
import com.scamshield.risk.RiskScoringService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
  private static final Map<String, Long> RATE_LIMITS = new ConcurrentHashMap<>();
  private static final List<String> PROVIDERS = List.of("Orange", "Mascom", "BTC", "Other");
  private static final List<String> ATTACK_TYPES = List.of("PIN", "OTP", "MONEY", "OTHER");
  private static final String REPORT_CHANNEL = "USSD_SIMULATOR";

  private final ReportRepository reports;
  private final PhoneRiskRepository risks;
  private final IncidentRepository incidents;
  private final RiskScoringService scorer;
  private final AuditService audit;

  public ReportService(
      ReportRepository reports,
      PhoneRiskRepository risks,
      IncidentRepository incidents,
      RiskScoringService scorer,
      AuditService audit) {
    this.reports = reports;
    this.risks = risks;
    this.incidents = incidents;
    this.scorer = scorer;
    this.audit = audit;
  }

  private String normalize(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Phone number is required");
    }
    String phone = raw.replaceAll("[\\s-]", "");
    if (phone.matches("^7[0-9]{7}$")) {
      phone = "+267" + phone;
    }
    if (!phone.matches("\\+267[0-9]{8}")) {
      throw new IllegalArgumentException(
          "Valid Botswana phone number required (e.g. +26771111111)");
    }
    return phone;
  }

  @Transactional
  public Map<String, Object> create(
      String rawPhone,
      String provider,
      String attack,
      String description,
      String reporter) {
    return create(rawPhone, provider, attack, description, reporter, REPORT_CHANNEL);
  }

  @Transactional
  public Map<String, Object> create(
      String rawPhone,
      String provider,
      String attack,
      String description,
      String reporter,
      String channel) {
    if (channel == null || channel.isBlank()) {
      channel = "USSD_SIMULATOR";
    }
    enforceRateLimit(reporter);
    String phone = normalize(rawPhone);
    validateReport(provider, attack, description, channel);

    String reporterKey = reporter == null || reporter.isBlank()
        ? "anonymous-" + UUID.randomUUID()
        : reporter;
    Report report = new Report();
    report.phoneNumber = phone;
    report.claimedProvider = provider;
    report.attackType = attack;
    report.description = description;
    report.channel = channel;
    report.reporterKey = reporterKey;
    report.createdAt = LocalDateTime.now();
    reports.save(report);
    audit.record(
        "REPORT_CREATED",
        reporterKey,
        "REPORT",
        String.valueOf(report.id),
        "Channel: " + channel + "; number: " + phone);

    List<Report> phoneReports = reports.findByPhoneNumber(phone);
    RiskScoringService.Result result = scorer.score(phoneReports.stream()
        .map(saved -> new RiskScoringService.Signal(
            saved.claimedProvider, saved.attackType, saved.reporterKey))
        .toList());
    updateRisk(phone, phoneReports.size(), result);
    updateIncident(phone, phoneReports.size(), result.score());

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("reportId", report.id);
    response.put("phoneNumber", phone);
    response.put("riskScore", result.score());
    response.put("level", result.level());
    response.put("reasons", result.reasons());
    response.put("message", "Report received. Thank you for helping protect the community.");
    return response;
  }

  private void enforceRateLimit(String reporter) {
    String key = reporter == null || reporter.isBlank() ? "anonymous" : reporter;
    long now = System.currentTimeMillis();
    long previous = RATE_LIMITS.getOrDefault(key, 0L);
    if (now - previous < 1_000) {
      throw new IllegalArgumentException("Please wait before submitting another report");
    }
    RATE_LIMITS.put(key, now);
  }

  private void validateReport(String provider, String attack, String description, String channel) {
    if (!PROVIDERS.contains(provider)) {
      throw new IllegalArgumentException("Invalid provider");
    }
    if (!ATTACK_TYPES.contains(attack)) {
      throw new IllegalArgumentException("Invalid attack type");
    }
    if (description != null && description.length() > 500) {
      throw new IllegalArgumentException("Description must be 500 characters or fewer");
    }
    if (description != null && description.matches(
        "(?i).*(?:(?:pin|otp)\\s*(?::|=|is)?\\s*\\d{4,}|password\\s*(?::|=|is)\\s*\\S+).*")) {
      throw new IllegalArgumentException("Remove any PIN, OTP, or password value before submitting");
    }
    if (!REPORT_CHANNEL.equals(channel)) {
      throw new IllegalArgumentException("Invalid report channel");
    }
  }

  private void updateRisk(
      String phone,
      int reportCount,
      RiskScoringService.Result result) {
    PhoneRisk risk = risks.findById(phone).orElseGet(PhoneRisk::new);
    risk.phoneNumber = phone;
    risk.score = result.score();
    risk.level = result.level();
    risk.reasons = String.join("; ", result.reasons());
    risk.reportCount = reportCount;
    risk.updatedAt = LocalDateTime.now();
    risks.save(risk);
  }

  private void updateIncident(String phone, int reportCount, int score) {
    if (score < 60) {
      return;
    }
    Incident incident = incidents.findByPhoneNumberAndStatusNot(phone, "RESOLVED")
        .orElseGet(() -> {
          Incident created = new Incident();
          created.reference = "SC-" + String.format("%03d", incidents.count() + 1);
          created.phoneNumber = phone;
          created.status = "OPEN";
          created.createdAt = LocalDateTime.now();
          return created;
        });
    incident.riskScore = score;
    incident.reportCount = reportCount;
    incident.updatedAt = LocalDateTime.now();
    incidents.save(incident);
  }

  public List<Report> recent() {
    return reports.findTop20ByOrderByCreatedAtDesc();
  }
}

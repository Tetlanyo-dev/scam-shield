package com.scamshield.admin;

import com.scamshield.common.domain.Report;
import com.scamshield.common.repo.AuditLogRepository;
import com.scamshield.common.repo.IncidentRepository;
import com.scamshield.common.repo.NotificationRepository;
import com.scamshield.common.repo.PhoneRiskRepository;
import com.scamshield.common.repo.ReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private static final List<String> VALID_PROVIDERS = List.of("Orange", "Mascom", "BTC", "Other");
  private final IncidentRepository incidents;
  private final ReportRepository reports;
  private final PhoneRiskRepository risks;
  private final AuditLogRepository audit;
  private final NotificationRepository notifications;

  public AdminController(
      IncidentRepository incidents,
      ReportRepository reports,
      PhoneRiskRepository risks,
      AuditLogRepository audit,
      NotificationRepository notifications) {
    this.incidents = incidents;
    this.reports = reports;
    this.risks = risks;
    this.audit = audit;
    this.notifications = notifications;
  }

  private void authorize(String key) {
    String expected = System.getenv().getOrDefault("SCAMSHIELD_ADMIN_KEY", "demo-admin-key");
    if (key == null || !key.equals(expected)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin authentication required");
    }
  }

  private static String normalize(String phone) {
    String normalized = phone.replaceAll("[\\s-]", "");
    return normalized.matches("^7[0-9]{7}$") ? "+267" + normalized : normalized;
  }

  @GetMapping("/dashboard")
  public Map<String, Object> dashboard(
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    var highRisk = risks.findByLevelIn(List.of("HIGH", "CRITICAL"));
    var allIncidents = incidents.findAllByOrderByUpdatedAtDesc();
    long activeIncidents = allIncidents.stream()
        .filter(incident -> !List.of("RESOLVED", "FALSE_POSITIVE").contains(incident.status))
        .count();
    long reportsToday = reports.countByCreatedAtAfter(LocalDate.now().atStartOfDay());
    return Map.of(
        "activeIncidents", activeIncidents,
        "reportsToday", reportsToday,
        "highRiskNumbers", highRisk.size(),
        "criticalNumbers", risks.findByLevelIn(List.of("CRITICAL")).size(),
        "providerImpersonationReports", reports.findAll().stream()
            .filter(report -> report.claimedProvider != null
                && !report.claimedProvider.equals("Other"))
            .count(),
        "incidents", allIncidents,
        "highRisk", highRisk,
        "recentReports", reports.findTop20ByOrderByCreatedAtDesc());
  }

  @GetMapping("/numbers/{phone}")
  public Map<String, Object> number(
      @PathVariable String phone,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    String normalizedPhone = normalize(phone);
    var risk = risks.findById(normalizedPhone).orElseThrow();
    var numberIncidents = incidents.findAllByOrderByUpdatedAtDesc().stream()
        .filter(incident -> incident.phoneNumber.equals(normalizedPhone))
        .toList();
    return Map.of(
        "risk", risk,
        "reports", reports.findByPhoneNumberOrderByCreatedAtDesc(normalizedPhone),
        "incidents", numberIncidents);
  }

  @GetMapping("/reports")
  public Map<String, Object> reportHistory(
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String provider,
      @RequestParam(required = false) String attackType,
      @RequestParam(required = false) String channel,
      @RequestParam(defaultValue = "100") int limit,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    int safeLimit = Math.max(1, Math.min(limit, 500));
    var matchingReports = reports.findAll().stream()
        .sorted(Comparator.comparing(
            (Report report) -> report.createdAt,
            Comparator.nullsLast(Comparator.reverseOrder())))
        .filter(report -> phone == null || report.phoneNumber.contains(normalize(phone)))
        .filter(report -> provider == null || provider.equalsIgnoreCase(report.claimedProvider))
        .filter(report -> attackType == null || attackType.equalsIgnoreCase(report.attackType))
        .filter(report -> channel == null || channel.equalsIgnoreCase(report.channel))
        .limit(safeLimit)
        .toList();
    return Map.of("reports", matchingReports, "count", matchingReports.size(), "limit", safeLimit);
  }

  @GetMapping("/analytics")
  public Map<String, Object> analytics(
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    var allReports = reports.findAll();
    LocalDate today = LocalDate.now();
    var dailyReports = new ArrayList<Map<String, Object>>();
    for (int offset = 6; offset >= 0; offset--) {
      LocalDate day = today.minusDays(offset);
      long count = allReports.stream()
          .filter(report -> report.createdAt != null
              && report.createdAt.toLocalDate().equals(day))
          .count();
      dailyReports.add(Map.of("date", day.toString(), "count", count));
    }

    var attackPatterns = allReports.stream().collect(Collectors.groupingBy(
        report -> report.attackType == null ? "UNKNOWN" : report.attackType,
        TreeMap::new,
        Collectors.counting()));
    var providerReports = allReports.stream().collect(Collectors.groupingBy(
        report -> report.claimedProvider == null ? "Unknown" : report.claimedProvider,
        TreeMap::new,
        Collectors.counting()));
    var reportChannels = allReports.stream().collect(Collectors.groupingBy(
        report -> report.channel == null ? "Unknown" : report.channel,
        TreeMap::new,
        Collectors.counting()));
    return Map.of(
        "dailyReports", dailyReports,
        "attackPatterns", attackPatterns,
        "providerReports", providerReports,
        "reportChannels", reportChannels,
        "totalReports", allReports.size());
  }

  @GetMapping("/providers/{provider}")
  public Map<String, Object> providerDashboard(
      @PathVariable String provider,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    if (!VALID_PROVIDERS.contains(provider)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown provider");
    }

    var providerReports = reports.findAll().stream()
        .filter(report -> provider.equalsIgnoreCase(report.claimedProvider))
        .toList();
    Set<String> reportedPhones = providerReports.stream()
        .map(report -> report.phoneNumber)
        .collect(Collectors.toSet());
    var providerRisks = risks.findAll().stream()
        .filter(risk -> reportedPhones.contains(risk.phoneNumber))
        .toList();
    var providerIncidents = incidents.findAllByOrderByUpdatedAtDesc().stream()
        .filter(incident -> reportedPhones.contains(incident.phoneNumber))
        .toList();
    var attackPatterns = providerReports.stream().collect(Collectors.groupingBy(
        report -> report.attackType,
        TreeMap::new,
        Collectors.counting()));
    var recentReports = providerReports.stream()
        .sorted(Comparator.comparing(
            (Report report) -> report.createdAt,
            Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(20)
        .toList();

    return Map.of(
        "provider", provider,
        "reportCount", providerReports.size(),
        "reportedNumbers", reportedPhones.size(),
        "highRiskNumbers", providerRisks.stream()
            .filter(risk -> List.of("HIGH", "CRITICAL").contains(risk.level))
            .count(),
        "incidents", providerIncidents,
        "attackPatterns", attackPatterns,
        "recentReports", recentReports);
  }

  @GetMapping("/audit")
  public Map<String, Object> auditLogs(
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    var logs = audit.findTop100ByOrderByCreatedAtDesc();
    return Map.of("events", logs, "count", logs.size());
  }

  @GetMapping("/notifications")
  public Map<String, Object> notifications(
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    var entries = notifications.findTop100ByOrderByCreatedAtDesc();
    return Map.of("notifications", entries, "count", entries.size());
  }
}

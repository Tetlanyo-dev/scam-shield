package com.scamshield.incident;

import com.scamshield.common.domain.Escalation;
import com.scamshield.common.domain.Incident;
import com.scamshield.common.domain.NotificationRecord;
import com.scamshield.common.repo.EscalationRepository;
import com.scamshield.common.repo.IncidentRepository;
import com.scamshield.common.repo.NotificationRepository;
import com.scamshield.common.repo.ReportRepository;
import com.scamshield.common.service.AuditService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
  private static final Set<String> STATUSES = Set.of(
      "OPEN", "UNDER_INVESTIGATION", "ESCALATED", "RESOLVED", "FALSE_POSITIVE");

  private final IncidentRepository incidents;
  private final EscalationRepository escalations;
  private final ReportRepository reports;
  private final NotificationRepository notifications;
  private final AuditService audit;

  public IncidentController(
      IncidentRepository incidents,
      EscalationRepository escalations,
      ReportRepository reports,
      NotificationRepository notifications,
      AuditService audit) {
    this.incidents = incidents;
    this.escalations = escalations;
    this.reports = reports;
    this.notifications = notifications;
    this.audit = audit;
  }

  private void authorize(String key) {
    String expected = System.getenv().getOrDefault("SCAMSHIELD_ADMIN_KEY", "demo-admin-key");
    if (key == null || !key.equals(expected)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin authentication required");
    }
  }

  @GetMapping
  public List<Incident> all(
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    return incidents.findAllByOrderByUpdatedAtDesc();
  }

  @GetMapping("/{id}")
  public Map<String, Object> one(
      @PathVariable Long id,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    Incident incident = incidents.findById(id).orElseThrow();
    return Map.of(
        "incident", incident,
        "reports", reports.findByPhoneNumberOrderByCreatedAtDesc(incident.phoneNumber),
        "escalations", escalations.findByIncidentIdOrderByCreatedAtDesc(id),
        "notifications", notifications.findByIncidentIdOrderByCreatedAtDesc(id));
  }

  @PatchMapping("/{id}")
  public Incident updateStatus(
      @PathVariable Long id,
      @RequestBody Map<String, String> body,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    String newStatus = body.get("status");
    if (!STATUSES.contains(newStatus)) {
      throw new IllegalArgumentException("Invalid incident status");
    }

    Incident incident = incidents.findById(id).orElseThrow();
    String oldStatus = incident.status;
    incident.status = newStatus;
    incident.updatedAt = LocalDateTime.now();
    Incident saved = incidents.save(incident);
    audit.record(
        "INCIDENT_STATUS_CHANGED",
        "admin",
        "INCIDENT",
        String.valueOf(id),
        oldStatus + " -> " + newStatus);
    return saved;
  }

  @PostMapping("/{id}/escalate")
  public Map<String, Object> escalate(
      @PathVariable Long id,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    Incident incident = incidents.findById(id).orElseThrow();
    if ("ESCALATED".equals(incident.status)) {
      throw new IllegalArgumentException("Incident is already in the provider-review queue");
    }
    String reference = "ESC-" + incident.reference + "-" + (escalations.count() + 1);

    Escalation escalation = new Escalation();
    escalation.incidentId = id;
    escalation.destination = "Internal provider review queue";
    escalation.reference = reference;
    escalation.status = "QUEUED";
    escalation.createdAt = LocalDateTime.now();
    escalations.save(escalation);

    incident.status = "ESCALATED";
    incident.escalationReference = reference;
    incident.updatedAt = LocalDateTime.now();
    incidents.save(incident);
    audit.record(
        "INCIDENT_ESCALATED",
        "admin",
        "INCIDENT",
        String.valueOf(id),
        "Added to " + escalation.destination + "; reference: " + reference);

    return Map.of(
        "status", escalation.status,
        "destination", escalation.destination,
        "reference", reference,
        "createdAt", escalation.createdAt);
  }

  public record NotificationRequest(String channel, String recipient, String message) {}

  @PostMapping("/{id}/notifications")
  public NotificationRecord notify(
      @PathVariable Long id,
      @RequestBody NotificationRequest request,
      @RequestHeader(value = "X-Admin-Key", required = false) String key) {
    authorize(key);
    Incident incident = incidents.findById(id).orElseThrow();
    String channel = request.channel() == null
        ? "IN_APP"
        : request.channel().toUpperCase(Locale.ROOT);
    if (!channel.equals("IN_APP")) {
      throw new IllegalArgumentException("Only internal in-app notifications are available");
    }

    String recipient = request.recipient() == null || request.recipient().isBlank()
        ? "Security analysts"
        : request.recipient().trim();
    String message = request.message() == null || request.message().isBlank()
        ? "ScamShield incident " + incident.reference + " requires review."
        : request.message().trim();
    if (recipient.length() > 120 || message.length() > 500) {
      throw new IllegalArgumentException("Recipient or message is too long");
    }

    NotificationRecord notification = new NotificationRecord();
    notification.incidentId = id;
    notification.channel = channel;
    notification.recipient = recipient;
    notification.message = message;
    notification.status = "RECORDED";
    notification.createdAt = LocalDateTime.now();
    NotificationRecord saved = notifications.save(notification);
    audit.record(
        "NOTIFICATION_RECORDED",
        "admin",
        "INCIDENT",
        String.valueOf(id),
        "In-app notification recorded for " + recipient);
    return saved;
  }
}

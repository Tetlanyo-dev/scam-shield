package com.scamshield.common.service;

import com.scamshield.common.domain.AuditLog;
import com.scamshield.common.repo.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuditService {
  private final AuditLogRepository logs;
  public AuditService(AuditLogRepository logs) { this.logs = logs; }

  @Transactional
  public void record(String action, String actor, String targetType, String targetId, String details) {
    AuditLog log = new AuditLog();
    log.action = action;
    log.actor = actor == null || actor.isBlank() ? "system" : actor;
    log.targetType = targetType;
    log.targetId = targetId;
    log.details = details;
    log.createdAt = LocalDateTime.now();
    logs.save(log);
  }
}

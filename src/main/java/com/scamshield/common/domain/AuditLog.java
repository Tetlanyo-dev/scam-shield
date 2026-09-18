package com.scamshield.common.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
  @Column(nullable = false) public String action;
  @Column(nullable = false) public String actor;
  @Column(name = "target_type", nullable = false) public String targetType;
  @Column(name = "target_id") public String targetId;
  @Column(length = 1000) public String details;
  @Column(name = "created_at", nullable = false) public LocalDateTime createdAt;
}

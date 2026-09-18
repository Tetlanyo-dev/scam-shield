package com.scamshield.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_simulations")
public class NotificationRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "incident_id", nullable = false)
  public Long incidentId;

  @Column(nullable = false)
  public String channel;

  @Column(nullable = false)
  public String recipient;

  @Column(nullable = false, length = 500)
  public String message;

  @Column(nullable = false)
  public String status;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;
}

package com.scamshield.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public String reference;

  @Column(name = "phone_number")
  public String phoneNumber;

  public String status;

  @Column(name = "risk_score")
  public int riskScore;

  @Column(name = "report_count")
  public int reportCount;

  @Column(name = "escalation_reference")
  public String escalationReference;

  @Column(name = "created_at")
  public LocalDateTime createdAt;

  @Column(name = "updated_at")
  public LocalDateTime updatedAt;
}

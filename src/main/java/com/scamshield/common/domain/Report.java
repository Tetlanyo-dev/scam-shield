package com.scamshield.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "phone_number")
  public String phoneNumber;

  @Column(name = "claimed_provider")
  public String claimedProvider;

  @Column(name = "attack_type")
  public String attackType;

  @Column(length = 500)
  public String description;

  @Column(name = "reporter_key", length = 120)
  public String reporterKey;

  @Column(nullable = false)
  public String channel;

  @Column(name = "created_at")
  public LocalDateTime createdAt;
}

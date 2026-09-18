package com.scamshield.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "phone_risks")
public class PhoneRisk {
  @Id
  @Column(name = "phone_number")
  public String phoneNumber;

  public int score;
  public String level;

  @Column(columnDefinition = "text")
  public String reasons;

  @Column(name = "report_count")
  public int reportCount;

  @Column(name = "updated_at")
  public LocalDateTime updatedAt;

  public PhoneRisk() {}
}

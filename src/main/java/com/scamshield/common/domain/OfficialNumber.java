package com.scamshield.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "official_numbers")
public class OfficialNumber {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(name = "phone_number", nullable = false, unique = true)
  public String phoneNumber;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "provider_id")
  public Provider provider;

  protected OfficialNumber() {}
}

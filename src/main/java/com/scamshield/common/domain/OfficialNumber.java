package com.scamshield.common.domain;
import jakarta.persistence.*;
@Entity @Table(name="official_numbers") public class OfficialNumber { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(name="phone_number",nullable=false,unique=true) public String phoneNumber; @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="provider_id") public Provider provider; protected OfficialNumber(){} }

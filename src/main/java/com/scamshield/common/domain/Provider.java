package com.scamshield.common.domain;
import jakarta.persistence.*;
@Entity @Table(name="providers") public class Provider { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,unique=true) public String name; protected Provider(){} public Provider(String name){this.name=name;} }

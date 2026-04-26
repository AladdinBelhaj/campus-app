package com.okdlab.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 40)
  private String code;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 120)
  private String campus;

  @Column(nullable = false, length = 150)
  private String contactEmail;

  protected Department() {
  }

  public Department(String code, String name, String campus, String contactEmail) {
    this.code = code;
    this.name = name;
    this.campus = campus;
    this.contactEmail = contactEmail;
  }

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getCampus() {
    return campus;
  }

  public String getContactEmail() {
    return contactEmail;
  }
}

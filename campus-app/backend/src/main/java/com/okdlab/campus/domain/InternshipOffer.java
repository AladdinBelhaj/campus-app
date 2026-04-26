package com.okdlab.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "internship_offers")
public class InternshipOffer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 140)
  private String title;

  @Column(nullable = false, length = 40)
  private String domain;

  @Column(nullable = false, length = 500)
  private String summary;

  @Column(nullable = false)
  private int seats;

  @Column(nullable = false)
  private int filledSeats;

  @Column(nullable = false, length = 80)
  private String location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OfferStatus status;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  protected InternshipOffer() {
  }

  public InternshipOffer(
      String title,
      String domain,
      String summary,
      int seats,
      int filledSeats,
      String location,
      OfferStatus status,
      Department department) {
    this.title = title;
    this.domain = domain;
    this.summary = summary;
    this.seats = seats;
    this.filledSeats = filledSeats;
    this.location = location;
    this.status = status;
    this.department = department;
  }

  @PrePersist
  @PreUpdate
  void touchTimestamp() {
    this.updatedAt = OffsetDateTime.now();
  }

  public void updateStatus(OfferStatus status) {
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDomain() {
    return domain;
  }

  public String getSummary() {
    return summary;
  }

  public int getSeats() {
    return seats;
  }

  public int getFilledSeats() {
    return filledSeats;
  }

  public String getLocation() {
    return location;
  }

  public OfferStatus getStatus() {
    return status;
  }

  public Department getDepartment() {
    return department;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}

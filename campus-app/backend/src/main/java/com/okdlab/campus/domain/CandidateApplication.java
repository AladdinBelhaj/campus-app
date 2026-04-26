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
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "candidate_applications")
public class CandidateApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String studentName;

  @Column(nullable = false, length = 160)
  private String studentEmail;

  @Column(nullable = false, length = 120)
  private String track;

  @Column(nullable = false, length = 500)
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ApplicationStatus status;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id", nullable = false)
  private InternshipOffer offer;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected CandidateApplication() {
  }

  public CandidateApplication(
      String studentName,
      String studentEmail,
      String track,
      String notes,
      ApplicationStatus status,
      InternshipOffer offer) {
    this.studentName = studentName;
    this.studentEmail = studentEmail;
    this.track = track;
    this.notes = notes;
    this.status = status;
    this.offer = offer;
  }

  @PrePersist
  void initCreatedAt() {
    this.createdAt = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getStudentName() {
    return studentName;
  }

  public String getStudentEmail() {
    return studentEmail;
  }

  public String getTrack() {
    return track;
  }

  public String getNotes() {
    return notes;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public InternshipOffer getOffer() {
    return offer;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}

package com.okdlab.campus.repository;

import com.okdlab.campus.domain.ApplicationStatus;
import com.okdlab.campus.domain.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {

  long countByStatus(ApplicationStatus status);
}

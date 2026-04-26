package com.okdlab.campus.service;

import com.okdlab.campus.domain.ApplicationStatus;
import com.okdlab.campus.domain.OfferStatus;
import com.okdlab.campus.repository.CandidateApplicationRepository;
import com.okdlab.campus.repository.InternshipOfferRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CampusMetricsService {

  private final Counter submissionsCounter;
  private final Map<String, Counter> domainCounters = new ConcurrentHashMap<>();
  private final MeterRegistry meterRegistry;

  public CampusMetricsService(
      MeterRegistry meterRegistry,
      InternshipOfferRepository internshipOfferRepository,
      CandidateApplicationRepository candidateApplicationRepository) {
    this.meterRegistry = meterRegistry;
    this.submissionsCounter = Counter.builder("campus_applications_submitted_total")
        .description("Nombre total de candidatures soumises")
        .register(meterRegistry);

    Gauge.builder("campus_published_offers", internshipOfferRepository,
            repository -> repository.countByStatus(OfferStatus.PUBLISHED))
        .description("Nombre d'offres de stage publiees")
        .register(meterRegistry);

    Gauge.builder("campus_pending_applications", candidateApplicationRepository,
            repository -> repository.countByStatus(ApplicationStatus.SUBMITTED))
        .description("Nombre de candidatures en attente")
        .register(meterRegistry);
  }

  public void recordApplicationSubmitted(String domain) {
    submissionsCounter.increment();
    domainCounters.computeIfAbsent(domain, this::registerDomainCounter).increment();
  }

  private Counter registerDomainCounter(String domain) {
    return Counter.builder("campus_applications_by_domain_total")
        .description("Candidatures par domaine")
        .tag("domain", domain)
        .register(meterRegistry);
  }
}

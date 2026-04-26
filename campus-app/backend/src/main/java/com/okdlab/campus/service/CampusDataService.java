package com.okdlab.campus.service;

import com.okdlab.campus.api.ApplicationResponse;
import com.okdlab.campus.api.CreateApplicationRequest;
import com.okdlab.campus.api.DashboardResponse;
import com.okdlab.campus.api.DepartmentResponse;
import com.okdlab.campus.api.InternshipOfferResponse;
import com.okdlab.campus.domain.ApplicationStatus;
import com.okdlab.campus.domain.CandidateApplication;
import com.okdlab.campus.domain.InternshipOffer;
import com.okdlab.campus.domain.OfferStatus;
import com.okdlab.campus.repository.CandidateApplicationRepository;
import com.okdlab.campus.repository.DepartmentRepository;
import com.okdlab.campus.repository.InternshipOfferRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CampusDataService {

  private final DepartmentRepository departmentRepository;
  private final InternshipOfferRepository internshipOfferRepository;
  private final CandidateApplicationRepository candidateApplicationRepository;
  private final CampusMetricsService campusMetricsService;

  public CampusDataService(
      DepartmentRepository departmentRepository,
      InternshipOfferRepository internshipOfferRepository,
      CandidateApplicationRepository candidateApplicationRepository,
      CampusMetricsService campusMetricsService) {
    this.departmentRepository = departmentRepository;
    this.internshipOfferRepository = internshipOfferRepository;
    this.candidateApplicationRepository = candidateApplicationRepository;
    this.campusMetricsService = campusMetricsService;
  }

  @Transactional(readOnly = true)
  public DashboardResponse getDashboard() {
    return new DashboardResponse(
        departmentRepository.count(),
        internshipOfferRepository.countByStatus(OfferStatus.PUBLISHED),
        candidateApplicationRepository.count(),
        candidateApplicationRepository.countByStatus(ApplicationStatus.SUBMITTED),
        candidateApplicationRepository.countByStatus(ApplicationStatus.ACCEPTED));
  }

  @Transactional(readOnly = true)
  public List<DepartmentResponse> listDepartments() {
    return departmentRepository.findAll().stream()
        .sorted(Comparator.comparing(department -> department.getName().toLowerCase()))
        .map(department -> new DepartmentResponse(
            department.getId(),
            department.getCode(),
            department.getName(),
            department.getCampus(),
            department.getContactEmail()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<InternshipOfferResponse> listInternships() {
    return internshipOfferRepository.findAll().stream()
        .sorted(Comparator.comparing(InternshipOffer::getUpdatedAt).reversed())
        .map(offer -> new InternshipOfferResponse(
            offer.getId(),
            offer.getTitle(),
            offer.getDomain(),
            offer.getSummary(),
            offer.getSeats(),
            offer.getFilledSeats(),
            offer.getLocation(),
            offer.getStatus(),
            offer.getDepartment().getName(),
            offer.getUpdatedAt()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ApplicationResponse> listApplications() {
    return candidateApplicationRepository.findAll().stream()
        .sorted(Comparator.comparing(CandidateApplication::getCreatedAt).reversed())
        .map(application -> new ApplicationResponse(
            application.getId(),
            application.getStudentName(),
            application.getStudentEmail(),
            application.getTrack(),
            application.getNotes(),
            application.getStatus(),
            application.getOffer().getId(),
            application.getOffer().getTitle(),
            application.getCreatedAt()))
        .toList();
  }

  public ApplicationResponse createApplication(CreateApplicationRequest request) {
    InternshipOffer offer = internshipOfferRepository.findById(request.offerId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));

    if (offer.getStatus() != OfferStatus.PUBLISHED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'offre n'est pas publiee");
    }

    CandidateApplication application = new CandidateApplication(
        request.studentName(),
        request.studentEmail(),
        request.track(),
        request.notes(),
        ApplicationStatus.SUBMITTED,
        offer);

    CandidateApplication saved = candidateApplicationRepository.save(application);
    campusMetricsService.recordApplicationSubmitted(offer.getDomain());

    return new ApplicationResponse(
        saved.getId(),
        saved.getStudentName(),
        saved.getStudentEmail(),
        saved.getTrack(),
        saved.getNotes(),
        saved.getStatus(),
        saved.getOffer().getId(),
        saved.getOffer().getTitle(),
        saved.getCreatedAt());
  }

  public InternshipOfferResponse updateOfferStatus(Long offerId, OfferStatus status) {
    InternshipOffer offer = internshipOfferRepository.findById(offerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));

    offer.updateStatus(status);

    return new InternshipOfferResponse(
        offer.getId(),
        offer.getTitle(),
        offer.getDomain(),
        offer.getSummary(),
        offer.getSeats(),
        offer.getFilledSeats(),
        offer.getLocation(),
        offer.getStatus(),
        offer.getDepartment().getName(),
        offer.getUpdatedAt());
  }
}

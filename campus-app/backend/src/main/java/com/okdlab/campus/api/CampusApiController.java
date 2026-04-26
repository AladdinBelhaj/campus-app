package com.okdlab.campus.api;

import com.okdlab.campus.service.CampusDataService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CampusApiController {

  private final CampusDataService campusDataService;

  public CampusApiController(CampusDataService campusDataService) {
    this.campusDataService = campusDataService;
  }

  @GetMapping("/dashboard")
  public DashboardResponse dashboard() {
    return campusDataService.getDashboard();
  }

  @GetMapping("/departments")
  public List<DepartmentResponse> departments() {
    return campusDataService.listDepartments();
  }

  @GetMapping("/internships")
  public List<InternshipOfferResponse> internships() {
    return campusDataService.listInternships();
  }

  @GetMapping("/applications")
  public List<ApplicationResponse> applications() {
    return campusDataService.listApplications();
  }

  @PostMapping("/applications")
  @ResponseStatus(HttpStatus.CREATED)
  public ApplicationResponse createApplication(@Valid @RequestBody CreateApplicationRequest request) {
    return campusDataService.createApplication(request);
  }

  @PatchMapping("/internships/{offerId}/status")
  public InternshipOfferResponse updateOfferStatus(
      @PathVariable Long offerId,
      @Valid @RequestBody UpdateOfferStatusRequest request) {
    return campusDataService.updateOfferStatus(offerId, request.status());
  }
}

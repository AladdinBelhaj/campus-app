package com.okdlab.campus.config;

import com.okdlab.campus.domain.ApplicationStatus;
import com.okdlab.campus.domain.CandidateApplication;
import com.okdlab.campus.domain.Department;
import com.okdlab.campus.domain.InternshipOffer;
import com.okdlab.campus.domain.OfferStatus;
import com.okdlab.campus.repository.CandidateApplicationRepository;
import com.okdlab.campus.repository.DepartmentRepository;
import com.okdlab.campus.repository.InternshipOfferRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataConfiguration {

  @Bean
  CommandLineRunner seedCampusData(
      DepartmentRepository departmentRepository,
      InternshipOfferRepository internshipOfferRepository,
      CandidateApplicationRepository candidateApplicationRepository) {
    return args -> {
      if (departmentRepository.count() > 0) {
        return;
      }

      Department networkTeam = departmentRepository.save(
          new Department("NETOPS", "Equipe reseau campus", "Campus central", "reseau@horizon.edu"));
      Department platformTeam = departmentRepository.save(
          new Department("PLAT", "Equipe plateforme OKD", "Campus central", "plateforme@horizon.edu"));
      Department supportTeam = departmentRepository.save(
          new Department("SUPPORT", "Support services numeriques", "Campus nord", "support@horizon.edu"));

      InternshipOffer networkInternship = internshipOfferRepository.save(
          new InternshipOffer(
              "Administration reseau et policies OpenShift",
              "network",
              "Mise en place de policies reseau et diagnostic des flux internes.",
              3,
              1,
              "Campus central",
              OfferStatus.PUBLISHED,
              networkTeam));

      InternshipOffer observabilityInternship = internshipOfferRepository.save(
          new InternshipOffer(
              "Observabilite cluster et dashboards Grafana",
              "monitoring",
              "Supervision des services numeriques avec Prometheus, Grafana et alerting.",
              2,
              0,
              "Campus central",
              OfferStatus.PUBLISHED,
              platformTeam));

      internshipOfferRepository.save(
          new InternshipOffer(
              "RBAC et segmentation des acces",
              "security",
              "Creation de roles d'exploitation pour les equipes stages et infrastructure.",
              2,
              0,
              "Campus nord",
              OfferStatus.PUBLISHED,
              platformTeam));

      internshipOfferRepository.save(
          new InternshipOffer(
              "Sauvegarde PostgreSQL et restauration",
              "storage",
              "Protection des donnees de candidatures avec stockage dynamique et backup regulier.",
              2,
              1,
              "Campus nord",
              OfferStatus.DRAFT,
              supportTeam));

      candidateApplicationRepository.save(
          new CandidateApplication(
              "Candidat1",
              "candidat1@etu.horizon.edu",
              "Licence administration systeme",
              "Interesse par la supervision et la maintenance des plateformes.",
              ApplicationStatus.ACCEPTED,
              networkInternship));

      candidateApplicationRepository.save(
          new CandidateApplication(
              "Candidat2",
              "candidat2@etu.horizon.edu",
              "Master infrastructure cloud",
              "Souhaite travailler sur Grafana et Prometheus pendant l'ete.",
              ApplicationStatus.SUBMITTED,
              observabilityInternship));
    };
  }
}

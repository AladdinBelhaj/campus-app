package com.okdlab.campus.repository;

import com.okdlab.campus.domain.InternshipOffer;
import com.okdlab.campus.domain.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipOfferRepository extends JpaRepository<InternshipOffer, Long> {

  long countByStatus(OfferStatus status);
}

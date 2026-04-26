package com.okdlab.campus;

import com.okdlab.campus.repository.InternshipOfferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CampusApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InternshipOfferRepository internshipOfferRepository;

  @Test
  void shouldListSeededInternships() throws Exception {
    mockMvc.perform(get("/api/internships"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").isNotEmpty())
        .andExpect(jsonPath("$[0].department").isNotEmpty());
  }

  @Test
  void shouldCreateApplication() throws Exception {
    Long offerId = internshipOfferRepository.findAll().stream()
        .filter(offer -> offer.getStatus().name().equals("PUBLISHED"))
        .findFirst()
        .orElseThrow()
        .getId();

    String payload = """
        {
          "studentName": "Leila Trabelsi",
          "studentEmail": "leila.trabelsi@etu.horizon.edu",
          "track": "Master reseaux",
          "notes": "Je souhaite contribuer aux labs de supervision et de securite.",
          "offerId": %d
        }
        """.formatted(offerId);

    mockMvc.perform(post("/api/applications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.studentName").value("Leila Trabelsi"))
        .andExpect(jsonPath("$.offerId").value(offerId));
  }
}

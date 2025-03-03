package br.com.frankefelipe5.calmail.api.controller;

import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.service.AIResponseService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AIResponseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AIResponseService aiResponseService;

  @Test
  void getResponseById_ReturnsOkWithAIResponse() throws Exception {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    AIResponseDTO responseDTO = new AIResponseDTO(id, "response text", new AIRequest("example", false, false, "example"), now);
    Mockito.when(aiResponseService.findResponseById(id)).thenReturn(responseDTO);
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.responseText").value("response text"));
  }

  @Test
  void getResponseById_ReturnsNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.when(aiResponseService.findResponseById(id)).thenThrow(new AIResponseNotFoundException("unable to find an response with id = " + id));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", id))
        .andExpect(status().isNotFound());
  }

  @Test
  void getResponseById_ReturnsBadRequest() throws Exception {
    String invalidId = "invalid-uuid";
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/responses/generated/{id}", invalidId))
        .andExpect(status().isBadRequest());
  }

}
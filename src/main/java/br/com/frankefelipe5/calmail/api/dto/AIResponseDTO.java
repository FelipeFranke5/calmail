package br.com.frankefelipe5.calmail.api.dto;

import java.time.Instant;
import java.util.UUID;
import org.springframework.hateoas.RepresentationModel;

public class AIResponseDTO extends RepresentationModel<AIResponseDTO> {

  private UUID id;
  private AIRequest request;
  private String responseText;
  private Instant createdAt;

  public AIResponseDTO(UUID id, String responseText, AIRequest request, Instant createdAt) {
    this.id = id;
    this.request = request;
    this.responseText = responseText;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public AIRequest getRequest() {
    return request;
  }

  public void setRequest(AIRequest request) {
    this.request = request;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}

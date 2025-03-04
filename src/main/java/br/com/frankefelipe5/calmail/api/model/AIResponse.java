package br.com.frankefelipe5.calmail.api.model;

import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class AIResponse {

  public AIResponse(UUID id, String responseText, AIRequest aiRequest, Instant createdAt) {
    this.setId(id);
    this.setResponseText(responseText);
    this.setRequest(aiRequest);
    this.setCreatedAt(createdAt);
  }

  public AIResponse(UUID id, String responseText) {
    this.setId(id);
    this.setResponseText(responseText);
  }

  public AIResponse(String responseText) {
    this.setResponseText(responseText);
  }

  public AIResponse(UUID id, String responseText, Instant createdAt) {
    this.setId(id);
    this.setResponseText(responseText);
    this.setCreatedAt(createdAt);
  }

  public AIResponse(AIRequest request, String responseText) {
    this.request = request;
    this.responseText = responseText;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, updatable = false)
  private AIRequest request;

  @Column(nullable = false, updatable = false, length = 2048)
  private String responseText;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public AIRequest getRequest() {
    return request;
  }

  public void setRequest(AIRequest request) {
    this.request = request;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((responseText == null) ? 0 : responseText.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AIResponse other = (AIResponse) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (responseText == null) {
      if (other.responseText != null) return false;
    } else if (!responseText.equals(other.responseText)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "AIResponse [id=" + id + ", responseText=" + responseText + "]";
  }
}

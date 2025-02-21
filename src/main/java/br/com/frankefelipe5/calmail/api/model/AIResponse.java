package br.com.frankefelipe5.calmail.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class AIResponse {

    public AIResponse() {
    }

    public AIResponse(UUID id, String responseText) {
        this.setId(id);
        this.setResponseText(responseText);
    }

    public AIResponse(String responseText) {
        this.setResponseText(responseText);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String responseText;

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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AIResponse other = (AIResponse) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (responseText == null) {
            if (other.responseText != null)
                return false;
        } else if (!responseText.equals(other.responseText))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AIResponse [id=" + id + ", responseText=" + responseText + "]";
    }

}

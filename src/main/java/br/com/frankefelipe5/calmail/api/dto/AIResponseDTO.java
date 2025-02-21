package br.com.frankefelipe5.calmail.api.dto;

import java.util.UUID;
import org.springframework.hateoas.RepresentationModel;

public class AIResponseDTO extends RepresentationModel<AIResponseDTO> {

    private UUID id;
    private String responseText;

    public AIResponseDTO(UUID id, String responseText) {
        this.id = id;
        this.responseText = responseText;
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

}

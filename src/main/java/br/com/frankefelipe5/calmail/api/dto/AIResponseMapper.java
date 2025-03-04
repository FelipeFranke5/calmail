package br.com.frankefelipe5.calmail.api.dto;

import br.com.frankefelipe5.calmail.api.model.AIResponse;

public class AIResponseMapper {
    public static AIResponseDTO toDTO(AIResponse entity) {
        return new AIResponseDTO(entity.getId(), entity.getResponseText(), entity.getRequest(), entity.getCreatedAt());
    }
}

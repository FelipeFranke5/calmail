package br.com.frankefelipe5.calmail.api.service;

import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import br.com.frankefelipe5.calmail.api.assembler.AIResponseAssembler;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.Request;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.external.APIRequest;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class AIResponseService {

    private final AIResponseRepository aiResponseRepository;
    private final AIResponseAssembler aiResponseAssembler;
    private final OpenAiChatModel chatModel;

    public AIResponseService(
        AIResponseRepository aiResponseRepository,
        AIResponseAssembler aiResponseAssembler,
        OpenAiChatModel chatModel
    ) {
        this.aiResponseRepository = aiResponseRepository;
        this.aiResponseAssembler = aiResponseAssembler;
        this.chatModel = chatModel;
    }

    public List<AIResponseDTO> listAll(boolean orderByCreatedAt) {
        List<AIResponse> responseList = orderByCreatedAt ?
                Optional.ofNullable(aiResponseRepository.findByOrderByCreatedAtAsc()).orElse(Collections.emptyList()) :
                Optional.ofNullable(aiResponseRepository.findAll()).orElse(Collections.emptyList());
        return responseList.stream()
                .map(aiResponseAssembler::toModel)
                .collect(Collectors.toList());
    }

    public AIResponseDTO findResponseById(UUID id) {
        AIResponse aiResponse = this.getAiResponse(id);
        return aiResponseAssembler.toModel(aiResponse);
    }

    public void deleteResponseById(UUID id) {
        AIResponse aiResponse = this.getAiResponse(id);
        aiResponseRepository.delete(aiResponse);
    }

    public AIResponseDTO saveData(Request request) {
        String generatedResponse = new APIRequest(request, chatModel, aiResponseRepository).getGeneratedResponse();
        String responseToBeSaved = getResponseToBeSaved(generatedResponse);
        AIResponse aiResponse = aiResponseRepository.save(new AIResponse(request, responseToBeSaved));
        return aiResponseAssembler.toModel(aiResponse);
    }

    @Transactional
    public void clearResponses() {
        aiResponseRepository.deleteAll();
    }

    private String getResponseToBeSaved(String generatedResponse) {
        return generatedResponse.length() < 254 ? generatedResponse : generatedResponse.substring(
            0, 100
        ) + "...";
    }

    private AIResponse getAiResponse(UUID id) {
        return aiResponseRepository.findById(id).orElseThrow(
            () -> new AIResponseNotFoundException("unable to find an response with id = " + id)
        );
    }

}

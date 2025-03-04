package br.com.frankefelipe5.calmail.api.service;

import br.com.frankefelipe5.calmail.api.assembler.AIResponseAssembler;
import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.exception.AIResponseAcessNotGrantedException;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.exception.AIResponseSQLException;
import br.com.frankefelipe5.calmail.api.external.APIRequest;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AIResponseService {

    private static final Logger logger = LoggerFactory.getLogger(AIResponseService.class);

    private final AIResponseRepository aiResponseRepository;
    private final AIResponseAssembler aiResponseAssembler;
    private final OpenAiChatModel chatModel;

    public AIResponseService(
            AIResponseRepository aiResponseRepository,
            AIResponseAssembler aiResponseAssembler,
            OpenAiChatModel chatModel) {
        this.aiResponseRepository = aiResponseRepository;
        this.aiResponseAssembler = aiResponseAssembler;
        this.chatModel = chatModel;
    }

    private void validateUserPermission() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) throw new SecurityException();
        if (!authentication.getName().equalsIgnoreCase(System.getenv("KEYCLOAK_ADMIN")))
            throw new AIResponseAcessNotGrantedException("authenticated user does not have acess to this resource");
    }

    public List<AIResponseDTO> listAll(boolean orderByCreatedAt) {
        try {
            List<AIResponse> responseList = orderByCreatedAt
                    ? Optional.ofNullable(aiResponseRepository.findByOrderByCreatedAtAsc())
                            .orElse(Collections.emptyList())
                    : Optional.ofNullable(aiResponseRepository.findAll()).orElse(Collections.emptyList());
            return responseList.stream().map(aiResponseAssembler::toModel).collect(Collectors.toList());
        } catch (DataAccessException | PersistenceException databaseException) {
            logger.error("Database connection error: ", databaseException);
            logger.error("Throwing AIResponseSQLException..");
            throw new AIResponseSQLException("database connection error");
        }
    }

    public AIResponseDTO findResponseById(UUID id) {
        AIResponse aiResponse = this.getAiResponse(id);
        return aiResponseAssembler.toModel(aiResponse);
    }

    public void deleteResponseById(UUID id) {
        this.validateUserPermission();
        AIResponse aiResponse = this.getAiResponse(id);
        aiResponseRepository.delete(aiResponse);
    }

    public AIResponseDTO saveData(AIRequest request) {
        String responseToBeSaved = new APIRequest(request, chatModel, aiResponseRepository).getGeneratedResponse();
        return aiResponseAssembler.toModel(aiResponseRepository.save(new AIResponse(request, responseToBeSaved)));
    }

    @Transactional
    public void clearResponses() {
        this.validateUserPermission();
        aiResponseRepository.deleteAll();
    }

    private AIResponse getAiResponse(UUID id) {
        return aiResponseRepository
                .findById(id)
                .orElseThrow(() -> new AIResponseNotFoundException("unable to find an response with id = " + id));
    }
}

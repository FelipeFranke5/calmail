package br.com.frankefelipe5.calmail.api.service;

import br.com.frankefelipe5.calmail.api.assembler.AIResponseAssembler;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.Request;
import br.com.frankefelipe5.calmail.api.exception.AIResponseGetDataException;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.exception.AIResponseSQLException;
import br.com.frankefelipe5.calmail.api.exception.AiResponseSaveDataException;
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

  public List<AIResponseDTO> listAll(boolean orderByCreatedAt) {
    try {
      List<AIResponse> responseList =
          orderByCreatedAt
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
    AIResponse aiResponse = this.getAiResponse(id);
    aiResponseRepository.delete(aiResponse);
  }

  public AIResponseDTO saveData(Request request) {
    String responseToBeSaved =
        new APIRequest(request, chatModel, aiResponseRepository).getGeneratedResponse();
    try {
      AIResponse aiResponse = aiResponseRepository.save(new AIResponse(request, responseToBeSaved));
      return aiResponseAssembler.toModel(aiResponse);
    } catch (Exception exception) {
      logger.error("exception at saveData() --- ", exception);
      throw new AiResponseSaveDataException("could not save response from external API");
    }
  }

  @Transactional
  public void clearResponses() {
    aiResponseRepository.deleteAll();
  }

  private AIResponse getAiResponse(UUID id) {
    try {
      return aiResponseRepository
          .findById(id)
          .orElseThrow(
              () -> new AIResponseNotFoundException("unable to find an response with id = " + id));
    } catch (Exception exception) {
      logger.error("exception raised at getAiResponse(UUID id)", exception);
      throw new AIResponseGetDataException("unable to retrieve data");
    }
  }
}

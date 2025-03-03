package br.com.frankefelipe5.calmail.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import br.com.frankefelipe5.calmail.api.assembler.AIResponseAssembler;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.exception.AIResponseNotFoundException;
import br.com.frankefelipe5.calmail.api.exception.AIResponseSQLException;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import jakarta.persistence.PersistenceException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.openai.OpenAiChatModel;

public class AIResponseServiceTest {

  private static AIResponseRepository aiResponseRepository;
  private static AIResponseAssembler aiResponseAssembler;
  private static OpenAiChatModel chatModel;
  private static AIResponseService aiResponseService;
  private static AIResponse aiResponse1, aiResponse2;
  private static UUID someUUID1, someUUID2;

  @BeforeAll
  static void setUpBeforeAll() {
    someUUID1 = UUID.randomUUID();
    someUUID2 = UUID.randomUUID();
    aiResponse1 = new AIResponse(someUUID1, "Some Response", Instant.now());
    aiResponse2 = new AIResponse(someUUID2, "Some Another Response", Instant.now().plusSeconds(10));
  }

  @BeforeEach
  void setUpBeforeEach() {
    aiResponseAssembler = new AIResponseAssembler();
    chatModel = mock(OpenAiChatModel.class);
    aiResponseRepository = mock(AIResponseRepository.class);
    aiResponseService = new AIResponseService(aiResponseRepository, aiResponseAssembler, chatModel);
  }

  @Test
  @DisplayName("Test listAll returns ordered list when 'orderByCreatedAt' is true")
  void
      testListAll_When_OrderByCreatedAtIsTrue_And_NothingWasSaved_ShouldReturnListOrderedByCreatedAt() {
    // Given
    boolean orderByCreatedAt = true;
    Mockito.when(aiResponseRepository.findByOrderByCreatedAtAsc()).thenReturn(List.of());
    // When
    List<AIResponseDTO> actual = aiResponseService.listAll(orderByCreatedAt);
    // Then
    assertNotNull(actual);
    assertEquals(List.of(), actual, () -> "listAll should have returned an empty list");
    Mockito.verify(aiResponseRepository, Mockito.times(0)).findAll();
  }

  @Test
  @DisplayName("Test listAll returns unordered list when 'orderByCreatedAt' is false")
  void testListAll_When_OrderByCreatedAtIsFalse_And_NothingWasSaved_ShouldReturnUnorderedList() {
    // Given
    boolean orderByCreatedAt = false;
    Mockito.when(aiResponseRepository.findAll()).thenReturn(List.of());
    // When
    List<AIResponseDTO> actual = aiResponseService.listAll(orderByCreatedAt);
    // Then
    assertNotNull(actual);
    assertEquals(List.of(), actual, () -> "listAll should have returned an empty list");
    Mockito.verify(aiResponseRepository, Mockito.times(0)).findByOrderByCreatedAtAsc();
  }

  @Test
  @DisplayName("Test listAll throws exception if connection to Database fails for some reason")
  void testListAll_When_ConnectionFails_ShouldThrowException() {
    // Given
    boolean orderByCreatedAt = true;
    Mockito.when(aiResponseRepository.findByOrderByCreatedAtAsc())
        .thenThrow(new PersistenceException());
    // When + Then
    AIResponseSQLException actual =
        assertThrows(
            AIResponseSQLException.class,
            () -> {
              aiResponseService.listAll(orderByCreatedAt);
            },
            () -> "AIResponseSQLException was not thrown");
    assertNotNull(actual, () -> "actual is null, it should not be null here");
  }

  @Test
  @DisplayName("Test listAll returns ordered list with two Response instances")
  void
      testListAll_When_OrderByCreatedAtIsTrue_And_TwoInstancesAreSaved_ShouldReturnListOfOrderedResponses() {
    // Given
    AIResponseDTO dto1 = aiResponseAssembler.toModel(aiResponse1);
    AIResponseDTO dto2 = aiResponseAssembler.toModel(aiResponse2);
    Mockito.when(aiResponseRepository.findByOrderByCreatedAtAsc())
        .thenReturn(List.of(aiResponse1, aiResponse2));
    // When
    List<AIResponseDTO> actual = aiResponseService.listAll(true);
    // Then
    assertEquals(2, actual.size());
    assertEquals(dto1, actual.get(0));
    assertEquals(dto2, actual.get(1));
    assertNotEquals(
        actual.get(0).getCreatedAt(),
        actual.get(1).getCreatedAt(),
        () -> "getCreatedAt() returned the same Instant for both instances");
    assertTrue(
        actual.get(0).getCreatedAt().isBefore(actual.get(1).getCreatedAt()),
        () -> "getCreatedAt() of the first response is after getCreatedAt() of the second");
  }

  @Test
  @DisplayName("Test listAll returns unordered list with two Response instances")
  void
      testListAll_When_OrderByCreatedAtIsFalse_And_TwoInstancesAreSaved_ShouldReturnListOfUnorderedResponses() {
    // Given
    Mockito.when(aiResponseRepository.findAll()).thenReturn(List.of(aiResponse2, aiResponse1));
    // When
    List<AIResponseDTO> actual = aiResponseService.listAll(false);
    // Then
    assertEquals(2, actual.size());
    assertNotEquals(
        actual.get(0).getCreatedAt(),
        actual.get(1).getCreatedAt(),
        () -> "getCreatedAt() returned the same Instant for both instances");
    assertFalse(
        actual.get(0).getCreatedAt().isBefore(actual.get(1).getCreatedAt()),
        () -> "getCreatedAt() of the first response is before getCreatedAt() of the second");
  }

  @Test
  @DisplayName("Test findResponseById returns AIResponseDTO when AIResponse is found")
  void testFindResponseById_When_IdCorrespondsToExistingInstance_ShouldReturnInstance() {
    // Given
    Mockito.when(aiResponseRepository.findById(someUUID1)).thenReturn(Optional.of(aiResponse1));
    AIResponseDTO expected = aiResponseAssembler.toModel(aiResponse1);
    // When
    AIResponseDTO actual = aiResponseService.findResponseById(someUUID1);
    // Then
    assertNotNull(actual);
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName(
      "Test findResponseById throws AIResponseNotFoundException when AIResponse is not found")
  void testFindResponseById_When_IdDoesNotCorrespondToAnyInstance_ShouldThrowException() {
    UUID nonExistentUUID = UUID.randomUUID();
    Mockito.when(aiResponseRepository.findById(nonExistentUUID)).thenReturn(Optional.empty());
    assertThrows(
        AIResponseNotFoundException.class,
        () -> aiResponseService.findResponseById(nonExistentUUID),
        "Expected findResponseById to throw, but it didn't");
  }
}

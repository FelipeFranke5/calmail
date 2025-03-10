package br.com.frankefelipe5.calmail.api.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.frankefelipe5.calmail.api.exception.AIResponseException;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Test for the custom File Writer")
public class AIResponseFileWriterTest {

    @Mock
    private AIResponseRepository aiResponseRepository;

    @InjectMocks
    private AIResponseFileWriter aiResponseFileWriter;

    static Instant beforeAllInstant;
    static Instant afterAllInstant;
    static final Logger logger = LoggerFactory.getLogger(AIResponseFileWriterTest.class);

    @BeforeAll
    public static void setUpBeforeAll() {
        logger.info("Starting tests for AIResponseFileWriter ..");
        beforeAllInstant = Instant.now();
    }

    @AfterAll
    public static void tearDownAfterAll() {
        afterAllInstant = Instant.now();
        logger.info("Duration in seconds: "
                + Duration.between(beforeAllInstant, afterAllInstant).toSeconds());
        logger.info("Duration in milis: "
                + Duration.between(beforeAllInstant, afterAllInstant).toMillis());
        logger.info("End of tests for AIResponseFileWriter ..");
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test createFileWithResponses creates file with empty responses")
    void testCreateFileWithResponses_When_NoDatabaseRecordsAreFound_ShouldWriteEmptyFile() {
        // Given
        when(aiResponseRepository.findAll()).thenReturn(List.of());
        String content = "";
        String expectedContent = "";
        // When
        aiResponseFileWriter.createFileWithResponses();
        try {
            content = Files.readString(Paths.get("src/main/resources/responses.txt"));
        } catch (IOException exception) {
            fail(exception);
        }
        // Then
        assertTrue(content.isBlank() && content.isEmpty());
        assertEquals(expectedContent, content);
        verify(aiResponseRepository, atLeastOnce()).findAll();
        aiResponseFileWriter.deleteFileWithResponses();
    }

    @Test
    @DisplayName("Test createFileWithResponses creates file with filled responses")
    void testCreateFileWithResponses_When_DatabaseRecordsAreFound_ShouldWriteFileWithContent() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AIResponse response1 = new AIResponse(id1, "This is some text example for the first response");
        AIResponse response2 = new AIResponse(id2, "This is another text example");
        when(aiResponseRepository.findAll()).thenReturn(List.of(response1, response2));
        String content = "";
        // When
        aiResponseFileWriter.createFileWithResponses();
        try {
            content = Files.readString(Paths.get("src/main/resources/responses.txt"));
        } catch (IOException exception) {
            fail(exception);
        }
        // Then
        assertFalse(content.isBlank() && content.isEmpty());
        assertTrue(content.contains("This is some text example for the first response"));
        assertTrue(content.contains("This is another text example"));
        assertTrue(content.contains(id1.toString()));
        assertTrue(content.contains(id2.toString()));
        assertTrue(content.contains("----"));
        verify(aiResponseRepository, atLeastOnce()).findAll();
        aiResponseFileWriter.deleteFileWithResponses();
    }

    @Test
    @DisplayName("Test deleteFileWithResponses deletes the created file")
    void testDeleteFileWithResponses_ShouldDeleteFile() {
        // Given
        when(aiResponseRepository.findAll()).thenReturn(List.of());
        // When
        aiResponseFileWriter.createFileWithResponses();
        aiResponseFileWriter.deleteFileWithResponses();
        // Then
        assertFalse(Files.exists(Paths.get("src/main/resources/responses.txt")));
    }

    @Test
    @DisplayName("Test deleteFileWithResponses throws AIResponseException when the file does not exist")
    void testDeleteFileWithResponses_When_FileDoesNotExist_ShouldThrowAIResponseException() {
        AIResponseException expectedException = assertThrowsExactly(AIResponseException.class, () -> {
            aiResponseFileWriter.deleteFileWithResponses();
            aiResponseFileWriter.deleteFileWithResponses();
        });
        assertEquals(
                "an error ocurred while deleting the file containing saved responses", expectedException.getMessage());
    }
}

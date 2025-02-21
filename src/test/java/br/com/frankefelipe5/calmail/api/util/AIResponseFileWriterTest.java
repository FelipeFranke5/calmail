package br.com.frankefelipe5.calmail.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class AIResponseFileWriterTest {

    @Mock
    private AIResponseRepository aiResponseRepository;

    @InjectMocks
    private AIResponseFileWriter aiResponseFileWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test createFileWithResponses creates file with empty responses")
    void testCreateFileWithEmptyResponses() throws IOException {
        // Given
        when(aiResponseRepository.findAll()).thenReturn(List.of());
        // When
        aiResponseFileWriter.createFileWithResponses();
        String content = Files.readString(Paths.get("src/main/resources/responses.txt"));
        // Then
        assertTrue(content.isBlank() && content.isEmpty());
        assertEquals("", content);
        verify(aiResponseRepository, atLeastOnce()).findAll();
        aiResponseFileWriter.deleteFileWithResponses();
    }

    @Test
    @DisplayName("Test createFileWithResponses creates file with filled responses")
    void testCreateFileWithFilledResponses() throws IOException {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AIResponse response1 = new AIResponse(id1, "This is some text example for the first response");
        AIResponse response2 = new AIResponse(id2, "This is another text example");
        when(aiResponseRepository.findAll()).thenReturn(List.of(response1, response2));
        // When
        aiResponseFileWriter.createFileWithResponses();
        String content = Files.readString(Paths.get("src/main/resources/responses.txt"));
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
    void testDeleteFileWithResponsesDeletesFile() {
        // Given
        when(aiResponseRepository.findAll()).thenReturn(List.of());
        // When
        aiResponseFileWriter.createFileWithResponses();
        aiResponseFileWriter.deleteFileWithResponses();
        // Then
        assertFalse(Files.exists(Paths.get("src/main/resources/responses.txt")));
    }
}

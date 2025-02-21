package br.com.frankefelipe5.calmail.api.external;

import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

import br.com.frankefelipe5.calmail.api.dto.Request;
import br.com.frankefelipe5.calmail.api.exception.APIRequestException;
import br.com.frankefelipe5.calmail.api.model.AIResponse;

public class APIRequestTest {

    @Mock
    private OpenAiChatModel chatModel;

    @Mock
    private AIResponseRepository aiResponseRepository;

    private Request request = buildDefaultRequestOne();

    private APIRequest apiRequest;

    @BeforeEach
    private void setup() {
        MockitoAnnotations.openMocks(this);
        apiRequest = spy(new APIRequest(request, chatModel, aiResponseRepository));
    }

    @Test
    @DisplayName("Test getGeneratedResponse returns a String when we have a list of messages")
    void testGetGeneratedResponseReturnsString() {
        // Given
        List<Message> messages = buildDefaultMessageListOne();
        ChatResponse chatResponse = buildDefaultChatResponseOne();
        Mockito.when(aiResponseRepository.findAll()).thenReturn(buildDefaultAiResponseListOne());
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        Mockito.when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        // When
        String generatedResponse = apiRequest.getGeneratedResponse();
        // Then
        assertNotNull(generatedResponse);
        assertTrue(generatedResponse.equals("assistant message"));
    }

    private Request buildDefaultRequestOne() {
        return new Request(
                "Test",
                true,
                false,
                null,
                null,
                "Test message");
    }

    @Test
    @DisplayName("Test getPrompt throws APIRequestException when the list of messages is null")
    void testGetPromptThrowsExceptionWhenListIsNull() {
        // Given
        List<Message> messages = null;
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        // When
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getPrompt();
        });
        // Then
        assertEquals("list of messages cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Test getPrompt does not throw when the list of messages is not null")
    void testGetPromptDoesNotThrowWhenListIsNotNull() {
        // Given
        List<Message> messages = buildDefaultMessageListOne();
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        // When
        Prompt prompt = apiRequest.getPrompt();
        // Then
        assertNotNull(prompt);
        assertTrue(prompt.getInstructions().size() == 2);
    }

    @Test
    @DisplayName("Test getPrompt throws APIRequestException when one of the messages have empty content")
    void testGetPromptThrowsExceptionWhenListSizeDoesNotEqualTwo() {
        // Given
        List<Message> messages = buildDefaultMessageListTwo();
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        // When
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getPrompt();
        });
        // Then
        assertEquals("all messages should have content", exception.getMessage());
    }

    @Test
    @DisplayName("Test getMessages throws APIRequestException when one of the messages is null")
    void testGetMessagesThrowsExceptionWhenOneMessageIsNull() {
        // Given
        Message message1 = buildDefaultSystemMessageOne();
        Message message2 = null;
        Mockito.doReturn(message1).when(apiRequest).getSystemMessage();
        Mockito.doReturn(message2).when(apiRequest).getUserMessage();
        // When
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getMessages();
        });
        // Then
        assertEquals("user message cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Test getMessages does not throw when all messages are not null")
    void testGetMessagesDoesNotThrowWhenMessagesAreNotNull() {
        // Given
        Message message1 = buildDefaultSystemMessageOne();
        Message message2 = buildDefaultUserMessageOne();
        Mockito.doReturn(message1).when(apiRequest).getSystemMessage();
        Mockito.doReturn(message2).when(apiRequest).getUserMessage();
        // When
        List<Message> messages = apiRequest.getMessages();
        // Then
        assertNotNull(messages);
        assertTrue(messages.size() == 2);
    }

    @Test
    @DisplayName("Test getUserMessage throws APIRequestException when requestData is empty")
    void testGetUserMessageThrowsExceptionWhenRequestDataIsEmpty() {
        // Given
        String requestData = "";
        String systemRole = "am filled";
        String systemContext = "am filled";
        Mockito.doReturn(requestData).when(apiRequest).getRequestDataAsString();
        Mockito.doReturn(systemRole).when(apiRequest).getSystemRoleFromFile();
        Mockito.doReturn(systemContext).when(apiRequest).getSystemContextFromFile();
        // When
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getUserMessage();
        });
        // Then
        assertEquals("request data cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test getUserMessage throws APIRequestException when systemRole is empty")
    void testGetUserMessageThrowsExceptionWhenSystemRoleIsEmpty() {
        // Given
        String requestData = "am filled";
        String systemRole = "";
        String systemContext = "am filled";
        Mockito.doReturn(requestData).when(apiRequest).getRequestDataAsString();
        Mockito.doReturn(systemRole).when(apiRequest).getSystemRoleFromFile();
        Mockito.doReturn(systemContext).when(apiRequest).getSystemContextFromFile();
        // When
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getUserMessage();
        });
        // Then
        assertEquals("system role cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test getUserMessage does not throw when systemRole and requestData are not empty")
    void testGetUserMessageReturnsMessageWhenRequestDataAndSystemRoleAreFilled() {
        // Given
        String requestData = "am filled req data";
        String systemRole = "am filled sys role";
        String systemContext = "am filled sys ctx";
        Mockito.doReturn(requestData).when(apiRequest).getRequestDataAsString();
        Mockito.doReturn(systemRole).when(apiRequest).getSystemRoleFromFile();
        Mockito.doReturn(systemContext).when(apiRequest).getSystemContextFromFile();
        // When
        Message returnedMessage = apiRequest.getUserMessage();
        // Then
        assertNotNull(returnedMessage);
        assertEquals("am filled sys ctx\n\nam filled sys role\n\nam filled req data", returnedMessage.getText());
    }

    @Test
    @DisplayName("Test getSystemMessage throws APIRequestException when systemRoleMessage is empty")
    void testGetSystemMessageThrowsExceptionWhenSystemRoleMessageIsEmpty() {
        // Givem
        String systemRoleMessage = "";
        Mockito.doReturn(systemRoleMessage).when(apiRequest).getSystemRoleFromFile();
        // When + Then
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getSystemMessage();
        });
        assertNotNull(exception);
        assertEquals("ai_role_pt.txt cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Test getSystemMessage does not throw when systemRoleMessage is filled")
    void testGetSystemMessageDoesNotThrowWhenSystemRoleMessageIsFilled() {
        // Given
        String systemRoleMessage = "am filled";
        Mockito.doReturn(systemRoleMessage).when(apiRequest).getSystemRoleFromFile();
        // When
        Message returnedMessage = apiRequest.getSystemMessage();
        // Then
        assertNotNull(returnedMessage);
        assertEquals(systemRoleMessage, returnedMessage.getText());
    }

    private List<Message> buildDefaultMessageListOne() {
        Message userMessage = new UserMessage("test user message");
        Message systemMessage = new SystemMessage("test system message");
        return List.of(userMessage, systemMessage);
    }

    private List<Message> buildDefaultMessageListTwo() {
        Message userMessage = new UserMessage("test user message");
        Message systemMessage = new SystemMessage("");
        return List.of(userMessage, systemMessage);
    }

    private Message buildDefaultUserMessageOne() {
        return new UserMessage("test user message");
    }

    private Message buildDefaultSystemMessageOne() {
        return new SystemMessage("test system message");
    }

    private ChatResponse buildDefaultChatResponseOne() {
        AssistantMessage assistantMessage = new AssistantMessage("assistant message");
        Generation generation = new Generation(assistantMessage);
        List<Generation> generations = List.of(generation);
        return new ChatResponse(generations);
    }

    private List<AIResponse> buildDefaultAiResponseListOne() {
        AIResponse response1 = new AIResponse("test1");
        AIResponse response2 = new AIResponse("test2");
        return List.of(response1, response2);
    }

}

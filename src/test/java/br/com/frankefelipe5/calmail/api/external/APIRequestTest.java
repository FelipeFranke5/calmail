package br.com.frankefelipe5.calmail.api.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.exception.APIRequestException;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

@DisplayName("Test for APIRequest and its methods")
public class APIRequestTest {

    static Instant beforeAllInstant;
    static Instant afterAllInstant;
    static final Logger logger = LoggerFactory.getLogger(APIRequestTest.class);

    @Mock
    private OpenAiChatModel chatModel;

    @Mock
    private AIResponseRepository aiResponseRepository;

    private AIRequest request = buildDefaultRequestOne();

    private APIRequest apiRequest;

    @BeforeAll
    public static void setUpBeforeAll() {
        logger.info("Starting tests for APIRequest ..");
        beforeAllInstant = Instant.now();
    }

    @AfterAll
    public static void tearDownAfterAll() {
        afterAllInstant = Instant.now();
        Duration duration = Duration.between(beforeAllInstant, afterAllInstant);
        logger.info("Duration in seconds: " + duration.toSeconds());
        logger.info("Duration in milis: " + duration.toMillis());
        logger.info("End of tests for APIRequest ..");
    }

    @BeforeEach
    private void setUp() {
        MockitoAnnotations.openMocks(this);
        apiRequest = spy(new APIRequest(request, chatModel, aiResponseRepository));
    }

    @Test
    @DisplayName("Test getGeneratedResponse returns a String when we have a list of messages")
    void testGetGeneratedResponse_When_MessagesListHasOneMessage_ShouldReturnAssistantMessage() {
        List<Message> messages = buildDefaultMessageListOne();
        ChatResponse chatResponse = buildDefaultChatResponseOne();
        Mockito.when(aiResponseRepository.findAll()).thenReturn(buildDefaultAiResponseListOne());
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        Mockito.when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        String generatedResponse = apiRequest.getGeneratedResponse();
        assertNotNull(generatedResponse, () -> "generatedResponse returned null");
        assertTrue(generatedResponse.equals("assistant message"));
    }

    private AIRequest buildDefaultRequestOne() {
        return new AIRequest("Test", true, false, null, null, "Test message");
    }

    @Test
    @DisplayName("Test getPrompt throws APIRequestException when the list of messages is null")
    void testGetPrompt_When_MessagesListIsNull_ShouldThrowAPIRequestException() {
        List<Message> messages = null;
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getPrompt();
        });
        assertEquals(
                "list of messages cannot be null",
                exception.getMessage(),
                () -> "expected literal 'list of messages cannot be null', got '"
                        + exception.getMessage()
                        + "' as the exception message");
    }

    @Test
    @DisplayName("Test getPrompt does not throw when the list of messages is not null")
    void testGetPrompt_When_MessagesListIsNotNull_ShouldReturnPromptInstance() {
        List<Message> messages = buildDefaultMessageListOne();
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        Prompt prompt = apiRequest.getPrompt();
        assertNotNull(prompt, () -> "the prompt is null, which is not expected in this test");
        assertTrue(prompt.getInstructions().size() == 2, () -> "prompt.getInstructions().size() does not equal 2");
    }

    @Test
    @DisplayName("Test getPrompt throws APIRequestException when one of the messages have empty content")
    void testGetPrompt_When_MessagesListHasOneMessageWithEmptyContent_ShouldThrowAPIRequestException() {
        List<Message> messages = buildDefaultMessageListTwo();
        Mockito.doReturn(messages).when(apiRequest).getMessages();
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getPrompt();
        });
        assertEquals(
                "all messages should have content",
                exception.getMessage(),
                () -> "expected literal 'all messages should have content', got '"
                        + exception.getMessage()
                        + "' as the exception message");
    }

    @Test
    @DisplayName("Test getMessages throws APIRequestException when one of the messages is null")
    void testGetMessages_When_OneMessageIsNull_ShouldThrowAPIRequestException() {
        Message message1 = buildDefaultSystemMessageOne();
        Message message2 = null;
        Mockito.doReturn(message1).when(apiRequest).getSystemMessage();
        Mockito.doReturn(message2).when(apiRequest).getUserMessage();
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getMessages();
        });
        assertEquals(
                "user message cannot be null",
                exception.getMessage(),
                () -> "expected literal 'user message cannot be null', got '"
                        + exception.getMessage()
                        + "' as the exception message");
    }

    @Test
    @DisplayName("Test getMessages does not throw when all messages are not null")
    void testGetMessages_When_MessagesAreNotNull_ShouldReturnListOfMessages() {
        Message message1 = buildDefaultSystemMessageOne();
        Message message2 = buildDefaultUserMessageOne();
        Mockito.doReturn(message1).when(apiRequest).getSystemMessage();
        Mockito.doReturn(message2).when(apiRequest).getUserMessage();
        List<Message> messages = apiRequest.getMessages();
        assertNotNull(messages, () -> "'messages' is null, which is not excpected here");
        assertTrue(messages.size() == 2);
    }

    @Test
    @DisplayName("Test getUserMessage throws APIRequestException when requestData is empty")
    void testGetUserMessage_When_RequestDataIsEmpty_ShouldThrowAPIRequestException() {
        String requestData = "";
        String systemRole = "am filled";
        String systemContext = "am filled";
        Mockito.doReturn(requestData).when(apiRequest).getRequestDataAsString();
        Mockito.doReturn(systemRole).when(apiRequest).getSystemRoleFromFile();
        Mockito.doReturn(systemContext).when(apiRequest).getSystemContextFromFile();
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getUserMessage();
        });
        assertEquals(
                "request data cannot be empty",
                exception.getMessage(),
                () -> "got different String for exception.getMessage()");
    }

    @Test
    @DisplayName("Test getUserMessage throws APIRequestException when systemRole is empty")
    void testGetUserMessage_When_SystemRoleIsEmpty_ShouldThrowAPIRequestException() {
        String requestData = "am filled";
        String systemRole = "";
        String systemContext = "am filled";
        Mockito.doReturn(requestData).when(apiRequest).getRequestDataAsString();
        Mockito.doReturn(systemRole).when(apiRequest).getSystemRoleFromFile();
        Mockito.doReturn(systemContext).when(apiRequest).getSystemContextFromFile();
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getUserMessage();
        });
        assertEquals(
                "system role cannot be empty",
                exception.getMessage(),
                () -> "got different String for exception.getMessage()");
    }

    @Test
    @DisplayName("Test getUserMessage does not throw when systemRole and requestData are not empty")
    void testGetUserMessage_When_SystemRoleAndRequestDataAreNotNull_ShouldReturnMessageInstance() {
        String requestData = "am filled req data";
        String systemRole = "am filled sys role";
        String systemContext = "am filled sys ctx";
        Mockito.doReturn(requestData).when(apiRequest).getRequestDataAsString();
        Mockito.doReturn(systemRole).when(apiRequest).getSystemRoleFromFile();
        Mockito.doReturn(systemContext).when(apiRequest).getSystemContextFromFile();
        Message returnedMessage = apiRequest.getUserMessage();
        assertNotNull(returnedMessage);
        assertEquals("am filled sys ctx\n\nam filled sys role\n\nam filled req data", returnedMessage.getText());
    }

    @Test
    @DisplayName("Test getSystemMessage throws APIRequestException when systemRoleMessage is empty")
    void testGetSystemMessage_When_SystemRoleIsEmpty_ShouldThrowAPIRequestException() {
        String systemRoleMessage = "";
        Mockito.doReturn(systemRoleMessage).when(apiRequest).getSystemRoleFromFile();
        APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
            apiRequest.getSystemMessage();
        });
        assertNotNull(exception);
        assertEquals(
                "ai_role_pt.txt cannot be empty",
                exception.getMessage(),
                () -> "got different String for exception.getMessage()");
    }

    @Test
    @DisplayName("Test getSystemMessage does not throw when systemRoleMessage is filled")
    void testGetSystemMessage_When_SystemRoleIsNotEmpty_ShouldReturnSystemMessage() {
        String systemRoleMessage = "am filled";
        Mockito.doReturn(systemRoleMessage).when(apiRequest).getSystemRoleFromFile();
        Message returnedMessage = apiRequest.getSystemMessage();
        assertNotNull(returnedMessage, () -> "returnedMessage is null");
        assertEquals(systemRoleMessage, returnedMessage.getText());
    }

    @Test
    @DisplayName("Test getSystemRoleFromFile does not throw when file exists")
    void testGetSystemRoleFromFile_When_FileExists_ShouldReturnContent() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock
                    .when(() -> Files.readString(Paths.get("target/ai_role_pt.txt")))
                    .thenReturn("example");
            String actual = apiRequest.getSystemRoleFromFile();
            assertNotNull(actual, () -> "actual is null");
            assertEquals("example", actual, () -> "actual text is not 'example'");
        }
    }

    @Test
    @DisplayName("Test getSystemRoleFromFile throws APIRequestException when file does not exist")
    void testGetSystemRoleFromFile_When_FileDoesNotExist_ShouldThrow() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock
                    .when(() -> Files.readString(Paths.get("target/ai_role_pt.txt")))
                    .thenThrow(new IOException("could not open file 'ai_role_pt.txt', check if it exists"));
            APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
                apiRequest.getSystemRoleFromFile();
            });
            assertNotNull(exception);
            assertEquals("could not open file 'ai_role_pt.txt', check if it exists", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Test getSystemContextFromFile does not throw when file exists")
    void testGetSystemContextFromFile_When_FileExists_ShouldReturnContent() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock
                    .when(() -> Files.readString(Paths.get("target/responses.txt")))
                    .thenReturn("example");
            String actual = apiRequest.getSystemContextFromFile();
            assertNotNull(actual, () -> "actual is null");
            assertEquals("example", actual, () -> "actual text is not 'example'");
        }
    }

    @Test
    @DisplayName("Test getSystemContextFromFile throws APIRequestException when file does not exist")
    void testGetSystemContextFromFile_When_FileDoesNotExist_ShouldThrow() {
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock
                    .when(() -> Files.readString(Paths.get("target/responses.txt")))
                    .thenThrow(new IOException("could not open file 'responses.txt', check if it exists"));
            APIRequestException exception = assertThrowsExactly(APIRequestException.class, () -> {
                apiRequest.getSystemContextFromFile();
            });
            assertNotNull(exception);
            assertEquals("could not open file 'responses.txt', check if it exists", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Test getRequestDataAsString throws APIRequestException when request is null")
    void testGrtRequestDataAsString_When_RequestIsNull_Throws() {
        APIRequestException exception = assertThrowsExactly(
                APIRequestException.class,
                () -> {
                    APIRequest myApiRequest = spy(new APIRequest(null, chatModel, aiResponseRepository));
                    myApiRequest.getRequestDataAsString();
                },
                () ->
                        "getRequestDataAsString() was supposed to throw APIRequestException when the value of request is null");
        assertNotNull(exception);
        assertEquals("request is null", exception.getMessage());
    }

    @Test
    @DisplayName("Test getRequestDataAsString returns content")
    void testGetRequestDataAsString_When_RequestIsNotNull_ReturnsContent() {
        AIRequest customRequest = new AIRequest("example", false, false, "example");
        APIRequest customAPIRequest = spy(new APIRequest(customRequest, chatModel, aiResponseRepository));
        String requestData = customAPIRequest.getRequestDataAsString();
        assertTrue(requestData.contains("Nome: example"), () -> "requestData does not contain 'Nome: example'");
        assertTrue(
                requestData.contains("Problema Recorrente: Não"),
                () -> "requestData does not contain 'Problema Recorrente: Não'");
        assertTrue(
                requestData.contains("Possui Protocolo: Não"),
                () -> "requestData does not contain 'Possui protocolo: Não'");
        assertTrue(requestData.contains("Mensagem: example"), () -> "requestData does not contain 'Mensagem: example'");
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

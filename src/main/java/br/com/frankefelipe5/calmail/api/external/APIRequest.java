package br.com.frankefelipe5.calmail.api.external;

import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.exception.APIRequestException;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import br.com.frankefelipe5.calmail.api.util.AIResponseFileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;

public class APIRequest {

    private final Logger logger = LoggerFactory.getLogger(APIRequest.class);
    private final AIRequest request;
    private final OpenAiChatModel chatModel;
    private final AIResponseRepository aiResponseRepository;

    public APIRequest(AIRequest request, OpenAiChatModel chatModel, AIResponseRepository aiResponseRepository) {
        this.request = request;
        this.chatModel = chatModel;
        this.aiResponseRepository = aiResponseRepository;
    }

    public String getGeneratedResponse() {
        ChatResponse response = chatModel.call(getPrompt());
        return response.getResult().getOutput().getText();
    }

    public Prompt getPrompt() {
        List<Message> messages = getMessages();
        if (messages == null) throw new APIRequestException("list of messages cannot be null");
        messages.stream().forEach(message -> {
            if (message.getText().isBlank() || message.getText().isEmpty()) {
                throw new APIRequestException("all messages should have content");
            }
        });
        return new Prompt(messages);
    }

    public List<Message> getMessages() {
        Message systemMessage = getSystemMessage();
        Message userMessage = getUserMessage();
        if (systemMessage == null) throw new APIRequestException("system message cannot be null");
        if (userMessage == null) throw new APIRequestException("user message cannot be null");
        return List.of(systemMessage, userMessage);
    }

    public Message getUserMessage() {
        AIResponseFileWriter responseFileWriter = new AIResponseFileWriter(aiResponseRepository);
        responseFileWriter.createFileWithResponses();
        String requestData = getRequestDataAsString();
        String systemRole = getSystemRoleFromFile();
        String systemContext = getSystemContextFromFile();
        if (requestData.isBlank() || requestData.isEmpty())
            throw new APIRequestException("request data cannot be empty");
        if (systemRole.isBlank() || systemRole.isEmpty()) throw new APIRequestException("system role cannot be empty");
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder
                .append(systemContext)
                .append("\n\n")
                .append(systemRole)
                .append("\n\n")
                .append(requestData);
        String message = messageBuilder.toString();
        responseFileWriter.deleteFileWithResponses();
        return new UserMessage(message);
    }

    public Message getSystemMessage() {
        String systemRoleMessage = getSystemRoleFromFile();
        if (systemRoleMessage.isBlank() || systemRoleMessage.isEmpty())
            throw new APIRequestException("ai_role_pt.txt cannot be empty");
        return new SystemMessage(systemRoleMessage);
    }

    public String getSystemRoleFromFile() {
        try {
            return Files.readString(Paths.get("target/ai_role_pt.txt"));
        } catch (IOException ioException) {
            logger.error("ERROR WHILE TRYING TO GET THE SYSTEM ROLE !!!", ioException);
            throw new APIRequestException("could not open file 'ai_role_pt.txt', check if it exists");
        }
    }

    public String getSystemContextFromFile() {
        try {
            return Files.readString(Paths.get("target/responses.txt"));
        } catch (IOException ioException) {
            logger.error("ERROR WHILE TRYING TO GET THE SYSTEM CONTEXT !!!", ioException);
            throw new APIRequestException("could not open file 'responses.txt', check if it exists");
        }
    }

    public String getRequestDataAsString() {
        if (this.request == null) throw new APIRequestException("request is null");
        StringBuilder builder = new StringBuilder();
        if (this.request.name() != null) {
            builder.append("Nome: " + this.request.name());
            builder.append("\n");
        }
        if (this.request.recurrent() == true) {
            builder.append("Problema Recorrente: Sim");
            builder.append("\n");
        } else {
            builder.append("Problema Recorrente: Não");
            builder.append("\n");
        }
        if (this.request.hasProtocol() == true) {
            builder.append("Possui Protocolo: Sim - ");
            builder.append("Prazo de " + this.request.protocolDeadlineInDays() + " dias úteis - ");
            builder.append("Status: " + this.request.protocolStatus());
            builder.append("\n");
        } else {
            builder.append("Possui Protocolo: Não");
            builder.append("\n");
        }
        builder.append("Mensagem: " + this.request.message());
        return builder.toString();
    }
}

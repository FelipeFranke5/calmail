package br.com.frankefelipe5.calmail.api.util;

import br.com.frankefelipe5.calmail.api.exception.AIResponseException;
import br.com.frankefelipe5.calmail.api.model.AIResponse;
import br.com.frankefelipe5.calmail.api.repository.AIResponseRepository;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AIResponseFileWriter {

    private final AIResponseRepository aiResponseRepository;
    private final String FILE_EXTENSTION = "src/main/resources/responses.txt";
    private Logger logger = LoggerFactory.getLogger(AIResponseFileWriter.class);

    public AIResponseFileWriter(AIResponseRepository aiResponseRepository) {
        this.aiResponseRepository = aiResponseRepository;
    }

    public void createFileWithResponses() {
        List<AIResponse> aiResponses = aiResponseRepository.findAll();
        StringBuilder content = new StringBuilder();
        try (FileWriter writer = new FileWriter(FILE_EXTENSTION)) {
            aiResponses.stream().forEach(response -> {
                content.append("---- START RESPONSE WITH ID " + response.getId() + " ----\n");
                content.append(response.getResponseText() + "\n");
                content.append("---- END CONTENT WITH ID " + response.getId() + " ----\n");
            });
            writer.write(content.toString());
        } catch (IOException ioException) {
            logger.error("ERROR CREATING FILE!!!", ioException);
            throw new AIResponseException("an error ocurred while creating the file containing saved responses");
        }
    }

    public void deleteFileWithResponses() {
        Path filePath = Paths.get(FILE_EXTENSTION);
        try {
            Files.delete(filePath);
        } catch (IOException ioException) {
            logger.error("ERROR DELETING FILE!!!", ioException);
            throw new AIResponseException("an error ocurred while deleting the file containing saved responses");
        }
    }
}

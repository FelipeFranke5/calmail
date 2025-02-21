package br.com.frankefelipe5.calmail.api.controller;

import br.com.frankefelipe5.calmail.api.service.AIResponseService;
import java.util.List;
import java.util.UUID;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.Request;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api")
public class AIResponseController {

    private final AIResponseService aiResponseService;

    public AIResponseController(AIResponseService aiResponseService) {
        this.aiResponseService = aiResponseService;
    }

    // Methods that do require ID below

    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<AIResponseDTO> getResponseById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(aiResponseService.findResponseById(id));
    }

    @CrossOrigin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponseById(@PathVariable UUID id) {
        aiResponseService.deleteResponseById(id);
        return ResponseEntity.noContent().build();
    }

    // Methods that do not require ID below

    @CrossOrigin
    @GetMapping("/list")
    public ResponseEntity<List<AIResponseDTO>> listResponses(
        @RequestParam(required = false, defaultValue = "false") String orderByCreatedAt
    ) {

        boolean order;
        if (!orderByCreatedAt.equalsIgnoreCase("true") && !orderByCreatedAt.equalsIgnoreCase("false")) {
            order = false;
        }
        order = Boolean.parseBoolean(orderByCreatedAt);
        return ResponseEntity.ok().body(aiResponseService.listAll(order));
    }

    @CrossOrigin
    @PostMapping("/create")
    public ResponseEntity<AIResponseDTO> createScript(@RequestBody Request requestBody) {
        AIResponseDTO response = aiResponseService.saveData(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @CrossOrigin
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        aiResponseService.clearResponses();
        return ResponseEntity.noContent().build();
    }
}

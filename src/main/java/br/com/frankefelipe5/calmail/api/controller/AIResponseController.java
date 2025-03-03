package br.com.frankefelipe5.calmail.api.controller;

import br.com.frankefelipe5.calmail.api.dto.AIRequest;
import br.com.frankefelipe5.calmail.api.dto.AIResponseDTO;
import br.com.frankefelipe5.calmail.api.service.AIResponseService;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responses")
public class AIResponseController {

  private final AIResponseService aiResponseService;

  public AIResponseController(AIResponseService aiResponseService) {
    this.aiResponseService = aiResponseService;
  }

  // Methods that do require ID

  @CrossOrigin
  @GetMapping("/generated/{id}")
  public ResponseEntity<AIResponseDTO> getResponseById(@PathVariable UUID id) {
    return ResponseEntity.ok().body(aiResponseService.findResponseById(id));
  }

  @CrossOrigin
  @DeleteMapping("/generated/{id}")
  public ResponseEntity<Void> deleteResponseById(@PathVariable UUID id) {
    aiResponseService.deleteResponseById(id);
    return ResponseEntity.noContent().build();
  }

  // Methods that do not require ID

  @CrossOrigin
  @GetMapping("/generated")
  public ResponseEntity<HashMap<String, Object>> listResponses(
      @RequestParam(required = false, defaultValue = "false") String orderByCreatedAt) {

    boolean order;
    boolean orderStringIsTrue = orderByCreatedAt.equalsIgnoreCase("true");
    boolean orderStringIsFalse = orderByCreatedAt.equalsIgnoreCase("false");
    order =
        (!orderStringIsTrue && !orderStringIsFalse)
            ? false
            : Boolean.parseBoolean(orderByCreatedAt);
    HashMap<String, Object> responseBody = new HashMap<>();
    responseBody.put("_results", aiResponseService.listAll(order));
    responseBody.put("order", order);
    return ResponseEntity.ok().body(responseBody);
  }

  @CrossOrigin
  @PostMapping("/create")
  public ResponseEntity<AIResponseDTO> createScript(@RequestBody AIRequest requestBody) {
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

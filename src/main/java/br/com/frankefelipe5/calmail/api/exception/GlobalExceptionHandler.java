package br.com.frankefelipe5.calmail.api.exception;

import br.com.frankefelipe5.calmail.api.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger debugger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(RequestException.class)
  public ResponseEntity<ErrorResponse> handleRequest(RequestException e) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.REQUEST_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(APIRequestException.class)
  public ResponseEntity<ErrorResponse> handleAPIRequest(APIRequestException e) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.API_REQUEST_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(AIResponseException.class)
  public ResponseEntity<ErrorResponse> handleAIResponse(AIResponseException e) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.AI_RESPONSE_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(AIResponseSQLException.class)
  public ResponseEntity<ErrorResponse> handleSQLException(AIResponseSQLException e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.AI_RESPONSE_SQL_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(AIResponseNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(AIResponseNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
        .body(new ErrorResponse(ErrorCode.AI_RESPONSE_NOT_FOUND_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.SECURITY_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMethod(
      HttpRequestMethodNotSupportedException e) {
    Map<String, Object> detail = new HashMap<>();
    Map<String, Object> error = new HashMap<>();
    detail.put("supportedMethods", e.getSupportedMethods());
    detail.put("usedMethod", e.getMethod());
    detail.put("message", "Unsupported Method for this route");
    error.put("$unsupportedMethodDetail", detail);
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(
            new ErrorResponse(
                ErrorCode.HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION.value(), error));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidArgs(MethodArgumentNotValidException e) {
    List<Map<String, String>> errors = new ArrayList<>();
    Map<String, Object> detail = new HashMap<>();
    e.getFieldErrors().stream()
        .forEach(
            errorField -> {
              HashMap<String, String> error = new HashMap<>();
              error.put(errorField.getField(), errorField.getDefaultMessage());
              errors.add(error);
            });
    detail.put("errors", errors);
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.METHOD_ARGUMENT_NOT_VALID_EXCEPTION.value(), detail));
  }

  @ExceptionHandler(UserDTOException.class)
  public ResponseEntity<ErrorResponse> handleUserDTOException(UserDTOException e) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.USER_DTO_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler({AuthResponseException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResponse> handleAuthResponseException() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
        .body(
            new ErrorResponse(
                ErrorCode.AUTH_RESPONSE_EXCEPTION.value(), "please authenticate to proceed"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.ILLEGAL_ARGUMENT_EXCEPTION.value(), e.getMessage()));
  }

  @ExceptionHandler(HttpClientErrorException.class)
  public ResponseEntity<ErrorResponse> handleHttpClientErrorExternalCall(
      HttpClientErrorException e) {
    HashMap<String, Object> detail = new HashMap<>();
    detail.put("message", "client error calling an external service");
    detail.put("externalServiceStatusCode", e.getStatusCode().value());
    detail.put("externalServiceStatusText", e.getStatusText());
    detail.put("externalServiceResponseBody", e.getResponseBodyAsString());
    return ResponseEntity.status(e.getStatusCode().value())
        .body(new ErrorResponse(ErrorCode.HTTP_CLIENT_ERROR_EXCEPTION.value(), detail));
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public ResponseEntity<ErrorResponse> handleHttpClientServerExternalCall(
      HttpServerErrorException e) {
    HashMap<String, Object> detail = new HashMap<>();
    detail.put("message", "server error calling an external service");
    detail.put("externalServiceStatusCode", e.getStatusCode().value());
    detail.put("externalServiceStatusText", e.getStatusText());
    detail.put("externalServiceResponseBody", e.getResponseBodyAsString());
    return ResponseEntity.status(e.getStatusCode().value())
        .body(new ErrorResponse(ErrorCode.HTTP_SERVER_ERROR_EXCEPTION.value(), detail));
  }

  @ExceptionHandler(NonTransientAiException.class)
  public ResponseEntity<ErrorResponse> handleNonTransientAiException(NonTransientAiException e) {
    boolean returnIs401 = e.getMessage().startsWith("401");
    boolean returnIs400 = e.getMessage().startsWith("400");
    boolean returnIs429 = e.getMessage().startsWith("429");

    if (!(returnIs401 || returnIs400 || returnIs429)) {
      return this.invalidOperationMessage(e);
    }
    debugger.error("error while calling AI: ", e);
    String[] separatedResponse = e.getMessage().split("- ");
    String jsonPart = separatedResponse[1];
    try {
      return this.errorMessage(returnIs401, returnIs400, jsonPart);
    } catch (JsonProcessingException notAJson) {
      return this.parseErrorMessage(notAJson);
    }
  }

  @ExceptionHandler(AIResponseAcessNotGrantedException.class)
  public ResponseEntity<ErrorResponse> handleAIResponseAcessNotGrantedException(AIResponseAcessNotGrantedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).body(new ErrorResponse(ErrorCode.ACESS_NOT_GRANTED_EXCEPTION.value(), e.getMessage()));
  }

  // Helper functions below

  private ResponseEntity<ErrorResponse> errorMessage(
      boolean returnIs401, boolean returnIs400, String jsonPart)
      throws JsonProcessingException, JsonMappingException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode responseNode = mapper.readTree(jsonPart);
    if (returnIs401) {
      return this.unauthorizedMessage();
    } else if (returnIs400) {
      return this.badRequestMessage(responseNode);
    } else {
      return this.limitRateMessage();
    }
  }

  private ResponseEntity<ErrorResponse> parseErrorMessage(JsonProcessingException notAJson) {
    debugger.error("error parsing because: ", notAJson);
    return ResponseEntity.internalServerError()
        .body(
            new ErrorResponse(
                ErrorCode.PARSING_JSON.value(), "internal server error - parsing error"));
  }

  private ResponseEntity<ErrorResponse> limitRateMessage() {
    return ResponseEntity.internalServerError()
        .body(
            new ErrorResponse(
                ErrorCode.NON_TRANSIENT_AI_EXCEPTION.value(), "reached usage limit for AI API"));
  }

  private ResponseEntity<ErrorResponse> badRequestMessage(JsonNode responseNode) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
        .body(new ErrorResponse(ErrorCode.NON_TRANSIENT_AI_EXCEPTION.value(), responseNode));
  }

  private ResponseEntity<ErrorResponse> unauthorizedMessage() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
        .body(
            new ErrorResponse(
                ErrorCode.NON_TRANSIENT_AI_EXCEPTION.value(), "unauthorized acess to the AI API"));
  }

  private ResponseEntity<ErrorResponse> invalidOperationMessage(NonTransientAiException e) {
    debugger.error("called: ", e);
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(ErrorCode.NON_TRANSIENT_AI_EXCEPTION.value(), "invalid operation"));
  }
}

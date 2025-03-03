package br.com.frankefelipe5.calmail.api.exception;

import br.com.frankefelipe5.calmail.api.dto.ErrorResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

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

  @ExceptionHandler(AuthResponseException.class)
  public ResponseEntity<ErrorResponse> handleAuthResponseException(AuthResponseException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value())
        .body(new ErrorResponse(ErrorCode.AUTH_RESPONSE_EXCEPTION.value(), e.getMessage()));
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
}

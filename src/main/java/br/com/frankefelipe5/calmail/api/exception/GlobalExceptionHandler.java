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

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RequestException.class)
  public ResponseEntity<ErrorResponse> handleRequest(RequestException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(1, e.getMessage()));
  }

  @ExceptionHandler(APIRequestException.class)
  public ResponseEntity<ErrorResponse> handleAPIRequest(APIRequestException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(2, e.getMessage()));
  }

  @ExceptionHandler(AIResponseException.class)
  public ResponseEntity<ErrorResponse> handleAIResponse(AIResponseException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(3, e.getMessage()));
  }

  @ExceptionHandler(AIResponseSQLException.class)
  public ResponseEntity<ErrorResponse> handleSQLException(AIResponseSQLException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(4, e.getMessage()));
  }

  @ExceptionHandler(AIResponseNotFoundException.class)
  public ResponseEntity<Void> handleNotFoundException(AIResponseNotFoundException e) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(5, "security error"));
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
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ErrorResponse(6, error));
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
    return ResponseEntity.badRequest().body(new ErrorResponse(7, detail));
  }

  @ExceptionHandler(UserDTOException.class)
  public ResponseEntity<ErrorResponse> handleUserDTOException(UserDTOException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(8, e.getMessage()));
  }

  @ExceptionHandler(AuthResponseException.class)
  public ResponseEntity<ErrorResponse> handleAuthResponseException(AuthResponseException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(9, e.getMessage()));
  }
}

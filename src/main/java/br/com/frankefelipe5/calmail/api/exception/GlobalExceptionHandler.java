package br.com.frankefelipe5.calmail.api.exception;

import br.com.frankefelipe5.calmail.api.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
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
}

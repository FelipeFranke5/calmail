package br.com.frankefelipe5.calmail.api.exception;

public class AuthResponseException extends RuntimeException {

    public AuthResponseException(String message) {
        super(message);
    }

    public AuthResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

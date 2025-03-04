package br.com.frankefelipe5.calmail.api.exception;

public class SecurityException extends RuntimeException {
    public SecurityException(Throwable rootCause) {
        super(rootCause);
    }
}

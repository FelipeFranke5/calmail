package br.com.frankefelipe5.calmail.api.exception;

public enum ErrorCode {
    REQUEST_EXCEPTION(1),
    API_REQUEST_EXCEPTION(2),
    AI_RESPONSE_EXCEPTION(3),
    AI_RESPONSE_SQL_EXCEPTION(4),
    AI_RESPONSE_NOT_FOUND_EXCEPTION(5),
    SECURITY_EXCEPTION(6),
    HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION(7),
    METHOD_ARGUMENT_NOT_VALID_EXCEPTION(8),
    USER_DTO_EXCEPTION(9),
    AUTH_RESPONSE_EXCEPTION(10),
    ILLEGAL_ARGUMENT_EXCEPTION(11),
    HTTP_CLIENT_ERROR_EXCEPTION(12),
    HTTP_SERVER_ERROR_EXCEPTION(13),
    NON_TRANSIENT_AI_EXCEPTION(14),
    PARSING_JSON(15),
    ACESS_NOT_GRANTED_EXCEPTION(16);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }
}

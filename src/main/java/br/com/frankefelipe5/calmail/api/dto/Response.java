package br.com.frankefelipe5.calmail.api.dto;

public record Response(int status, long requestDurationInSeconds, String response) {
}

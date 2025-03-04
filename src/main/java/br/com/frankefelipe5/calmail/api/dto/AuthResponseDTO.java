package br.com.frankefelipe5.calmail.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponseDTO {

    @JsonProperty("access_token")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}

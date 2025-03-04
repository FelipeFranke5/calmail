package br.com.frankefelipe5.calmail.api.dto;

import br.com.frankefelipe5.calmail.api.exception.UserDTOException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDTO(
        @NotBlank @Size(max = 30) String clientId,
        @NotBlank @Size(max = 30) String username,
        @NotBlank @Size(max = 254) String password,
        @NotBlank String grantType) {

    public UserDTO {
        if (clientId == null || username == null || password == null || grantType == null) {
            throw new UserDTOException("missing one of required fields: clientId, username, password, grantType");
        }
        if (!grantType.equalsIgnoreCase("password")) {
            throw new UserDTOException("grantType must be password");
        }
    }
}

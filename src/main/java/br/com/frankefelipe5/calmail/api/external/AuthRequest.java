package br.com.frankefelipe5.calmail.api.external;

import br.com.frankefelipe5.calmail.api.dto.AuthResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.exception.AuthResponseException;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

public class AuthRequest {

    private UserDTO userDTO;
    private RestClient restClient;
    private MultiValueMap<String, String> body;

    public AuthRequest(UserDTO userDTO) {
        this.setUserDTO(userDTO);
        this.setRestClient();
        this.setBody();
    }

    public String getBaseURL() {
        return System.getenv("KC_TOKEN_BASE_URL");
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    private void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public MultiValueMap<String, String> getBody() {
        return body;
    }

    private void setBody() {
        this.body = new LinkedMultiValueMap<>();
        this.body.add("client_id", this.userDTO.clientId());
        this.body.add("username", this.userDTO.username());
        this.body.add("password", this.userDTO.password());
        this.body.add("grant_type", this.userDTO.grantType());
    }

    private void setRestClient() {
        this.restClient = RestClient.builder().baseUrl(this.getBaseURL()).build();
    }

    public String getAccessToken() {
        AuthResponseDTO response = this.getRestClient()
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(this.getBody())
                .retrieve()
                .body(AuthResponseDTO.class);
        if (response == null) throw new AuthResponseException("authorization service returned null");
        return response.getAccessToken();
    }
}

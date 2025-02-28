package br.com.frankefelipe5.calmail.api.external;

import br.com.frankefelipe5.calmail.api.dto.AuthResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.exception.AuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class AuthRequest {

  private static final Logger logger = LoggerFactory.getLogger(AuthRequest.class);
  private UserDTO userDTO;
  private RestClient restClient;
  private MultiValueMap<String, String> body;

  public AuthRequest(UserDTO userDTO) {
    this.userDTO = userDTO;
    this.setRestClient();
    this.setBody();
  }

  public String getBaseURL() {
    try {
      return System.getenv("KC_TOKEN_BASE_URL");
    } catch (Exception exception) {
      throw new AuthResponseException("could not get base URL for authorization service");
    }
  }

  public UserDTO getUserDTO() {
    return userDTO;
  }

  public void setUserDTO(UserDTO userDTO) {
    this.userDTO = userDTO;
  }

  public RestClient getRestClient() {
    return restClient;
  }

  public MultiValueMap<String, String> getBody() {
    return body;
  }

  public void setBody() {
    this.body = new LinkedMultiValueMap<>();
    this.body.add("client_id", this.userDTO.clientId());
    this.body.add("username", this.userDTO.username());
    this.body.add("password", this.userDTO.password());
    this.body.add("grant_type", this.userDTO.grantType());
  }

  public void setRestClient() {
    this.restClient = RestClient.builder().baseUrl(this.getBaseURL()).build();
  }

  public String getAccessToken() {
    try {
      AuthResponseDTO response =
          this.restClient
              .post()
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(this.getBody())
              .retrieve()
              .body(AuthResponseDTO.class);
      if (response == null) {
        logger.error("error getting access token - response is null");
        logger.error("payload: " + this.getBody().toString());
        throw new AuthResponseException(
            "error getting access token - authorization service returned null");
      }
      return response.getAccessToken();
    } catch (RestClientResponseException httpCallException) {
      StringBuilder errorMessage = new StringBuilder();
      errorMessage
          .append("authorization service returned with status code: ")
          .append(httpCallException.getStatusCode().value())
          .append(" - messge: ")
          .append(httpCallException.getResponseBodyAsString());
      throw new AuthResponseException(errorMessage.toString());
    } catch (Exception e) {
      logger.error("error getting access token - exception raised");
      logger.error("payload: " + this.getBody().toString());
      logger.error("exception message: " + e.getMessage());
      throw new AuthResponseException("null");
    }
  }
}

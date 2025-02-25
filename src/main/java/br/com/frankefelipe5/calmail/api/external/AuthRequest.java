package br.com.frankefelipe5.calmail.api.external;

import br.com.frankefelipe5.calmail.api.dto.AuthResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.exception.AuthResponseException;
import java.net.URI;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.BodyInserters.FormInserter;

public class AuthRequest {

  private static final String KC_TOKEN_BASE_URL = System.getenv("KC_ISSUER_URL");

  private static final Logger logger = LoggerFactory.getLogger(AuthRequest.class);
  private UserDTO userDTO;
  private RestClient restClient;
  private FormInserter<String> body;

  public AuthRequest(UserDTO userDTO) {
    this.userDTO = userDTO;
    this.setRestClient();
    this.setBody();
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

  public FormInserter<String> getBody() {
    return body;
  }

  public void setBody() {
    this.body =
        BodyInserters.fromFormData("client_id", this.userDTO.clientId())
            .with("username", this.userDTO.username())
            .with("password", this.userDTO.password())
            .with("grant_type", this.userDTO.grantType());
  }

  public void setRestClient() {
    this.restClient =
        RestClient.builder()
            .baseUrl(KC_TOKEN_BASE_URL)
            .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
  }

  public String getAccessToken() {
    try {
      AuthResponseDTO response =
          this.restClient
              .post()
              .uri(URI.create("protocol/openid-connect/token"))
              .body(this.getBody())
              .retrieve()
              .body(AuthResponseDTO.class);
      if (response == null) {
        logger.error("error getting access token");
        logger.error(this.getBody().toString());
        throw new AuthResponseException("error getting access token");
      }
      return response.getAccessToken();
    } catch (RestClientResponseException httpCallException) {
      HashMap<String, Object> detail = new HashMap<>();
      detail.put("originalStatusCode", httpCallException.getStatusCode().value());
      detail.put("originalBody", httpCallException.getResponseBodyAsString());
      String errorMessage =
          "could not get acess token because the Resource Server returned an error: "
              + detail.toString();
      throw new AuthResponseException(errorMessage);
    } catch (Exception e) {
      logger.error("error getting access token - exception raised");
      logger.error("payload: " + this.getBody().toString());
      logger.error("exception message: " + e.getMessage());
      throw new AuthResponseException("could not get access token");
    }
  }
}

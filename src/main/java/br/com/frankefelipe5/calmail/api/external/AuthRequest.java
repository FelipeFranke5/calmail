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

public class AuthRequest {

  private static final String KC_TOKEN_BASE_URL = System.getenv("KC_ISSUER_URL");

  private static final Logger logger = LoggerFactory.getLogger(AuthRequest.class);
  private UserDTO userDTO;
  private RestClient restClient;
  private HashMap<String, String> body;

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

  public HashMap<String, String> getBody() {
    return body;
  }

  public void setBody() {
    this.body = new HashMap<>();
    this.body.put("client_id", this.userDTO.clientId());
    this.body.put("username", this.userDTO.username());
    this.body.put("password", this.userDTO.password());
    this.body.put("grant_type", this.userDTO.grantType());
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
      logger.error(this.getBody().toString());
      logger.error(e.getMessage());
      logger.error(e.getStackTrace().toString());
      throw new AuthResponseException("error getting access token because of an internal error");
    }
  }
}

package br.com.frankefelipe5.calmail.api.external;

import br.com.frankefelipe5.calmail.api.dto.AuthResponseDTO;
import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.exception.AuthResponseException;
import java.net.URI;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public class AuthRequest {

  private static final String KC_TOKEN_BASE_URL = System.getenv("KC_ISSUER_URL");

  private static final Logger logger = LoggerFactory.getLogger(AuthRequest.class);
  private UserDTO userDTO;
  private RestClient restClient;
  private MultiValueMap<String, String> body;

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
    this.restClient =
        RestClient.builder()
            .baseUrl(KC_TOKEN_BASE_URL)
            .build();
  }

  public String getAccessToken() {
    try {
      AuthResponseDTO response =
          this.restClient
              .post()
              .uri(URI.create("protocol/openid-connect/token"))
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
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

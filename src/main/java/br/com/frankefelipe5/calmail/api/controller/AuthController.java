package br.com.frankefelipe5.calmail.api.controller;

import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import javax.validation.Valid;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private String keyCloakBaseURL = System.getenv("KC_ISSUER_URL");

  @PostMapping
  public ResponseEntity<String> getAuthToken(@RequestBody @Valid UserDTO userDTO) {
    HttpHeaders headers = new HttpHeaders();
    RestTemplate restTemplate = new RestTemplate();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> data = getData(userDTO);
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(data, headers);
    return restTemplate.postForEntity(keyCloakBaseURL, httpEntity, String.class);
  }

  private MultiValueMap<String, String> getData(UserDTO userDTO) {
    MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
    data.add("client_id", userDTO.clientId());
    data.add("username", userDTO.username());
    data.add("password", userDTO.password());
    data.add("grant_type", userDTO.grantType());
    return data;
  }
}

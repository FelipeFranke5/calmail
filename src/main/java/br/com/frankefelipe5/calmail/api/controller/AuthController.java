package br.com.frankefelipe5.calmail.api.controller;

import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.service.AuthService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping
  public ResponseEntity<String> getAuthToken(@RequestBody @Valid UserDTO userDTO) {
    return ResponseEntity.ok(this.authService.getAuthToken(userDTO));
  }
}

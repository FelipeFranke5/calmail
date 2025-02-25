package br.com.frankefelipe5.calmail.api.service;

import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.external.AuthRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private AuthRequest authRequest;

  public String getAuthToken(UserDTO userDTO) {
    this.authRequest = new AuthRequest(userDTO);
    return this.authRequest.getAccessToken();
  }
}

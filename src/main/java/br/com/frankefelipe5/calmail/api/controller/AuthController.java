package br.com.frankefelipe5.calmail.api.controller;

import br.com.frankefelipe5.calmail.api.dto.UserDTO;
import br.com.frankefelipe5.calmail.api.service.AuthService;
import java.util.HashMap;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    @CrossOrigin
    @PostMapping
    public ResponseEntity<HashMap<String, Object>> getAuthToken(@RequestBody @Valid UserDTO userDTO) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("acess_token", this.authService.getAuthToken(userDTO));
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);
    }
}

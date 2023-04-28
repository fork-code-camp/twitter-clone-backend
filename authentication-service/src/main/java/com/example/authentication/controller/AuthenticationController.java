package com.example.authentication.controller;

import com.example.authentication.dto.request.AuthenticationRequest;
import com.example.authentication.dto.request.RegisterRequest;
import com.example.authentication.dto.response.ActivationCodeResponse;
import com.example.authentication.dto.response.AuthenticationResponse;
import com.example.authentication.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ActivationCodeResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/activate")
    public ResponseEntity<ActivationCodeResponse> activate(
            @RequestParam
            @Pattern(regexp = "^\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}$", message = "{activation.invalid}")
            String activationCode
    ) {
        return ResponseEntity.ok(authenticationService.activate(activationCode));
    }
}

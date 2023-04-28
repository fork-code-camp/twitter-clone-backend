package com.example.authentication.controller;

import com.example.authentication.dto.response.ActivationCodeResponse;
import com.example.authentication.dto.request.AuthenticationRequest;
import com.example.authentication.dto.response.AuthenticationResponse;
import com.example.authentication.dto.request.RegisterRequest;
import com.example.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ActivationCodeResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/activate")
    public ResponseEntity<ActivationCodeResponse> activate(@RequestParam String activationCode) {
        return ResponseEntity.ok(authenticationService.activate(activationCode));
    }
}

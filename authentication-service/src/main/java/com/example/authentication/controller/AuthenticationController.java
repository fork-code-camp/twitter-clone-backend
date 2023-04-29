package com.example.authentication.controller;

import com.example.authentication.dto.ActivationCodeResponse;
import com.example.authentication.dto.AuthenticationRequest;
import com.example.authentication.dto.AuthenticationResponse;
import com.example.authentication.dto.RegisterRequest;
import com.example.authentication.service.AuthenticationService;
import com.example.authentication.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

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

    @GetMapping("/validate/{jwt}")
    public ResponseEntity<Boolean> validateJwt(@PathVariable String jwt) {
        return ResponseEntity.ok(tokenService.isTokenValid(jwt));
    }
}

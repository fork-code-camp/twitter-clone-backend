package com.example.authentication.service;

import com.example.authentication.entity.Account;
import com.example.authentication.entity.Token;
import com.example.authentication.exception.InvalidTokenException;
import com.example.authentication.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import static com.example.authentication.model.TokenType.BEARER;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public void createToken(Account account, String jwt) {
        tokenRepository.save(
                Token.builder()
                        .account(account)
                        .jwt(jwt)
                        .tokenType(BEARER)
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    public void deleteTokenByAccount(Account account) {
        tokenRepository.findByAccount_Id(account.getId())
                .ifPresent(tokenRepository::delete);
    }

    public String isTokenValid(String jwt) {
        UserDetails userDetails = extractUserDetails(jwt);
        boolean isTokenValid = tokenRepository.findByJwt(jwt)
                .map(token -> !token.isExpired() && !token.isRevoked())
                .orElse(false);

        if (isTokenValid && jwtService.isJwtValid(jwt, userDetails)) {
            return userDetails.getUsername();
        } else {
            throw new InvalidTokenException("Authentication token is invalid!");
        }
    }

    private UserDetails extractUserDetails(String jwt) {
        String email = jwtService.extractEmail(jwt);
        return userDetailsService.loadUserByUsername(email);
    }
}

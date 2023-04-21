package com.example.authentication.service;

import com.example.authentication.entity.Account;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final AccountTokenService accountTokenService;
    private final AccountService accountService;
    private final JwtService jwtService;
    private final MessageSourceService messageService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7);
        String userEmail = jwtService.extractEmail(jwt);
        Account account = accountService.findAccountByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("account by email {} not found", userEmail);
                    throw new EntityNotFoundException(messageService.generateMessage(
                            "error.entity.not_found", userEmail
                    ));
                });

        accountTokenService.deleteAccountToken(account);
        SecurityContextHolder.clearContext();
        log.info("user with email {} has logged out", userEmail);
    }
}

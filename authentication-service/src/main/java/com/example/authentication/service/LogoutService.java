package com.example.authentication.service;

import com.example.authentication.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenService tokenService;
    private final AccountService accountService;
    private final JwtService jwtService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String jwt = jwtService.extractJwt(request);

        if (jwt != null && !jwt.isEmpty()) {
            String email = jwtService.extractEmail(jwt);
            Account account = accountService.findAccountByEmail(email);

            tokenService.deleteTokenByAccount(account);
            SecurityContextHolder.clearContext();
            log.info("user with email {} has logged out", email);
        }
    }
}

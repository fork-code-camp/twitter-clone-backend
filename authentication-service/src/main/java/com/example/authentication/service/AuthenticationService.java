package com.example.authentication.service;

import com.example.authentication.client.ProfileServiceClient;
import com.example.authentication.client.request.CreateProfileRequest;
import com.example.authentication.dto.request.AuthenticationRequest;
import com.example.authentication.dto.request.RegisterRequest;
import com.example.authentication.dto.response.ActivationCodeResponse;
import com.example.authentication.dto.response.AuthenticationResponse;
import com.example.authentication.entity.Account;
import com.example.authentication.entity.ActivationCode;
import com.example.authentication.exception.AccountNotActivatedException;
import com.example.authentication.exception.InvalidCredentialsException;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final ActivationCodeService activationCodeService;
    private final AccountService accountService;
    private final TokenService tokenService;
    private final MessageSourceService messageService;
    private final ProfileServiceClient profileServiceClient;
    private final AuthenticationManager authenticationManager;

    public ActivationCodeResponse register(RegisterRequest request) {
        if (accountService.isAccountExists(request.email())) {
            throw new EntityExistsException(
                    messageService.generateMessage("error.account.already_exists", request.email())
            );
        }

        Account newAccount = accountService.createAccount(request.email(), request.password(), false);
        log.info("account {} has been created", newAccount.getId());

        CreateProfileRequest createProfileRequest = new CreateProfileRequest(request.username(), request.email(), LocalDate.now());
        String profileId = profileServiceClient.createProfile(createProfileRequest);
        log.info("profile {} has been created", profileId);

        activationCodeService.sendNewActivationCode(newAccount);
        log.info("activation code has been sent to {}", newAccount.getEmail());

        return ActivationCodeResponse.builder()
                .message(messageService.generateMessage("activation.send.success"))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (DisabledException e) {
            throw new AccountNotActivatedException(
                    messageService.generateMessage("error.account.not_activated", request.email())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException(
                    messageService.generateMessage("error.account.credentials_invalid")
            );
        }

        Account account = accountService.findAccountByEmail(request.email());

        if (!account.isEnabled()) {
            throw new AccountNotActivatedException(
                    messageService.generateMessage("error.account.not_activated", account.getEmail())
            );
        }

        String jwt = jwtService.generateJwt(account);

        tokenService.deleteTokenByAccount(account);
        tokenService.createToken(account, jwt);
        log.info("jwt was generated {}", jwt);

        return AuthenticationResponse.builder()
                .jwt(jwt)
                .build();
    }

    public ActivationCodeResponse activate(String key) {
        ActivationCode activationCode = activationCodeService.findActivationCodeByKey(key);

        activationCodeService.checkActivationCodeExpiration(activationCode);
        accountService.enableAccount(activationCode.getAccount());
        activationCodeService.deleteActivationCodeById(activationCode.getId());

        log.info("account with email {} has been successfully activated", activationCode.getAccount().getEmail());
        return ActivationCodeResponse.builder()
                .message(messageService.generateMessage("account.activation.success"))
                .build();
    }
}

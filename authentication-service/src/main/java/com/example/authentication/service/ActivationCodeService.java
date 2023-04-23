package com.example.authentication.service;

import com.example.authentication.entity.Account;
import com.example.authentication.entity.ActivationCode;
import com.example.authentication.exception.ActivationCodeExpiredException;
import com.example.authentication.exception.ActivationCodeNotFoundException;
import com.example.authentication.repository.ActivationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationCodeService {

    private final ActivationCodeRepository activationCodeRepository;
    private final EmailService emailService;
    private final MessageSourceService messageService;

    public ActivationCode findActivationCodeByKey(String key) {
        return activationCodeRepository.findActivationCodeByKey(key)
                .orElseThrow(() -> new ActivationCodeNotFoundException(
                        messageService.generateMessage("error.activation_code.not_found", key)
                ));
    }

    public void deleteActivationCodeById(Long id) {
        activationCodeRepository.deleteById(id);
    }

    public void checkActivationCodeExpiration(ActivationCode activationCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = activationCode.getExpirationTime();
        if (expirationTime.isBefore(now)) {
            deleteActivationCodeById(activationCode.getId());
            sendNewActivationCode(activationCode.getAccount());
            long minutes = ChronoUnit.MINUTES.between(expirationTime, now);

            throw new ActivationCodeExpiredException(
                    messageService.generateMessage("error.activation_code.expired",
                            activationCode.getKey(),
                            minutes,
                            activationCode.getAccount().getEmail()
                    )
            );
        }
    }

    public void sendNewActivationCode(Account account) {
        ActivationCode activationCode = ActivationCode.builder()
                .account(account)
                .key(UUID.randomUUID().toString())
                .expirationTime(LocalDateTime.now().plusHours(2L))
                .build();

        emailService.sendActivationCode(activationCode);
        activationCodeRepository.save(activationCode);
    }
}

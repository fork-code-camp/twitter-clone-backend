package com.example.authentication.repository;

import com.example.authentication.entity.ActivationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationCodeRepository extends JpaRepository<ActivationCode, Long> {

    Optional<ActivationCode> findActivationCodeByKey(String key);

    Optional<ActivationCode> findActivationCodeByAccount_Email(String email);
}

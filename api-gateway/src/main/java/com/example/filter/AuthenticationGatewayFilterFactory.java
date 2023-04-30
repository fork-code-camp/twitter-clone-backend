package com.example.filter;

import lombok.extern.slf4j.Slf4j;
import com.example.client.AuthClient;
import com.example.exception.UnavailableServiceException;
import com.example.exception.InvalidTokenException;
import com.example.exception.MissingTokenException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final AuthClient authClient;

    public AuthenticationGatewayFilterFactory(@Lazy AuthClient authClient) {
        super(Config.class);
        this.authClient = authClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();

            headers.computeIfAbsent(HttpHeaders.AUTHORIZATION, (key) -> {
                throw new MissingTokenException(
                        "You haven't authentication token, please authenticate."
                );
            });

            String jwt = extractJwt(headers);
            validateJwt(jwt)
                    .subscribe(isJwtValid -> {
                        if (isJwtValid == null) {
                            throw new UnavailableServiceException(
                                    "Exception when calling auth-service"
                            );
                        }

                        log.info("jwt validation has done successfully: {}", isJwtValid);

                        if (!isJwtValid) {
                            throw new InvalidTokenException(
                                    "Invalid token. Please authenticate and try again."
                            );
                        }
                    });

            return chain.filter(
                    exchange
                        .mutate()
                        .request(exchange.getRequest())
                        .build()
            );
        };
    }

    public static class Config {
    }

    private Mono<Boolean> validateJwt(String jwt) {
        return Mono.fromCallable(
                        () -> authClient.validateJwt(jwt))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String extractJwt(HttpHeaders headers) {
        String jwt = headers.get(HttpHeaders.AUTHORIZATION).get(0);
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        return jwt;
    }
}

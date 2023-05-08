package com.example.filter;

import com.example.exception.MissingTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final RouteValidator validator;
    private final RestTemplate restTemplate;

    public AuthenticationGatewayFilterFactory(RouteValidator validator, RestTemplate restTemplate) {
        super(Config.class);
        this.validator = validator;
        this.restTemplate = restTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = null;
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new MissingTokenException("Missing authorization header!");
                }

                String jwt = extractJwt(exchange.getRequest().getHeaders());

                String loggedInUser = restTemplate.getForObject("http://localhost:8080//api//v1//auth//validate//" + jwt, String.class);
                request = exchange.getRequest()
                        .mutate()
                        .header("loggedInUser", loggedInUser)
                        .build();
            }
            assert request != null;
            return chain.filter(
                    exchange.mutate()
                            .request(request)
                            .build()
            );
        };
    }

    public static class Config {
    }

    private String extractJwt(HttpHeaders headers) {
        String jwt = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        return jwt;
    }
}

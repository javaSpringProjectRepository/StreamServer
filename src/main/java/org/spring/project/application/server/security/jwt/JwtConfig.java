package org.spring.project.application.server.security.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;

@Getter
@Component
public class JwtConfig {

    @Value("${jwt.config.secretKey}")
    private String secretKey;
    @Value("${jwt.config.tokenPrefix}")
    private String accessTokenPrefix;
    @Value("${jwt.config.tokenRefreshPrefix}")
    private String refreshTokenPrefix;
    @Value("${jwt.config.tokenKey}")
    private String accessTokenKey;
    @Value("${jwt.config.refreshTokenKey}")
    private String refreshTokenKey;
    @Value("${jwt.config.tokenExpiration}")
    private Integer tokenExpiration;
    @Value("${jwt.config.authoritiesKey}")
    private String authoritiesKey;

    @PostConstruct
    protected void init(){
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}

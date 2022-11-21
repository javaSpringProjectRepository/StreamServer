package org.spring.project.application.server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.model.Role;
import org.spring.project.application.server.model.User;
import org.spring.project.application.server.properties.KeyProperties;
import org.spring.project.application.server.repository.UserRepository;
import org.spring.project.application.server.security.jwt.JwtConfig;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final KeyProperties keyProperties;
    private final UtilService utilService;

    public void createTokens(HttpServletResponse response, String username, List<String> authorities) {
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecretKey());

        String accessToken = JWT.create()
                .withSubject(username)
                .withClaim(jwtConfig.getAuthoritiesKey(), authorities)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtConfig.getTokenExpiration() * 60 * 1000))
                .sign(algorithm);
        String refreshToken = JWT.create()
                .withSubject(username)
                .sign(algorithm);
        Optional<User> userFromDatabase = userRepository.findByUsername(username);
        if (userFromDatabase.isPresent()) {
            User user = userFromDatabase.get();
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
            response.setHeader(jwtConfig.getAccessTokenKey(), jwtConfig.getAccessTokenPrefix() + accessToken);
            response.setHeader(jwtConfig.getRefreshTokenKey(), jwtConfig.getRefreshTokenPrefix() + refreshToken);
        }
    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshHeader = request.getHeader(jwtConfig.getRefreshTokenKey());
        if (refreshHeader != null && refreshHeader.startsWith(jwtConfig.getRefreshTokenPrefix())) {
            try {
                String refreshToken = refreshHeader.substring(jwtConfig.getRefreshTokenPrefix().length());
                Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecretKey());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refreshToken);
                Optional<User> userFromDatabase = userRepository.findByUsername(decodedJWT.getSubject());
                if (userFromDatabase.isPresent()) {
                    User user = userFromDatabase.get();
                    try {
                        if (user.getRefreshToken().equals(refreshToken)) {
                            createTokens(response, user.getUsername(),
                                    user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
                            log.info("Пользователь {} обновил токен", user.getUsername());
                            return new ResponseEntity<>(new HttpHeaders() {{
                                add(keyProperties.getServerMessage(),
                                        utilService.sendServerErrorMessage("Токен обновлен"));
                            }}, OK);
                        } else {
                            log.error("Токен устарел");
                            return new ResponseEntity<>(new HttpHeaders() {{
                                add(keyProperties.getServerMessage(),
                                        utilService.sendServerErrorMessage("Токен устарел"));
                            }}, FORBIDDEN);
                        }
                    } catch (NullPointerException e) {
                        log.error("Токен обновления не назначен в базе");
                        return new ResponseEntity<>(new HttpHeaders() {{
                            add(keyProperties.getServerMessage(),
                                    utilService.sendServerErrorMessage("Токен обновления пользователя не назначен"));
                        }}, FORBIDDEN);
                    }
                } else {
                    log.error("Пользователь {} не найден в базе", decodedJWT.getSubject());
                    return new ResponseEntity<>(new HttpHeaders() {{
                        add(keyProperties.getServerMessage(),
                                utilService.sendServerErrorMessage("Пользователь " + decodedJWT.getSubject() +
                                " не найден"));
                    }}, NOT_FOUND);
                }
            } catch (SignatureVerificationException e) {
                log.error("Ошибка при расшифровке токена");
                return new ResponseEntity<>(new HttpHeaders() {{
                    add(keyProperties.getServerMessage(),
                            utilService.sendServerErrorMessage("Ошибка при расшифровке токена"));
                }}, FORBIDDEN);
            } catch (Exception e) {
                log.error("Ошибка при обновлении токена");
                return new ResponseEntity<>(new HttpHeaders() {{
                    add(keyProperties.getServerMessage(),
                            utilService.sendServerErrorMessage("Ошибка при обновлении токена"));
                }}, FORBIDDEN);
            }
        } else {
            log.error("Токен не найден в заголовках запроса");
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(),
                        utilService.sendServerErrorMessage("Токен не найден в заголовках запроса"));
            }}, FORBIDDEN);
        }
    }
}

package org.spring.project.application.server.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.properties.KeyProperties;
import org.spring.project.application.server.service.UtilService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final UtilService utilService;
    private final KeyProperties keyProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Запрос: {}", request.getRequestURI());
        String authorizationToken = request.getHeader(AUTHORIZATION);
        if (authorizationToken != null && authorizationToken.startsWith(jwtConfig.getAccessTokenPrefix())) {
            try {
                String token = authorizationToken.substring(jwtConfig.getAccessTokenPrefix().length());
                Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecretKey());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                String username = decodedJWT.getSubject();
                List<String> roles = decodedJWT.getClaim(jwtConfig.getAuthoritiesKey()).asList(String.class);
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
            } catch (TokenExpiredException e) {
                log.error("Время действия токена пользователя {} истекло",
                        JWT.decode(authorizationToken.substring(jwtConfig.getAccessTokenPrefix().length()))
                                .getSubject());
                response.setStatus(NOT_ACCEPTABLE.value());
                response.setHeader(keyProperties.getServerMessage(),
                        utilService.sendServerErrorMessage("Время действия токена истекло"));
            } catch (Exception e) {
                log.error("Ошибка в 'CustomAuthorizationFilter'");
                response.setStatus(FORBIDDEN.value());
                response.setHeader(keyProperties.getServerMessage(),
                        utilService.sendServerErrorMessage("Ошибка при авторизации"));
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
package org.spring.project.application.server.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.service.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            AuthenticationDto authenticationDto =
                    new ObjectMapper().readValue(request.getInputStream(), AuthenticationDto.class);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    authenticationDto.getUsername(),
                    authenticationDto.getPassword()
            );
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            throw new IllegalArgumentException("Ошибка в 'CustomAuthenticationFilter' при аутентификации пользователя");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) {
        tokenService.createTokens(response, authResult.getName(),
                authResult.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }

}

@Data
class AuthenticationDto {

    private String username;
    private String password;

}
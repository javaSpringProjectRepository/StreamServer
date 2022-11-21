package org.spring.project.application.server.security;

import lombok.RequiredArgsConstructor;
import org.spring.project.application.server.properties.KeyProperties;
import org.spring.project.application.server.security.jwt.CustomAuthenticationFilter;
import org.spring.project.application.server.security.jwt.CustomAuthorizationFilter;
import org.spring.project.application.server.security.jwt.JwtConfig;
import org.spring.project.application.server.service.TokenService;
import org.spring.project.application.server.service.UtilService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final TokenService tokenService;
    private final UtilService utilService;
    private final KeyProperties keyProperties;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        CustomAuthenticationFilter customAuthenticationFilter =
                new CustomAuthenticationFilter(authenticationManagerBean(), tokenService, utilService, keyProperties);
        customAuthenticationFilter.setFilterProcessesUrl("/authentication");

        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests().anyRequest().permitAll()
                .and()
                .addFilter(customAuthenticationFilter)
                .addFilterBefore(
                        new CustomAuthorizationFilter(jwtConfig, utilService, keyProperties),
                        UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

package org.spring.project.application.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.dto.RegistrationDto;
import org.spring.project.application.server.model.User;
import org.spring.project.application.server.repository.RoleRepository;
import org.spring.project.application.server.repository.StatusRepository;
import org.spring.project.application.server.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final StatusRepository statusRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.error("Пользователь {} не найден", username);
            throw new UsernameNotFoundException("Пользователь не найден");
        } else {
            User userFromDatabase = user.get();
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            userFromDatabase.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));

            return new org.springframework.security.core.userdetails.User(
                    userFromDatabase.getUsername(),
                    userFromDatabase.getPassword(), authorities);
        }
    }

    public void saveInDatabase(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(roleRepository.findByName("USER").orElse(null)));
        user.setStates(Collections.singleton(statusRepository.findByName("ACTIVE").orElse(null)));
        userRepository.save(user);
        log.info("Пользователь {} создан", user.getUsername());
    }

    public ResponseEntity<?> saveUser(RegistrationDto registrationDto, BindingResult bindingResult) {
        List<String> errorList = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            log.error("Ошибка валидации пользователя {}", registrationDto.getUsername());
            bindingResult.getAllErrors().forEach(error -> errorList.add(error.getDefaultMessage()));
        }
        Optional<User> checkUsernameExist = userRepository.findByUsername(registrationDto.getUsername());
        if (checkUsernameExist.isPresent()) {
            log.error("Имя пользователя {} уже существует", registrationDto.getUsername());
            errorList.add(String.format("Пользователь с именем %s уже существует", registrationDto.getUsername()));
        }
        if (!errorList.isEmpty()) {
            return new ResponseEntity<>(errorList, BAD_REQUEST);
        }
        User user = new User(
                registrationDto.getUsername(),
                registrationDto.getPassword(),
                registrationDto.getEmail(),
                new BigDecimal(0));
        saveInDatabase(user);
        return new ResponseEntity<>(CREATED);
    }
}

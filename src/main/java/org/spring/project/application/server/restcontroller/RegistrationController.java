package org.spring.project.application.server.restcontroller;

import lombok.RequiredArgsConstructor;
import org.spring.project.application.server.dto.RegistrationDto;
import org.spring.project.application.server.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> registration(@RequestBody @Valid RegistrationDto registrationDto,
                                          BindingResult bindingResult){
        return userService.saveUser(registrationDto, bindingResult);
    }
}

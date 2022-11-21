package org.spring.project.application.server.restcontroller;

import lombok.RequiredArgsConstructor;
import org.spring.project.application.server.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final GameService gameService;

    @GetMapping("/library")
    public ResponseEntity<?> getUserLibrary(Authentication authentication) {
        return gameService.getUserLibrary(authentication);
    }
}

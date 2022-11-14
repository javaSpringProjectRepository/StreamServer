package org.spring.project.application.server.restcontroller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final GameService gameService;

    @GetMapping("/start")
    public ResponseEntity<?> applicationStart(){
        return new ResponseEntity<>(OK);
    }

    @GetMapping("/library")
    public ResponseEntity<?> getUserLibrary(Authentication authentication) {
        return gameService.getUserLibrary(authentication);
    }
}

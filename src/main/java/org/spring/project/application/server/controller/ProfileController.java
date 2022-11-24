package org.spring.project.application.server.controller;

import lombok.RequiredArgsConstructor;
import org.spring.project.application.server.model.User;
import org.spring.project.application.server.properties.FolderProperties;
import org.spring.project.application.server.properties.KeyProperties;
import org.spring.project.application.server.properties.ResourceProperties;
import org.spring.project.application.server.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final FolderProperties folderProperties;
    private final KeyProperties keyProperties;
    private final ResourceProperties resourceProperties;

    @GetMapping()
    public String getProfilePage(Authentication authentication, Model model) {
        Optional<User> user = userRepository.findByUsername(authentication.getName());
        model.addAttribute(keyProperties.getStyleFolder(),
                folderProperties.getStyleFolder() + resourceProperties.getProfilePageStyle());
        model.addAttribute(keyProperties.getResourcesFolder(), folderProperties.getResourcesHtmlFolder());
        model.addAttribute(keyProperties.getUser(), user);
        return "profile_page";
    }
}

package org.spring.project.application.server.controller;

import lombok.RequiredArgsConstructor;
import org.spring.project.application.server.model.Game;
import org.spring.project.application.server.model.User;
import org.spring.project.application.server.properties.FolderProperties;
import org.spring.project.application.server.properties.KeyProperties;
import org.spring.project.application.server.properties.ResourceProperties;
import org.spring.project.application.server.repository.GameRepository;
import org.spring.project.application.server.repository.UserRepository;
import org.spring.project.application.server.service.GameService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final FolderProperties folderProperties;
    private final KeyProperties keyProperties;
    private final ResourceProperties resourceProperties;
    private final GameService gameService;

    @GetMapping()
    public String storePage(Model model) {
        List<Game> games = gameRepository.findAll().stream()
                .sorted(Comparator.comparing(Game::getTitle)).collect(Collectors.toList());
        model.addAttribute(keyProperties.getStyleFolder(),
                folderProperties.getStyleFolder() + resourceProperties.getStorePageStyle());
        model.addAttribute(keyProperties.getResourcesFolder(), folderProperties.getResourcesHtmlFolder());
        model.addAttribute(keyProperties.getGames(), games);
        return "store_page";
    }

    @GetMapping("/games/{gameName}")
    public String gamePage(@PathVariable String gameName, Authentication authentication, Model model) {
        Optional<Game> gameFromDatabase = gameRepository.findByName(gameName);
        boolean userHasGame = false;
        if (authentication != null) {
            Optional<User> userFromDatabase = userRepository.findByUsername(authentication.getName());
            if (userFromDatabase.isPresent() && gameFromDatabase.isPresent() &&
                    userFromDatabase.get().getGames().contains(gameFromDatabase.get())) {
                userHasGame = true;
            }
        }
        List<String> errors = new ArrayList<>();
        if (gameFromDatabase.isEmpty()) {
            errors.add("Игра не найдена");
        }
        model.addAttribute(keyProperties.getStyleFolder(),
                folderProperties.getStyleFolder() + resourceProperties.getGamePageStyle());
        model.addAttribute(keyProperties.getUserHasGame(), userHasGame);
        model.addAttribute(keyProperties.getGame(), gameFromDatabase);
        model.addAttribute(keyProperties.getErrors(), errors);
        return "game_page";
    }

    @GetMapping("/games/category/{gameCategory}")
    public String gameCategory(@PathVariable String gameCategory, Model model) {
        List<Game> games = gameRepository.findAllByGameCategory_Name(gameCategory).stream().collect(Collectors.toList());
        model.addAttribute(keyProperties.getStyleFolder(),
                folderProperties.getStyleFolder() + resourceProperties.getGamesByCategoryPageStyle());
        model.addAttribute(keyProperties.getGames(), games);
        return "games_by_category";
    }

    @PostMapping("{gameName}/buy")
    public String buyGame(@PathVariable String gameName,
                          Authentication authentication,
                          HttpServletResponse response,
                          Model model) {
        model.addAttribute(keyProperties.getStatus(), gameService.buyGame(gameName, authentication, response));
        model.addAttribute(keyProperties.getStyleFolder(),
                folderProperties.getStyleFolder() + resourceProperties.getSuccessBuyingPageStyle());
        return "success_buying";
    }
}

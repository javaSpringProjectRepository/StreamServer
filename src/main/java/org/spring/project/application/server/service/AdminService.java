package org.spring.project.application.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.model.*;
import org.spring.project.application.server.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.OK;

@Service
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StatusRepository statusRepository;
    private final GameRepository gameRepository;
    private final GameUpdateNewsRepository gameUpdateNewsRepository;
    private final GameCategoryRepository gameCategoryRepository;

    public void saveGameInDatabase(Game game) {
        gameRepository.save(game);
        log.info("Игра {} добавлена в базу", game.getName());
    }

    public ResponseEntity<?> saveRole(Role role) {
        if (!role.getName().matches("[A-Z]{" + role.getName().length() + "}")) {
            log.error("Название роли введено неверно: {}", role.getName());
            return new ResponseEntity<>("Название роли должно содержать только заглавные буквы", NOT_ACCEPTABLE);
        }
        Optional<Role> checkRoleExist = roleRepository.findByName(role.getName());
        if (checkRoleExist.isPresent()) {
            log.error("Роль {} уже существует", role.getName());
            return new ResponseEntity<>(String.format("Роль %s уже существует", role.getName()), NOT_ACCEPTABLE);
        }
        roleRepository.save(role);
        log.info("Роль {} создана", role.getName());
        return new ResponseEntity<>(String.format("Роль %s создана", role.getName()), CREATED);
    }

    public ResponseEntity<?> saveState(State state) {
        if (!state.getName().matches("[A-Z]{" + state.getName().length() + "}")) {
            log.error("Название статуса введено неверно");
            return new ResponseEntity<>("Название статуса должно содержать только заглавные буквы",
                    NOT_ACCEPTABLE);
        }
        Optional<State> checkStateExist = statusRepository.findByName(state.getName());
        if (checkStateExist.isPresent()) {
            log.error("Статус {} уже существует", state.getName());
            return new ResponseEntity<>(String.format("Статус %s уже существует", state.getName()), NOT_ACCEPTABLE);
        }
        statusRepository.save(state);
        log.info("Статус {} создан", state.getName());
        return new ResponseEntity<>(String.format("Статус %s создан", state.getName()), CREATED);
    }

    public ResponseEntity<?> saveGameCategory(GameCategory gameCategory) {
        if (!gameCategory.getName().matches("[A-Z]{" + gameCategory.getName().length() + "}")) {
            log.error("Название категории введено неверно");
            return new ResponseEntity<>("Название категории должно содержать только заглавные буквы",
                    NOT_ACCEPTABLE);
        }
        Optional<GameCategory> checkCategoryExist = gameCategoryRepository.findByName(gameCategory.getName());
        if (checkCategoryExist.isPresent()) {
            log.error("Категория {} уже существует", gameCategory.getName());
            return new ResponseEntity<>(String.format("Категория %s уже существует",
                    gameCategory.getName()), NOT_ACCEPTABLE);
        }
        gameCategoryRepository.save(gameCategory);
        log.info("Категория {} создана", gameCategory.getName());
        return new ResponseEntity<>(String.format("Категория %s создана", gameCategory.getName()), CREATED);
    }

    public ResponseEntity<?> addRoleToUser(String username, String roleName) {
        Optional<User> userFromDatabase = userRepository.findByUsername(username);
        Optional<Role> roleFromDatabase = roleRepository.findByName(roleName);
        if (userFromDatabase.isEmpty()) {
            log.error("Пользователь {} не найден", username);
            return new ResponseEntity<>(String.format("Пользователь с именем %s не найден", username), NOT_FOUND);
        }
        if (roleFromDatabase.isEmpty()) {
            log.error("Роль {} не найдена", roleName);
            return new ResponseEntity<>(String.format("Роль %s не найдена", roleName), NOT_FOUND);
        }
        User user = userFromDatabase.get();
        Role role = roleFromDatabase.get();
        if (user.getRoles().stream().anyMatch(userRole -> userRole.getName().equals(role.getName()))) {
            log.error("У пользователя {} уже есть роль {}",
                    user.getUsername(), role.getName());
            return new ResponseEntity<>(String.format("У пользователя %s уже есть роль %s",
                    user.getUsername(), role.getName()), NOT_ACCEPTABLE);
        }
        try {
            user.getRoles().add(role);
        } catch (NullPointerException e) {
            user.setRoles(new HashSet<>(Collections.singletonList(role)));
        }
        userRepository.save(user);
        log.info("Пользователю {} добавлена роль {}", user.getUsername(), role.getName());
        return new ResponseEntity<>(
                String.format("Пользователю %s добавлена роль %s", user.getUsername(), role.getName()), OK);
    }

    public ResponseEntity<?> addStateToUser(String username, String statusName) {
        Optional<User> userFromDatabase = userRepository.findByUsername(username);
        Optional<State> stateFromDatabase = statusRepository.findByName(statusName);
        if (userFromDatabase.isEmpty()) {
            log.error("Имя {} не найдено", username);
            return new ResponseEntity<>(String.format("Пользователь с именем %s не найден", username), NOT_FOUND);
        }
        if (stateFromDatabase.isEmpty()) {
            log.error("Статус {} не найден", statusName);
            return new ResponseEntity<>(String.format("Статус %s не найден", statusName), NOT_FOUND);
        }
        User user = userFromDatabase.get();
        State state = stateFromDatabase.get();
        if (user.getStates().stream().anyMatch(userState -> userState.getName().equals(state.getName()))) {
            log.error("У пользователя {} уже есть статус {}", user.getUsername(), state.getName());
            return new ResponseEntity<>(String.format("У пользователя %s уже есть статус %s",
                    user.getUsername(), state.getName()), NOT_ACCEPTABLE);
        }
        try {
            user.getStates().add(state);
        } catch (NullPointerException e) {
            user.setStates(new HashSet<>(Collections.singletonList(state)));
        }
        userRepository.save(user);
        log.info("Пользователю {} добавлен статус {}", user.getUsername(), state.getName());
        return new ResponseEntity<>(
                String.format("Пользователю %s добавлен статус %s", user.getUsername(), state.getName()), OK);
    }

    public ResponseEntity<?> addGameCategoryToGame(String gameName, String categoryName) {
        Optional<Game> gameFromDatabase = gameRepository.findByName(gameName);
        Optional<GameCategory> categoryFromDatabase = gameCategoryRepository.findByName(categoryName);
        if (gameFromDatabase.isEmpty()) {
            log.error("Игра {} не найдена в базе", gameName);
            return new ResponseEntity<>(String.format("Пользователь с именем %s не найден", gameName), NOT_FOUND);
        }
        if (categoryFromDatabase.isEmpty()) {
            log.error("Категория {} не найдена в базе", categoryName);
            return new ResponseEntity<>(String.format("Категория %s не найдена в базе", categoryName), NOT_FOUND);
        }
        Game game = gameFromDatabase.get();
        GameCategory category = categoryFromDatabase.get();
        game.setGameCategory(category);
        gameRepository.save(game);
        log.info("Игре {} присвоена категория {}", game.getName(), category.getName());
        return new ResponseEntity<>(
                String.format("Игре %s присвоена категория %s", game.getName(), category.getName()), OK);
    }

    public ResponseEntity<?> addUpdateNewsToGame(String gameName, String updateText, String updateImagePath) {
        Game game = gameRepository.findByName(gameName).orElse(null);
        if (game == null) {
            log.error("Игра {} не найдена в базе", gameName);
            return new ResponseEntity<>(String.format("Игра %s не найдена в базе", gameName), NOT_FOUND);
        }
        GameUpdateNews gameUpdateNews = new GameUpdateNews(updateText, updateImagePath, LocalDateTime.now());
        gameUpdateNewsRepository.save(gameUpdateNews);
        try {
            game.getGameUpdateNews().add(gameUpdateNews);
        } catch (NullPointerException e) {
            game.setGameUpdateNews(new HashSet<>(Collections.singletonList(gameUpdateNews)));
        }
        gameRepository.save(game);
        log.info("Игре {} добавлены новости обновления", game.getTitle());
        return new ResponseEntity<>(String.format("Игре %s добавлены новости обновления", game.getTitle()), OK);
    }
}

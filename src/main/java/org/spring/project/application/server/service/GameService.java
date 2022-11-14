package org.spring.project.application.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.spring.project.application.server.dto.GameFileDto;
import org.spring.project.application.server.dto.GameLibraryDto;
import org.spring.project.application.server.model.Game;
import org.spring.project.application.server.model.GameUpdateNews;
import org.spring.project.application.server.model.User;
import org.spring.project.application.server.properties.FolderProperties;
import org.spring.project.application.server.properties.KeyProperties;
import org.spring.project.application.server.properties.ResourceProperties;
import org.spring.project.application.server.repository.GameRepository;
import org.spring.project.application.server.repository.UserRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('USER')")
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final FolderProperties folderProperties;
    private final ResourceProperties resourceProperties;
    private final KeyProperties keyProperties;

    //    Метод для использования сервером (без ResponseEntity)
    public void saveGameToUser(String username, String gameName) {
        Optional<User> userFromDatabase = userRepository.findByUsername(username);
        Optional<Game> gameFromDatabase = gameRepository.findByName(gameName);
        if (userFromDatabase.isEmpty()) {
            log.error("Пользователь {} не найден в базе", username);
            return;
        }
        if (gameFromDatabase.isEmpty()) {
            log.error("Игра {} не найдена в базе", gameName);
            return;
        }
        User user = userFromDatabase.get();
        Game game = gameFromDatabase.get();
        if (user.getGames().stream().anyMatch(userGame -> userGame.getName().equals(gameName))) {
            log.error("У пользователя {} уже есть игра {}",
                    user.getUsername(), game.getName());
            return;
        }
        user.getGames().add(game);
        userRepository.save(user);
        log.info("Пользователю {} добавлена игра {}", user.getUsername(), game.getName());
    }

    public HttpStatus buyGame(String gameName, Authentication authentication, HttpServletResponse response) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        Game game = gameRepository.findByName(gameName).orElse(null);
        if (user == null) {
            log.error("Пользователь {} не найден в базе", authentication.getName());
            response.setHeader(keyProperties.getServerMessage(), "Пользователь " + authentication.getName() +
                    " не найден в базе");
            response.setStatus(NOT_FOUND.value());
            return NOT_FOUND;
        }
        if (game == null) {
            log.error("Игра {} не найдена в базе", gameName);
            response.setHeader(keyProperties.getServerMessage(), "Игра " + gameName + " не найдена в базе");
            response.setStatus(NOT_FOUND.value());
            return NOT_FOUND;
        }
        if (user.getGames().stream().anyMatch(userGame -> userGame.getName().equals(game.getName()))) {
            log.error("У пользователя {} уже есть игра {}", user.getUsername(), game.getTitle());
            response.setHeader(keyProperties.getServerMessage(), "У пользователя " + user.getUsername() +
                    " уже есть игра " + game.getTitle());
            response.setStatus(FORBIDDEN.value());
            return FORBIDDEN;
        }
        if (user.getBudget().compareTo(game.getPrice()) < 0) {
            log.error("Недостаточно средств");
            response.setHeader(keyProperties.getServerMessage(), "Недостаточно средств");
            response.setStatus(FORBIDDEN.value());
            return FORBIDDEN;
        }
        user.setBudget(user.getBudget().subtract(game.getPrice()));
        user.getGames().add(game);
        userRepository.save(user);
        log.info("Игра {} добавлена в библиотеку игрока {}", game.getName(), user.getUsername());
        response.setHeader(keyProperties.getServerMessage(), "Игра " + game.getName() + " добавлена в библиотеку");
        response.setStatus(OK.value());
        return OK;
    }

    public ResponseEntity<?> getUserLibrary(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            log.error("Пользователь {} не найден в базе", authentication.getName());
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(), "Пользователь " + authentication.getName() +
                        " не найден в базе");
            }}, NOT_FOUND);
        }
        List<GameLibraryDto> games = user.getGames().stream().map(game -> {
            File gameDirectory = new File(
                    folderProperties.getGamesFolder() +
                            game.getName());
            long size = 0;
            if (gameDirectory.exists()) {
                size = FileUtils.sizeOfDirectory(new File(
                        folderProperties.getGamesFolder() +
                                game.getName()));
            }
            return new GameLibraryDto(
                    game.getName(),
                    game.getTitle(),
                    game.getGameStartFileName(),
                    size,
                    game.getGameUpdateNews());
        })
                .collect(Collectors.toList());
        return new ResponseEntity<>(games, OK);
    }

    private Optional<Game> userGameCheck(String gameName, String userName, HttpServletResponse response) {
        User user = userRepository.findByUsername(userName).orElse(null);
        Game game = gameRepository.findByName(gameName).orElse(null);
        if (user == null) {
            log.error("Пользователь {} не найден в базе", userName);
            response.setHeader(keyProperties.getServerMessage(), "Пользователь " + userName +
                    " не найден в базе");
            response.setStatus(NOT_FOUND.value());
            return Optional.empty();
        }
        if (game == null) {
            log.error("Игра {} не найдена в базе", gameName);
            response.setHeader(keyProperties.getServerMessage(), "Игра " + gameName + " не найдена в базе");
            response.setStatus(NOT_FOUND.value());
            return Optional.empty();
        }
        if (user.getGames().stream().anyMatch(userGame -> userGame.getName().equals(game.getName()))) {
            return Optional.of(game);
        } else {
            log.error("Игра {} не найдена в библиотеке пользователя {}", game.getTitle(), user.getUsername());
            response.setHeader(keyProperties.getServerMessage(), "Игра " + game.getTitle() +
                    " не найдена в вашей библиотеке");
            response.setStatus(NOT_FOUND.value());
            return Optional.empty();
        }
    }

    public void downloadGameNewsPreviewImage(String gameName, Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            File file = new File(
                    folderProperties.getResourcesFolder() +
                            game.getName() +
                            File.separator +
                            resourceProperties.getNewsPreviewImage() +
                            File.separator +
                            resourceProperties.getNewsPreviewImage() + resourceProperties.getImageFormat());
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                log.error("Не удалось отправить картинку новостей игры {} пользователю {}",
                        game.getTitle(), authentication.getName());
                response.setHeader(keyProperties.getServerMessage(), "Серверу не удалось отправить картинку новостей");
                response.setStatus(FORBIDDEN.value());
            }
        }
    }

    public void downloadLibraryMainPageImage(String gameName, Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            File file = new File(
                    folderProperties.getResourcesFolder() +
                            game.getName() +
                            File.separator +
                            resourceProperties.getLibraryMainImage() +
                            File.separator +
                            resourceProperties.getLibraryMainImage() + resourceProperties.getImageFormat());
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                log.error("Не удалось отправить картинку главной страницы библиотеки игры {} пользователю {}",
                        game.getTitle(), authentication.getName());
                response.setHeader(keyProperties.getServerMessage(),
                        "Серверу не удалось отправить картинку главной страницы библиотеки");
                response.setStatus(FORBIDDEN.value());
            }
        }
    }

    public ResponseEntity<?> getGameFilesList(String gameName, Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            String gameDirectoryPath = folderProperties.getGamesFolder() + game.getName();
            File gameDirectory = new File(gameDirectoryPath);
            Collection<File> gameFilesList = FileUtils.listFiles(
                    gameDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            List<String> gameFilesInfo = new ArrayList<>();
            gameFilesList.forEach(file -> gameFilesInfo.add(file.getAbsolutePath().substring(gameDirectoryPath.length())));
            return new ResponseEntity<>(gameFilesInfo, new HttpHeaders() {{
                add(keyProperties.getGameSize(), String.valueOf(FileUtils.sizeOfDirectory(gameDirectory)));
            }}, OK);
        } else {
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(), response.getHeader(keyProperties.getServerMessage()));
            }}, HttpStatus.valueOf(response.getStatus()));
        }
    }

    public ResponseEntity<?> getGameUpdateNews(String gameName, Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            return new ResponseEntity<>(game.getGameUpdateNews(), OK);
        } else {
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(), response.getHeader(keyProperties.getServerMessage()));
            }}, HttpStatus.valueOf(response.getStatus()));
        }
    }

    public void downloadUpdateImage(String gameName, GameUpdateNews gameUpdateNews,
                                    Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            File file = new File(
                    folderProperties.getResourcesFolder() +
                            game.getName() +
                            File.separator +
                            resourceProperties.getUpdateImage() +
                            File.separator +
                            gameUpdateNews.getUpdateImage());
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                log.error("Не удалось отправить картинку новостей обновлений игры {} пользователю {}",
                        game.getTitle(), authentication.getName());
                response.setHeader(keyProperties.getServerMessage(),
                        "Серверу не удалось отправить картинку новостей обновлений");
                response.setStatus(FORBIDDEN.value());
            }
        }
    }

    public void downloadGameLibraryLogo(String gameName, Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {

            File file = new File(
                    folderProperties.getResourcesFolder() +
                            game.getName() +
                            File.separator +
                            resourceProperties.getLibraryLogo() +
                            File.separator +
                            resourceProperties.getLibraryLogo() + resourceProperties.getImageFormat());
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                log.error("Не удалось отправить лого игры {} пользователю {}", game.getTitle(), authentication.getName());
                response.setHeader(keyProperties.getServerMessage(), "Серверу не удалось отправить лого игры");
                response.setStatus(FORBIDDEN.value());
            }
        }
    }

    public void downloadGameFile(String gameName, GameFileDto gameFileDto,
                                 Authentication authentication,
                                 HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            File file = new File(
                    folderProperties.getGamesFolder() +
                            game.getName() +
                            gameFileDto.getFilePath());
            if (file.length() == gameFileDto.getFileLength()) {
                response.setHeader(keyProperties.getServerMessage(), "Файл " + gameFileDto.getFilePath() + " игры " +
                        game.getTitle() + " уже загружен");
                response.setStatus(FOUND.value());
            } else if (file.length() > gameFileDto.getFileLength()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    in.skip(gameFileDto.getFileLength());
                    IOUtils.copy(in, response.getOutputStream());
                    response.flushBuffer();
                } catch (IOException e) {
                    log.error("Не удалось отправить файл {} игры {} пользователю {}",
                            gameFileDto.getFilePath(), game.getTitle(), authentication.getName());
                    response.setHeader(keyProperties.getServerMessage(), "Не удалось отправить файл " +
                            gameFileDto.getFilePath() + " игры " + game.getTitle());
                    response.setStatus(SERVICE_UNAVAILABLE.value());
                }
            } else {
                response.setStatus(FORBIDDEN.value());
            }
        }

    }

    public ResponseEntity<?> getActualGameSize(String gameName, Authentication authentication, HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            File gameDirectory = new File(folderProperties.getGamesFolder() + game.getName());
            long sizeOfDirectory = 0;
            if (gameDirectory.exists()) {
                sizeOfDirectory = FileUtils.sizeOfDirectory(gameDirectory);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add(keyProperties.getGameSize(), String.valueOf(sizeOfDirectory));
            return new ResponseEntity<>(headers, OK);
        } else {
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(), response.getHeader(keyProperties.getServerMessage()));
            }}, HttpStatus.valueOf(response.getStatus()));
        }
    }
}

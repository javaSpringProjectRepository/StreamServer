package org.spring.project.application.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    private final UtilService utilService;

    //    ?????????? ?????? ?????????????????????????? ???????????????? (?????? ResponseEntity)
    public void saveGameToUser(String username, String gameName) {
        Optional<User> userFromDatabase = userRepository.findByUsername(username);
        Optional<Game> gameFromDatabase = gameRepository.findByName(gameName);
        if (userFromDatabase.isEmpty()) {
            log.error("???????????????????????? {} ???? ???????????? ?? ????????", username);
            return;
        }
        if (gameFromDatabase.isEmpty()) {
            log.error("???????? {} ???? ?????????????? ?? ????????", gameName);
            return;
        }
        User user = userFromDatabase.get();
        Game game = gameFromDatabase.get();
        if (user.getGames().stream().anyMatch(userGame -> userGame.getName().equals(gameName))) {
            log.error("?? ???????????????????????? {} ?????? ???????? ???????? {}",
                    user.getUsername(), game.getName());
            return;
        }
        user.getGames().add(game);
        userRepository.save(user);
        log.info("???????????????????????? {} ?????????????????? ???????? {}", user.getUsername(), game.getName());
    }

    public int buyGame(String gameName, Authentication authentication, HttpServletResponse response) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        Game game = gameRepository.findByName(gameName).orElse(null);
        if (user == null) {
            log.error("???????????????????????? {} ???? ???????????? ?? ????????", authentication.getName());
            response.setStatus(NOT_FOUND.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("???????????????????????? " + authentication.getName() + " ???? ???????????? ?? ????????"));
            return NOT_FOUND.value();
        }
        if (game == null) {
            log.error("???????? {} ???? ?????????????? ?? ????????", gameName);
            response.setStatus(NOT_FOUND.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("???????? " + gameName + " ???? ?????????????? ?? ????????"));
            return NOT_FOUND.value();
        }
        if (user.getGames().stream().anyMatch(userGame -> userGame.getName().equals(game.getName()))) {
            log.error("?? ???????????????????????? {} ?????? ???????? ???????? {}", user.getUsername(), game.getTitle());
            response.setStatus(FORBIDDEN.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("?? ???????????????????????? " + user.getUsername() +
                            " ?????? ???????? ???????? "));
            return FORBIDDEN.value();
        }
        if (user.getBudget().compareTo(game.getPrice()) < 0) {
            log.error("???????????????????????? ??????????????");
            response.setStatus(FORBIDDEN.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("???????????????????????? ??????????????"));
            return FORBIDDEN.value();
        }
        user.setBudget(user.getBudget().subtract(game.getPrice()));
        user.getGames().add(game);
        userRepository.save(user);
        log.info("???????? {} ?????????????????? ?? ???????????????????? ???????????? {}", game.getName(), user.getUsername());
        response.setStatus(OK.value());
        response.setHeader(keyProperties.getServerMessage(),
                utilService.sendServerErrorMessage("???????? " + game.getName() + " ?????????????????? ?? ????????????????????"));
        return OK.value();
    }

    public ResponseEntity<?> getUserLibrary(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            log.error("???????????????????????? {} ???? ???????????? ?? ????????", authentication.getName());
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(), utilService.sendServerErrorMessage(
                        "???????????????????????? " + authentication.getName() + " ???? ???????????? ?? ????????"));
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
            log.error("???????????????????????? {} ???? ???????????? ?? ????????", userName);
            response.setStatus(NOT_FOUND.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("???????????????????????? " + userName + " ???? ???????????? ?? ????????"));
            return Optional.empty();
        }
        if (game == null) {
            log.error("???????? {} ???? ?????????????? ?? ????????", gameName);
            response.setStatus(NOT_FOUND.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("???????? " + gameName + " ???? ?????????????? ?? ????????"));
            return Optional.empty();
        }
        if (user.getGames().stream().anyMatch(userGame -> userGame.getName().equals(game.getName()))) {
            return Optional.of(game);
        } else {
            log.error("???????? {} ???? ?????????????? ?? ???????????????????? ???????????????????????? {}", game.getTitle(), user.getUsername());
            response.setStatus(NOT_FOUND.value());
            response.setHeader(keyProperties.getServerMessage(),
                    utilService.sendServerErrorMessage("???????? " + game.getTitle() + " ???? ?????????????? ?? ?????????? ????????????????????"));
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
                log.error("???? ?????????????? ?????????????????? ???????????????? ???????????????? ???????? {} ???????????????????????? {}",
                        game.getTitle(), authentication.getName());
                response.setStatus(FORBIDDEN.value());
                response.setHeader(keyProperties.getServerMessage(),
                        utilService.sendServerErrorMessage("?????????????? ???? ?????????????? ?????????????????? ???????????????? ????????????????"));
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
                log.error("???? ?????????????? ?????????????????? ???????????????? ?????????????? ???????????????? ???????????????????? ???????? {} ???????????????????????? {}",
                        game.getTitle(), authentication.getName());
                response.setStatus(FORBIDDEN.value());
                response.setHeader(keyProperties.getServerMessage(), utilService.sendServerErrorMessage(
                        "?????????????? ???? ?????????????? ?????????????????? ???????????????? ?????????????? ???????????????? ????????????????????"));
            }
        }
    }

    public ResponseEntity<?> getGameFilesList(String gameName, Authentication authentication,
                                              HttpServletResponse response) {
        Game game = userGameCheck(gameName, authentication.getName(), response).orElse(null);
        if (game != null) {
            String gameDirectoryPath = folderProperties.getGamesFolder() + game.getName();
            File gameDirectory = new File(gameDirectoryPath);
            Collection<File> gameFilesList = FileUtils.listFiles(
                    gameDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            List<String> gameFilesInfo = new ArrayList<>();
            gameFilesList.forEach(file ->
                    gameFilesInfo.add(file.getAbsolutePath().substring(gameDirectoryPath.length())));
            return new ResponseEntity<>(gameFilesInfo, new HttpHeaders() {{
                add(keyProperties.getGameSize(), String.valueOf(FileUtils.sizeOfDirectory(gameDirectory)));
            }}, OK);
        } else {
            return new ResponseEntity<>(new HttpHeaders() {{
                add(keyProperties.getServerMessage(), response.getHeader(keyProperties.getServerMessage()));
            }}, HttpStatus.valueOf(response.getStatus()));
        }
    }

    public ResponseEntity<?> getGameUpdateNews(String gameName, Authentication authentication,
                                               HttpServletResponse response) {
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
                log.error("???? ?????????????? ?????????????????? ???????????????? ???????????????? ???????????????????? ???????? {} ???????????????????????? {}",
                        game.getTitle(), authentication.getName());
                response.setStatus(FORBIDDEN.value());
                response.setHeader(keyProperties.getServerMessage(),
                        utilService.sendServerErrorMessage("?????????????? ???? ?????????????? ?????????????????? ???????????????? ???????????????? ????????????????????"));
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
                log.error("???? ?????????????? ?????????????????? ???????? ???????? {} ???????????????????????? {}", game.getTitle(), authentication.getName());
                response.setStatus(FORBIDDEN.value());
                response.setHeader(keyProperties.getServerMessage(),
                        utilService.sendServerErrorMessage("?????????????? ???? ?????????????? ?????????????????? ?????????????? ????????"));
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
                response.setStatus(FOUND.value());
                response.setHeader(keyProperties.getServerMessage(), utilService.sendServerErrorMessage(
                        "???????? " + gameFileDto.getFilePath() + " ???????? " + game.getTitle() + " ?????? ????????????????"));
            } else if (file.length() > gameFileDto.getFileLength()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    in.skip(gameFileDto.getFileLength());
                    IOUtils.copy(in, response.getOutputStream());
                    response.flushBuffer();
                } catch (IOException e) {
                    log.error("???? ?????????????? ?????????????????? ???????? {} ???????? {} ???????????????????????? {}",
                            gameFileDto.getFilePath(), game.getTitle(), authentication.getName());
                    response.setStatus(SERVICE_UNAVAILABLE.value());
                    response.setHeader(keyProperties.getServerMessage(), utilService.sendServerErrorMessage(
                            "???? ?????????????? ?????????????????? ???????? " + gameFileDto.getFilePath() + " ???????? " + game.getTitle()));
                }
            } else {
                response.setStatus(FORBIDDEN.value());
            }
        }

    }

    public ResponseEntity<?> getActualGameSize(String gameName, Authentication authentication,
                                               HttpServletResponse response) {
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

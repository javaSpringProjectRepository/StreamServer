package org.spring.project.application.server.restcontroller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spring.project.application.server.dto.GameFileDto;
import org.spring.project.application.server.model.GameUpdateNews;
import org.spring.project.application.server.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
@RequestMapping("/games")
@RequiredArgsConstructor
public class LibraryController {

    private final GameService gameService;

    @GetMapping("{gameName}/news_preview")
    public void getGameNewsPreviewImage(@PathVariable String gameName,
                                        Authentication authentication,
                                        HttpServletResponse response) {
        gameService.downloadGameNewsPreviewImage(gameName, authentication, response);
    }

    @GetMapping("{gameName}/actual_size")
    public ResponseEntity<?> getActualGameSize(@PathVariable String gameName,
                                               Authentication authentication,
                                               HttpServletResponse response) {
        return gameService.getActualGameSize(gameName, authentication, response);
    }

    @GetMapping("{gameName}/library_main_image")
    public void getLibraryMainPageImage(@PathVariable String gameName,
                                        Authentication authentication,
                                        HttpServletResponse response) {
        gameService.downloadLibraryMainPageImage(gameName, authentication, response);
    }

    @GetMapping("{gameName}/files_list")
    public ResponseEntity<?> getGameFilesList(@PathVariable String gameName,
                                              Authentication authentication,
                                              HttpServletResponse response) {
        return gameService.getGameFilesList(gameName, authentication, response);
    }

    @PostMapping("{gameName}/download_file")
    public void getGameFile(@PathVariable String gameName,
                            @RequestBody GameFileDto gameFileDto,
                            Authentication authentication,
                            HttpServletResponse response) {
        gameService.downloadGameFile(gameName, gameFileDto, authentication, response);
    }

    @GetMapping("{gameName}/library_logo")
    public void getGameLibraryLogo(@PathVariable String gameName,
                                   Authentication authentication,
                                   HttpServletResponse response) {
        gameService.downloadGameLibraryLogo(gameName, authentication, response);
    }

    @GetMapping("{gameName}/update_news")
    public ResponseEntity<?> getGameUpdateNews(@PathVariable String gameName,
                                               Authentication authentication,
                                               HttpServletResponse response) {
        return gameService.getGameUpdateNews(gameName, authentication, response);
    }

    @PostMapping("{gameName}/update_image")
    public void getGameUpdateImage(@PathVariable String gameName,
                                   @RequestBody GameUpdateNews gameUpdateNews,
                                   Authentication authentication,
                                   HttpServletResponse response) {
        gameService.downloadUpdateImage(gameName, gameUpdateNews, authentication, response);
    }
}

package org.spring.project.application.server.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KeyProperties {

    @Value("${key.header.gameSize}")
    private String gameSize;
    @Value("${key.header.serverMessage}")
    private String serverMessage;
    @Value("${key.html.styleFolder}")
    private String styleFolder;
    @Value("${key.html.user}")
    private String user;
    @Value("${key.html.status}")
    private String status;
    @Value("${key.html.userHasGame}")
    private String userHasGame;
    @Value("${key.html.game}")
    private String game;
    @Value("${key.html.games}")
    private String games;
    @Value("${key.html.error}")
    private String error;
    @Value("${key.html.errors}")
    private String errors;

}

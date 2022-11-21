package org.spring.project.application.server.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FolderProperties {

    @Value("${folder.resources}")
    private String resourcesFolder;
    @Value("${folder.games}")
    private String gamesFolder;
    @Value("${folder.html.styles}")
    private String styleFolder;
    @Value("${folder.html.resources}")
    private String resourcesHtmlFolder;
}

package org.spring.project.application.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spring.project.application.server.model.GameUpdateNews;

import java.util.Set;

@Getter
@AllArgsConstructor
public class GameLibraryDto {

    private final String name;
    private final String title;
    private final String gameStartFileName;
    private final long defaultGameSize;
    private final Set<GameUpdateNews> gameUpdateNews;
}
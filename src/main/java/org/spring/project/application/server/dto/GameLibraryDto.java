package org.spring.project.application.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.spring.project.application.server.model.GameUpdateNews;

import java.util.Set;

@Data
@AllArgsConstructor
public class GameLibraryDto {

    private String name;
    private String title;
    private String gameStartFileName;
    private long defaultGameSize;
    private Set<GameUpdateNews> gameUpdateNews;
}
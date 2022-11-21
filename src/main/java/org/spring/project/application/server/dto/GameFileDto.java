package org.spring.project.application.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameFileDto {

    String filePath;
    Long fileLength;
}
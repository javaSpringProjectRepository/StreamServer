package org.spring.project.application.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameFileDto {

    String filePath;
    Long fileLength;
}
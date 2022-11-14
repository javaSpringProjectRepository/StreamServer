package org.spring.project.application.server.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "update_news")
@NoArgsConstructor
@RequiredArgsConstructor
public class GameUpdateNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String updateText;
    @NonNull
    private String updateImage;
    @NonNull
    private LocalDateTime updateTime;
}

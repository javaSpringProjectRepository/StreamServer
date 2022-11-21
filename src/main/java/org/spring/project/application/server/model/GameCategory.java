package org.spring.project.application.server.model;

import lombok.*;

import javax.persistence.*;

import java.util.Set;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "game_category")
@NoArgsConstructor
@RequiredArgsConstructor
public class GameCategory {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String title;
    @OneToMany(fetch = EAGER, mappedBy = "gameCategory")
    private Set<Game> games;
}

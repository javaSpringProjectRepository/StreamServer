package org.spring.project.application.server.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Table(name = "games")
@NoArgsConstructor
@RequiredArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String title;
    @NonNull
    private String gameStartFileName;
    @NonNull
    private String fullText;
    @NonNull
    private BigDecimal price;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "games_categories",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private GameCategory gameCategory;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "game_update",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "update_id"))
    private Set<GameUpdateNews> gameUpdateNews;

}

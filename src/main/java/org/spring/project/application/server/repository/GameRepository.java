package org.spring.project.application.server.repository;

import org.spring.project.application.server.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByName(String name);
    Optional<Game> findAllByGameCategory_Name(String name);
}

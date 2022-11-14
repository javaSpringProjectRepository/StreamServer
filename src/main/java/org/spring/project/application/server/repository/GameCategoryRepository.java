package org.spring.project.application.server.repository;

import org.spring.project.application.server.model.GameCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameCategoryRepository extends JpaRepository<GameCategory, Long> {
    Optional<GameCategory> findByName(String categoryName);
}

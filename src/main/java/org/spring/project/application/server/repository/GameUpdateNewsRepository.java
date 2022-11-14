package org.spring.project.application.server.repository;

import org.spring.project.application.server.model.GameUpdateNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameUpdateNewsRepository extends JpaRepository<GameUpdateNews, Long> {
}

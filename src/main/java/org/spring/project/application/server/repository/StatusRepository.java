package org.spring.project.application.server.repository;

import org.spring.project.application.server.model.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<State, Long> {

    Optional<State> findByName(String name);
}

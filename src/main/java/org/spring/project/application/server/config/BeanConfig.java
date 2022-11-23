package org.spring.project.application.server.config;

import org.spring.project.application.server.model.*;
import org.spring.project.application.server.service.AdminService;
import org.spring.project.application.server.service.GameService;
import org.spring.project.application.server.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    //Database sample

//    @Bean
//    public CommandLineRunner run(UserService userService, GameService gameService, AdminService adminService) {
//        return args -> {
//            adminService.saveRole(new Role("USER"));
//            adminService.saveRole(new Role("ADMIN"));
//            adminService.saveState(new State("ACTIVE"));
//            adminService.saveState(new State("BANNED"));
//            adminService.saveGameCategory(new GameCategory("ARCADE", "Аркада"));
//            adminService.saveGameCategory(new GameCategory("SPORT", "Спорт"));
//            adminService.saveGameCategory(new GameCategory("PUZZLE", "Головоломка"));
//            userService.saveInDatabase(new User("admin", "12345", "", new BigDecimal("0.00")));
//            adminService.addRoleToUser("admin", "ADMIN");
//            userService.saveInDatabase(new User("userA", "12345", "", new BigDecimal("1700.00")));
//            adminService.saveGameInDatabase(new Game(
//                    "action",
//                    "Action",
//                    "Action.exe",
//                    "Action full text",
//                    new BigDecimal("1000.00")
//            ));
//            adminService.saveGameInDatabase(new Game(
//                    "tennis",
//                    "Tennis",
//                    "Tennis.exe",
//                    "Tennis full text",
//                    new BigDecimal("700.00")
//            ));
//            adminService.saveGameInDatabase(new Game(
//                    "puzzle",
//                    "Puzzle",
//                    "Puzzle.exe",
//                    "Puzzle full text",
//                    new BigDecimal("500.00")
//            ));
//            gameService.saveGameToUser("userA", "action");
//            gameService.saveGameToUser("userA", "tennis");
//            adminService.addUpdateNewsToGame("action", "Обновление 1.1. Обновлены многие функции игрового процесса", "Update_1.1.jpg");
//            adminService.addUpdateNewsToGame("action", "Обновление 1.2. Обновлены многие функции игрового процесса", "Update_1.2.jpg");
//            adminService.addGameCategoryToGame("action", "ARCADE");
//            adminService.addGameCategoryToGame("tennis", "SPORT");
//            adminService.addGameCategoryToGame("puzzle", "PUZZLE");
//        };
//    }
}
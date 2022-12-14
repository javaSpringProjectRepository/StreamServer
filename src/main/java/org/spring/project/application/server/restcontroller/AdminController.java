package org.spring.project.application.server.restcontroller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.spring.project.application.server.model.Role;
import org.spring.project.application.server.model.State;
import org.spring.project.application.server.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/create_role")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        return adminService.saveRole(role);
    }

    @PostMapping("/create_state")
    public ResponseEntity<?> createStatus(@RequestBody State state) {
        return adminService.saveState(state);
    }

    @PostMapping("/role_to_user")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form) {
        return adminService.addRoleToUser(form.getUsername(), form.getRoleName());
    }

    @PostMapping("/state_to_user")
    public ResponseEntity<?> addStatusToUser(@RequestBody StatusToUserForm form) {
        return adminService.addStateToUser(form.getUsername(), form.getStatusName());
    }
}

@Data
class RoleToUserForm {
    private String username;
    private String roleName;
}

@Data
class StatusToUserForm {
    private String username;
    private String statusName;
}

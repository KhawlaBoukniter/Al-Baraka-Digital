package org.albarakadigital.controller;

import org.albarakadigital.entity.enums.Role;
import org.albarakadigital.entity.User;
import org.albarakadigital.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/api/admin/users")
    public ResponseEntity<User> createUser(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String password = (String) payload.get("password");
        String fullName = (String) payload.get("fullName");
        Role role = Role.valueOf((String) payload.get("role"));

        User user = adminService.createUser(email, password, fullName, role);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/api/admin/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String fullName = (String) payload.get("fullName");
        Boolean active = (Boolean) payload.get("active");
        Role role = payload.containsKey("role") ? Role.valueOf((String) payload.get("role")) : null;

        User user = adminService.updateUser(id, fullName, active, role);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
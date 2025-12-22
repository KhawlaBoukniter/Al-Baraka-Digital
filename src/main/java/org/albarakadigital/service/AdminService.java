package org.albarakadigital.service;

import org.albarakadigital.entity.User;
import org.albarakadigital.entity.enums.Role;

public interface AdminService {
    User createUser(String email, String password, String fullName, Role role);
    User updateUser(Long id, String fullName, Boolean active, Role role);
    void deleteUser(Long id);
}

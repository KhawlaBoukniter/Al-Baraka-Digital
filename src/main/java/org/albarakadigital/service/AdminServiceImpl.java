package org.albarakadigital.service;

import jakarta.persistence.EntityNotFoundException;
import org.albarakadigital.entity.enums.Role;
import org.albarakadigital.entity.User;
import org.albarakadigital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String email, String password, String fullName, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, String fullName, Boolean active, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (fullName != null) {
            user.setFullName(fullName);
        }
        if (active != null) {
            user.setActive(active);
        }
        if (role != null) {
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        userRepository.delete(user);
    }
}
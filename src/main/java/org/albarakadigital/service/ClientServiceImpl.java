package org.albarakadigital.service;

import jakarta.persistence.EntityNotFoundException;
import org.albarakadigital.entity.Account;
import org.albarakadigital.entity.enums.Role;
import org.albarakadigital.entity.User;
import org.albarakadigital.repository.AccountRepository;
import org.albarakadigital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = "ACC" + String.format("%08d", new Random().nextInt(100000000));
        } while (accountRepository.findByAccountNumber(number).isPresent());
        return number;
    }

    @Transactional
    public User createClient(String email, String password, String fullName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setBalance(0.0);
        account.setOwner(user);

        user.setAccount(account);

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
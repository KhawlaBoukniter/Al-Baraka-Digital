package org.albarakadigital.controller;

import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.User;
import org.albarakadigital.service.ClientServiceImpl;
import org.albarakadigital.service.OperationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientServiceImpl clientService;
    private final OperationServiceImpl operationService;

    @PostMapping("/api/client/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String fullName = payload.get("fullName");

        User user = clientService.createClient(email, password, fullName);

        return ResponseEntity.ok("Compte créé avec numéro de compte : " + user.getAccount().getAccountNumber());
    }

    @PostMapping("/api/client/operations")
    public ResponseEntity<String> createOperation(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String type = (String) payload.get("type");
        double amount = Double.parseDouble(payload.get("amount").toString());

        if ("DEPOSIT".equals(type)) {
            String email = authentication.getName();
            User user = clientService.getUserByEmail(email);
            Operation operation = operationService.createDeposit(user.getAccount().getId(), amount);
            return ResponseEntity.ok("Dépôt créé avec ID : " + operation.getId());
        }

        return ResponseEntity.badRequest().body("Type d'opération non supporté");
    }
}
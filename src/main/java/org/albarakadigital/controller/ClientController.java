package org.albarakadigital.controller;

import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.User;
import org.albarakadigital.service.ClientService;
import org.albarakadigital.service.OperationService;
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

    private final ClientService clientService;
    private final OperationService operationService;

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

        String email = authentication.getName();
        User user = clientService.getUserByEmail(email);
        Long accountId = user.getAccount().getId();

        Operation operation;
        if ("DEPOSIT".equals(type)) {
            operation = operationService.createDeposit(accountId, amount);
        } else if ("WITHDRAWAL".equals(type)) {
            operation = operationService.createWithdrawal(accountId, amount);
        } else {
            return ResponseEntity.badRequest().body("Type d'opération non supporté");
        }

        return ResponseEntity.ok("Opération créée avec ID : " + operation.getId());
    }
}
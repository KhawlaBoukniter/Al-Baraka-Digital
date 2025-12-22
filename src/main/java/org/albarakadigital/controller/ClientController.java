package org.albarakadigital.controller;

import org.albarakadigital.entity.Document;
import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.User;
import org.albarakadigital.service.ClientService;
import org.albarakadigital.service.DocumentService;
import org.albarakadigital.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final OperationService operationService;
    private final DocumentService documentService;

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
        Long sourceAccountId = user.getAccount().getId();

        Operation operation;
        if ("DEPOSIT".equals(type)) {
            operation = operationService.createDeposit(sourceAccountId, amount);
        } else if ("WITHDRAWAL".equals(type)) {
            operation = operationService.createWithdrawal(sourceAccountId, amount);
        } else if ("TRANSFER".equals(type)) {
            String destinationAccountNumber = (String) payload.get("destinationAccountNumber");
            operation = operationService.createTransfer(sourceAccountId, destinationAccountNumber, amount);
        } else {
            return ResponseEntity.badRequest().body("Type d'opération non supporté");
        }

        return ResponseEntity.ok("Opération créée avec ID : " + operation.getId());
    }

    @PostMapping("/api/client/operations/{id}/document")
    public ResponseEntity<String> uploadDocument(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        Document document = documentService.uploadJustificatif(id, file);
        return ResponseEntity.ok("Justificatif uploadé avec ID : " + document.getId());
    }
}
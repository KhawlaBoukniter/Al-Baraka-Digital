package org.albarakadigital.controller;

import org.albarakadigital.entity.User;
import org.albarakadigital.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/api/client/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String fullName = payload.get("fullName");

        User user = clientService.createClient(email, password, fullName);

        return ResponseEntity.ok("Compte créé avec numéro de compte : " + user.getAccount().getAccountNumber());
    }
}
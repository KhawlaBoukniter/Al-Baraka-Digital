package org.albarakadigital.controller;

import org.albarakadigital.entity.Operation;
import org.albarakadigital.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping("/api/agent/operations/pending")
    public ResponseEntity<List<Operation>> getPendingOperations() {
        return ResponseEntity.ok(agentService.getPendingOperations());
    }

    @PutMapping("/api/agent/operations/{id}/approve")
    public ResponseEntity<Operation> approveOperation(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.approveOperation(id));
    }

    @PutMapping("/api/agent/operations/{id}/reject")
    public ResponseEntity<Operation> rejectOperation(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.rejectOperation(id));
    }
}
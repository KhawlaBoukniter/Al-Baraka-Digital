package org.albarakadigital.service;

import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.enums.OperationStatus;
import org.albarakadigital.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final OperationRepository operationRepository;

    public List<Operation> getPendingOperations() {
        return operationRepository.findByStatus(OperationStatus.PENDING);
    }

    @Transactional
    public Operation approveOperation(Long id) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opération non trouvée"));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Opération non en attente");
        }

        operation.setStatus(OperationStatus.APPROVED);
        operation.setValidatedAt(LocalDateTime.now());

        switch (operation.getType()) {
            case DEPOSIT:
                operation.getAccountSource().setBalance(operation.getAccountSource().getBalance() + operation.getAmount());
                break;
            case WITHDRAWAL:
                operation.getAccountSource().setBalance(operation.getAccountSource().getBalance() - operation.getAmount());
                break;
            case TRANSFER:
                operation.getAccountSource().setBalance(operation.getAccountSource().getBalance() - operation.getAmount());
                operation.getAccountDestination().setBalance(operation.getAccountDestination().getBalance() + operation.getAmount());
                break;
        }

        operation.setExecutedAt(LocalDateTime.now());
        operation.setStatus(OperationStatus.EXECUTED);

        return operationRepository.save(operation);
    }

    @Transactional
    public Operation rejectOperation(Long id) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opération non trouvée"));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Opération non en attente");
        }

        operation.setStatus(OperationStatus.REJECTED);
        operation.setValidatedAt(LocalDateTime.now());

        return operationRepository.save(operation);
    }
}
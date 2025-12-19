package org.albarakadigital.service;

import org.albarakadigital.entity.Account;
import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.enums.OperationStatus;
import org.albarakadigital.entity.enums.OperationType;
import org.albarakadigital.repository.AccountRepository;
import org.albarakadigital.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;


    @Transactional
    public Operation createDeposit(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Operation operation = new Operation();
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(amount);
        operation.setCreatedAt(LocalDateTime.now());
        operation.setAccountSource(account);

        if (amount <= 10000.0) {
            operation.setStatus(OperationStatus.EXECUTED);
            operation.setExecutedAt(LocalDateTime.now());
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);
        } else {
            operation.setStatus(OperationStatus.PENDING);
        }

        return operationRepository.save(operation);
    }

    @Transactional
    public Operation createWithdrawal(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        if (account.getBalance() < amount) {
            throw new RuntimeException("Solde insuffisant");
        }

        Operation operation = new Operation();
        operation.setType(OperationType.WITHDRAWAL);
        operation.setAmount(amount);
        operation.setCreatedAt(LocalDateTime.now());
        operation.setAccountSource(account);

        if (amount <= 10000.0) {
            operation.setStatus(OperationStatus.EXECUTED);
            operation.setExecutedAt(LocalDateTime.now());
            account.setBalance(account.getBalance() - amount);
            accountRepository.save(account);
        } else {
            operation.setStatus(OperationStatus.PENDING);
        }

        return operationRepository.save(operation);
    }

    @Transactional
    public Operation createTransfer(Long sourceAccountId, String destinationAccountNumber, Double amount) {
        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new RuntimeException("Compte source non trouvé"));

        Account destination = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new RuntimeException("Compte destination non trouvé"));

        if (source.getBalance() < amount) {
            throw new RuntimeException("Solde insuffisant sur le compte source");
        }

        Operation operation = new Operation();
        operation.setType(OperationType.TRANSFER);
        operation.setAmount(amount);
        operation.setCreatedAt(LocalDateTime.now());
        operation.setAccountSource(source);
        operation.setAccountDestination(destination);

        if (amount <= 10000.0) {
            operation.setStatus(OperationStatus.EXECUTED);
            operation.setExecutedAt(LocalDateTime.now());

            source.setBalance(source.getBalance() - amount);
            destination.setBalance(destination.getBalance() + amount);

            accountRepository.save(source);
            accountRepository.save(destination);
        } else {
            operation.setStatus(OperationStatus.PENDING);
        }

        return operationRepository.save(operation);
    }
}
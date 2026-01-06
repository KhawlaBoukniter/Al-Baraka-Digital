package org.albarakadigital.service;

import jakarta.persistence.EntityNotFoundException;
import org.albarakadigital.entity.AIAnalysis;
import org.albarakadigital.entity.Account;
import org.albarakadigital.entity.Document;
import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.enums.AIRecommendation;
import org.albarakadigital.entity.enums.OperationStatus;
import org.albarakadigital.entity.enums.OperationType;
import org.albarakadigital.repository.AIAnalysisRepository;
import org.albarakadigital.repository.AccountRepository;
import org.albarakadigital.repository.DocumentRepository;
import org.albarakadigital.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;

    private final AIAnalysisRepository aiAnalysisRepository;
    private final DocumentRepository documentRepository;
    private final ChatClient chatClient;

    @Transactional
    public Operation createDeposit(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Compte non trouvé"));

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
                .orElseThrow(() -> new EntityNotFoundException("Compte non trouvé"));

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
                .orElseThrow(() -> new EntityNotFoundException("Compte source non trouvé"));

        Account destination = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Compte destination non trouvé"));

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
            source.setBalance(source.getBalance() - amount);
            destination.setBalance(destination.getBalance() + amount);
            accountRepository.save(source);
            accountRepository.save(destination);
        }

        return operationRepository.save(operation);
    }

    public List<Operation> getOperationsByAccount(Long accountId) {
        return operationRepository.findByAccountSource_Id(accountId);
    }

        @Transactional
        public AIAnalysis analyzeAndExecute(Long operationId, Long documentId) {

            Operation operation = operationRepository.findById(operationId)
                    .orElseThrow(() -> new RuntimeException("Opération non trouvée"));

            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            if (operation.getStatus() != OperationStatus.PENDING) {
                throw new RuntimeException("Opération doit être en PENDING");
            }

            try {
                String prompt = String.format(
                        """
                                Analysez ce justificatif bancaire:
                                Document: %s
                                Opération: %s de %.2f DH
                                Répondez UNIQUEMENT par: APPROVE / REJECT / NEED_HUMAN_REVIEW
                                """,
                        document.getFileName(),
                        operation.getType(),
                        operation.getAmount()
                );

                String response = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content()
                        .trim()
                        .toUpperCase();

                AIRecommendation recommendation = response.contains("APPROVE")
                        ? AIRecommendation.APPROVE
                        : response.contains("REJECT")
                        ? AIRecommendation.REJECT
                        : AIRecommendation.NEED_HUMAN_REVIEW;

                AIAnalysis analysis = AIAnalysis.builder()
                        .operation(operation)
                        .document(document)
                        .recommendation(recommendation)
                        .analysisDetails(recommendation.name())
                        .confidenceScore(0.85)
                        .analyzedAt(LocalDateTime.now())
                        .build();

                if (recommendation == AIRecommendation.APPROVE) {
                    operation.setStatus(OperationStatus.EXECUTED);
                    operation.setExecutedAt(LocalDateTime.now());
                    Account account = operation.getAccountSource();
                    account.setBalance(account.getBalance() + operation.getAmount());
                    accountRepository.save(account);
                } else if (recommendation == AIRecommendation.REJECT) {
                    operation.setStatus(OperationStatus.REJECTED);
                } else {
                    operation.setStatus(OperationStatus.PENDING);
                }

                operationRepository.save(operation);
                return aiAnalysisRepository.save(analysis);

            } catch (Exception e) {
                operation.setStatus(OperationStatus.PENDING);
                operationRepository.save(operation);
                throw new RuntimeException("Erreur analyse: " + e.getMessage());
            }
        }

        @Override
        public Operation getOperationById(Long id) {
            return operationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Opération non trouvée"));
        }

}
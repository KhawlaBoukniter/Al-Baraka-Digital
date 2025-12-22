package org.albarakadigital.service;

import org.albarakadigital.entity.Operation;

import java.util.List;

public interface OperationService {
    Operation createDeposit(Long accountId, Double amount);
    Operation createWithdrawal(Long accountId, Double amount);
    Operation createTransfer(Long sourceAccountId, String destinationAccountNumber, Double amount);
    List<Operation> getOperationsByAccount(Long accountId);
}

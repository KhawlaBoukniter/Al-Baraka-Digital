package org.albarakadigital.service;

import org.albarakadigital.entity.Operation;

public interface OperationService {
    Operation createDeposit(Long accountId, Double amount);
}

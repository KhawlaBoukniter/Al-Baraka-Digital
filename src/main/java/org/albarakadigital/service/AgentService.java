package org.albarakadigital.service;

import org.albarakadigital.entity.Operation;

import java.util.List;

public interface AgentService {
    List<Operation> getPendingOperations();
    Operation approveOperation(Long id);
    Operation rejectOperation(Long id);
}

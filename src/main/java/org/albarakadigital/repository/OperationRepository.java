package org.albarakadigital.repository;

import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.enums.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation,Long> {
    List<Operation> findByStatus(OperationStatus status);
}

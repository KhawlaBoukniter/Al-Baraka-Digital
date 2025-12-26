package org.albarakadigital.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.albarakadigital.entity.Account;
import org.albarakadigital.entity.enums.OperationStatus;
import org.albarakadigital.entity.enums.OperationType;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "operations")
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime validatedAt;

    private LocalDateTime executedAt;

    @ManyToOne
    @JoinColumn(name = "account_source_id")
    @JsonIgnoreProperties({"owner", "accountSource", "accountDestination"})
    private Account accountSource;

    @ManyToOne
    @JoinColumn(name = "account_destination_id")
    @JsonIgnoreProperties({"owner", "accountSource", "accountDestination"})
    private Account accountDestination;

}
package org.albarakadigital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.albarakadigital.entity.enums.AIRecommendation;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "operation_id", unique = true)
    private Operation operation;

    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    private AIRecommendation recommendation;

    private String analysisDetails;
    private Double confidenceScore;
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
}

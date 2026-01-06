package org.albarakadigital.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {

    private final ChatClient chatClient;

    /**
     * Analyse un document bancaire et retourne une recommandation
     *
     * @param operationAmount Montant de l'opération
     * @param operationType Type d'opération (DEPOSIT, WITHDRAWAL, TRANSFER)
     * @param documentPath Chemin du document uploadé
     * @return Map avec: recommendation, confidenceScore, analysisDetails
     */
    public Map<String, Object> analyzeDocument(
            Double operationAmount,
            String operationType,
            String documentPath) {

        log.info("Début analyse IA pour {} DH - {}", operationAmount, operationType);

        try {
            // 1. Extraire contenu du document
            String documentContent = extractDocumentContent(documentPath);
            log.info("✅ Contenu extrait du document");

            // 2. Construire le prompt d'analyse
            String prompt = buildAnalysisPrompt(operationAmount, operationType, documentContent);

            // 3. Envoyer à OpenAI et obtenir réponse
            String aiResponse = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("✅ Réponse OpenAI reçue");

            // 4. Parser la réponse JSON
            Map<String, Object> result = parseAIResponse(aiResponse);

            log.info("✅ Analyse terminée: {}", result.get("recommendation"));
            return result;

        } catch (Exception e) {
            log.error("❌ Erreur analyse IA: {}", e.getMessage());

            // Fallback: retourner NEED_HUMAN_REVIEW en cas d'erreur
            return Map.of(
                    "recommendation", "NEED_HUMAN_REVIEW",
                    "confidenceScore", 0.0,
                    "analysisDetails", "Erreur lors de l'analyse: " + e.getMessage()
            );
        }
    }

    /**
     * Extrait le contenu du document (PDF ou image)
     */
    private String extractDocumentContent(String documentPath) throws Exception {
        File file = new File(documentPath);

        if (!file.exists()) {
            throw new RuntimeException("Fichier non trouvé: " + documentPath);
        }

        String extension = documentPath.substring(documentPath.lastIndexOf('.') + 1).toLowerCase();

        if ("pdf".equals(extension)) {
            return extractPDFContent(file);
        } else if ("jpg".equals(extension) || "png".equals(extension)) {
            return extractImageContent(file);
        } else {
            throw new RuntimeException("Type de fichier non supporté: " + extension);
        }
    }

    /**
     * Extrait texte d'un PDF
     */
    private String extractPDFContent(File file) throws Exception {
        try {
            org.apache.pdfbox.pdmodel.PDDocument document =
                    org.apache.pdfbox.pdmodel.PDDocument.load(file);

            org.apache.pdfbox.text.PDFTextStripper stripper =
                    new org.apache.pdfbox.text.PDFTextStripper();

            String text = stripper.getText(document);
            document.close();

            return text.isEmpty() ? "[PDF illisible]" : text;
        } catch (Exception e) {
            log.warn("⚠️ Impossible d'extraire PDF, utilisation contenu par défaut");
            return "[Document PDF - contenu non extractible]";
        }
    }

    /**
     * Extrait texte d'une image
     */
    private String extractImageContent(File file) throws Exception {
        byte[] imageBytes = Files.readAllBytes(file.toPath());

        // Pour images: on va simplement retourner un message
        // Dans une vrai implémentation, on utiliserait Vision API
        return "[Image reçue - " + file.getName() + "]";
    }

    /**
     * Construit le prompt pour OpenAI
     */
    private String buildAnalysisPrompt(
            Double amount,
            String operationType,
            String documentContent) {

        return String.format(
                """
                Vous êtes un expert en validation de documents bancaires.
                
                OPÉRATION DEMANDÉE:
                - Type: %s
                - Montant: %.2f DH
                
                CONTENU DU DOCUMENT:
                %s
                
                ANALYSE REQUISE:
                1. Le document correspond-il à l'opération?
                2. Les montants sont-ils cohérents?
                3. Y a-t-il des signaux d'alerte (falsification, incohérence)?
                
                RÉPONDEZ EN JSON VALIDE (et UNIQUEMENT du JSON):
                {
                  "recommendation": "APPROVE ou REJECT ou NEED_HUMAN_REVIEW",
                  "confidence_score": 0.0 à 1.0,
                  "analysis_details": "votre analyse",
                  "reason_for_rejection": "si REJECT, la raison"
                }
                """,
                operationType,
                amount,
                documentContent
        );
    }

    /**
     * Parse la réponse JSON d'OpenAI
     */
    private Map<String, Object> parseAIResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Extraire le JSON de la réponse
            String jsonPart = response;
            if (response.contains("{")) {
                jsonPart = response.substring(
                        response.indexOf("{"),
                        response.lastIndexOf("}") + 1
                );
            }

            JsonNode json = mapper.readTree(jsonPart);

            Map<String, Object> result = new HashMap<>();
            result.put("recommendation", json.get("recommendation").asText());
            result.put("confidenceScore", json.get("confidence_score").asDouble());
            result.put("analysisDetails", json.get("analysis_details").asText());

            if (json.has("reason_for_rejection")) {
                result.put("reasonForRejection", json.get("reason_for_rejection").asText());
            }

            return result;
        } catch (Exception e) {
            log.error("Erreur parsing JSON: {}", e.getMessage());
            return Map.of(
                    "recommendation", "NEED_HUMAN_REVIEW",
                    "confidenceScore", 0.0,
                    "analysisDetails", "Erreur parsing réponse IA"
            );
        }
    }
}

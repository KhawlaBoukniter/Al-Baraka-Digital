package org.albarakadigital.service;

import org.albarakadigital.entity.Document;
import org.albarakadigital.entity.enums.FileType;
import org.albarakadigital.entity.Operation;
import org.albarakadigital.repository.DocumentRepository;
import org.albarakadigital.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final OperationRepository operationRepository;


    @Transactional
    public Document uploadJustificatif(Long operationId, MultipartFile file) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Opération non trouvée"));

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Fichier trop volumineux (max 5MB)");
        }

        String originalFilename = file.getOriginalFilename();
        String fileTypeStr = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();
        FileType fileType;
        try {
            fileType = FileType.valueOf(fileTypeStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de fichier non supporté (PDF/JPG/PNG)");
        }

        String storagePath = "uploads/" + UUID.randomUUID() + "." + fileTypeStr.toLowerCase();
        try {
            file.transferTo(new File(storagePath));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier");
        }

        Document document = new Document();
        document.setFileName(originalFilename);
        document.setFileType(fileType);
        document.setStoragePath(storagePath);
        document.setUploadedAt(LocalDateTime.now());
        document.setOperation(operation);

        return documentRepository.save(document);
    }
}
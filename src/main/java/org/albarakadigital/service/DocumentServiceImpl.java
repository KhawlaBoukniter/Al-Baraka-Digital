package org.albarakadigital.service;

import org.albarakadigital.entity.Document;
import org.albarakadigital.entity.enums.FileType;
import org.albarakadigital.entity.Operation;
import org.albarakadigital.repository.DocumentRepository;
import org.albarakadigital.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final OperationRepository operationRepository;

    @Value("${upload.dir:uploads/}")
    private String uploadDir;

    @Override
    @Transactional
    public Document uploadJustificatif(Long operationId, MultipartFile file) {

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Opération non trouvée"));

        if (file.isEmpty()) {
            throw new RuntimeException("Fichier vide");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Fichier trop volumineux (max 5MB)");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new RuntimeException("Nom de fichier invalide");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toUpperCase();
        FileType fileType;
        try {
            fileType = FileType.valueOf(extension);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type de fichier non supporté (PDF/JPG/PNG)");
        }

        String absoluteUploadDir = System.getProperty("user.dir") + File.separator + uploadDir;
        File dir = new File(absoluteUploadDir);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Impossible de créer le dossier d'upload");
        }

        String filename = UUID.randomUUID() + "." + extension.toLowerCase();

        File destination = new File(dir, filename);

        try {
            file.transferTo(destination);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
        }

        String storagePath = uploadDir + filename;

        Document document = new Document();
        document.setFileName(originalFilename);
        document.setFileType(fileType);
        document.setStoragePath(storagePath);
        document.setUploadedAt(LocalDateTime.now());
        document.setOperation(operation);

        return documentRepository.save(document);
    }
}

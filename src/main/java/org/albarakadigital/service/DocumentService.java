package org.albarakadigital.service;

import org.albarakadigital.entity.Document;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    Document uploadJustificatif(Long operationId, MultipartFile file);
}

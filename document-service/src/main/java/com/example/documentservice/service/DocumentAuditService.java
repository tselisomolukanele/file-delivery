package com.example.documentservice.service;

import java.time.LocalDateTime;

import com.example.documentservice.persistence.model.DocumentAudit;
import com.example.documentservice.persistence.model.DocumentAuditAction;
import com.example.documentservice.persistence.model.DocumentAuditAuthType;
import com.example.documentservice.persistence.repository.DocumentAuditRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class DocumentAuditService {

    @Inject
    DocumentAuditRepository documentAuditRepository;

    public void recordDocumentAccess(Long documentId, String userName, DocumentAuditAction actionType,
        DocumentAuditAuthType authType) {
        DocumentAudit audit = new DocumentAudit();
        audit.setDocumentId(documentId);
        audit.setUserName(userName);
        audit.setActionType(actionType);
        audit.setAuthType(authType);
        audit.setOccurredAt(LocalDateTime.now());

        documentAuditRepository.persist(audit);
    }
}

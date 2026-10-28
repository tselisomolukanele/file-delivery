package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import com.example.documentservice.persistence.model.DocumentAudit;
import com.example.documentservice.persistence.model.DocumentAuditAction;
import com.example.documentservice.persistence.model.DocumentAuditAuthType;
import com.example.documentservice.persistence.repository.DocumentAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DocumentAuditServiceTest {

    private DocumentAuditRepository documentAuditRepository;
    private DocumentAuditService service;

    @BeforeEach
    void setUp() throws Exception {
        documentAuditRepository = mock(DocumentAuditRepository.class);
        service = new DocumentAuditService();
        setField("documentAuditRepository", documentAuditRepository);
    }

    @Test
    void recordDocumentAccessPersistsAuditWithMetadata() throws Exception {
        service.recordDocumentAccess(7L, "alice", DocumentAuditAction.DOWNLOAD, DocumentAuditAuthType.LINK_OTP);

        ArgumentCaptor<DocumentAudit> captor = ArgumentCaptor.forClass(DocumentAudit.class);
        verify(documentAuditRepository).persist(captor.capture());

        DocumentAudit audit = captor.getValue();
        assertEquals(7L, audit.getDocumentId());
        assertEquals("alice", audit.getUserName());
        assertEquals(DocumentAuditAction.DOWNLOAD, audit.getActionType());
        assertEquals(DocumentAuditAuthType.LINK_OTP, audit.getAuthType());
        assertNotNull(audit.getOccurredAt());
        assertNotNull(service);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = DocumentAuditService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}

package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;

import com.example.documentservice.persistence.model.Document;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrcherstratorServiceTest {

    private OrcherstratorService service;
    private DocumentLinkService documentLinkService;
    private DocumentDataService documentDataService;
    private CustomerService customerService;

    @BeforeEach
    void setUp() throws Exception {
        service = new OrcherstratorService();
        documentLinkService = mock(DocumentLinkService.class);
        documentDataService = mock(DocumentDataService.class);
        customerService = mock(CustomerService.class);

        setField("documentLinkService", documentLinkService);
        setField("documentDataService", documentDataService);
        setField("customerService", customerService);
    }

    @Test
    void createAndUploadDocumentUploadsDataAndAddsToCustomer() throws Exception {
        File file = File.createTempFile("upload", ".pdf");
        file.deleteOnExit();

        Document createdDocument = new Document();
        createdDocument.setChecksum("checksum-123");
        when(customerService.addDocumentToCustomer("alice", file, "invoice.pdf")).thenReturn(createdDocument);

        service.createAndUploadDocument("alice", file, "invoice.pdf", "application/pdf");

        verify(documentDataService).uploadFile(argThat(documentData ->
            "alice_checksum-123".equals(documentData.getFilename())
                && "application/pdf".equals(documentData.getMimetype())
                && documentData.getFile() == file
        ));
        verify(customerService).addDocumentToCustomer("alice", file, "invoice.pdf");
    }

    @Test
    void getDocumentDataLoadsDataUsingStoredLink() {
        DocumentLinkEntry linkEntry =
            DocumentLinkEntry
            .builder()
            .ownerId("alice")
            .documentId(42L)
            .build();

        DocumentData expected =
            DocumentData
            .builder()
            .data(new byte[]{1, 2, 3})
            .filename("alice_invoice.pdf")
            .mimetype("application/pdf")
            .build();            

        when(documentLinkService.getDocumentLink("token-1")).thenReturn(linkEntry);
        when(documentDataService.downloadFile("alice", 42L)).thenReturn(expected);

        DocumentData result = service.getDocumentData("token-1");

        assertSame(expected, result);
        assertEquals("alice_invoice.pdf", result.getFilename());
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = OrcherstratorService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}

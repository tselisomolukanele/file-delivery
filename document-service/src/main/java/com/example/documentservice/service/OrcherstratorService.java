package com.example.documentservice.service;

import java.io.File;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped 
@Transactional
@Slf4j 
public class OrcherstratorService {
    @Inject 
    DocumentLinkService documentLinkService;

    @Inject
    DocumentDataService documentDataService;
    
    @Inject
    CustomerService customerService;

    public void createAndUploadDocument(String customerName, File data, String filename, String mimetype) {
        log.info("Creating and uploading document for customer: {}", customerName);
        customerService.addDocumentToCustomer(customerName, filename, data.length());

        // Handle logic that cannot be rollbacked last
        documentDataService.uploadFile(new DocumentData(null, data, customerName + "_" + filename, mimetype));
    }

    public DocumentData getDocumentData(String objectKey) {
        var documentLinkEntry = documentLinkService.getDocumentLink(objectKey);
        var documentData = documentDataService.downloadFile(documentLinkEntry.getOwnerId(), documentLinkEntry.getDocumentId());
        return documentData;
    }
}

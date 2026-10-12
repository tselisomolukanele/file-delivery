package com.example.documentservice.service;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@Transactional
public class DocumentLinkService {

    private static final String STORE_KEY = "document-links";
    
    @ConfigProperty(name = "host.base.url")
    String baseUrl;

    @Inject 
    RedisDataSource ds;

    private final HashCommands<String, String, DocumentLinkEntry> commands;

    public DocumentLinkService(RedisDataSource ds) { 
        commands = ds.hash(DocumentLinkEntry.class); 
    }

    public DocumentLinkEntry createDocumentLink(String documentId, String ownerId) {

        String uuid = UUID.randomUUID().toString();

        DocumentLinkEntry entry = new DocumentLinkEntry();
        entry.setId(uuid);
        entry.setDocumentId(documentId);
        entry.setOwnerId(ownerId);
        entry.setLink(baseUrl + "/document/download/" + uuid);

        commands.hset(STORE_KEY, uuid, entry);

        return entry;
    }

    public DocumentLinkEntry getDocumentLink(String objectKey) {
        
        var documentLinkEntry = commands.hget(STORE_KEY, objectKey);

        if (documentLinkEntry == null) {
            new BadRequestException("Document link not found for key: " + objectKey);
        }

        return documentLinkEntry;
    }
}

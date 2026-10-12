package com.example.documentservice.service;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@Transactional
public class DocumentLinkService {
    
    @ConfigProperty(name = "host.base.url")
    String baseUrl;

    @ConfigProperty(name = "token.ttl")
    int tokenTtl;

    @Inject 
    RedisDataSource ds;

    private final ValueCommands<String, DocumentLinkEntry> valueCommands;

    public DocumentLinkService(RedisDataSource ds) { 
        valueCommands = ds.value(DocumentLinkEntry.class);
    }

    public DocumentLinkEntry createDocumentLink(String documentId, String ownerId) {

        String uuid = UUID.randomUUID().toString();

        DocumentLinkEntry entry = new DocumentLinkEntry();
        entry.setId(uuid);
        entry.setDocumentId(documentId);
        entry.setOwnerId(ownerId);
        entry.setLink(baseUrl + "/document/download/" + uuid);

        valueCommands.set(uuid, entry, new SetArgs().ex(tokenTtl));

        return entry;
    }

    public DocumentLinkEntry getDocumentLink(String objectKey) {
        
        var documentLinkEntry = valueCommands.getdel(objectKey);

        if (documentLinkEntry == null) {
            throw new NotFoundException("Document link not found for key: " + objectKey);
        }

        return documentLinkEntry;
    }
}

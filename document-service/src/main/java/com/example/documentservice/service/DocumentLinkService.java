package com.example.documentservice.service;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@Transactional
public class DocumentLinkService {
    
    @ConfigProperty(name = "document.link.base.url")
    String baseUrl;

    @ConfigProperty(name = "link.ttl")
    int linkTtl;

    @Inject 
    RedisDataSource ds;

    private final ValueCommands<String, DocumentLinkEntry> valueCommands;

    public DocumentLinkService(RedisDataSource ds) { 
        valueCommands = ds.value(DocumentLinkEntry.class);
    }

    public DocumentLinkEntry createDocumentLink(long documentId, String ownerId) {

        String uuid = UUID.randomUUID().toString();

        DocumentLinkEntry entry =
            DocumentLinkEntry
            .builder()
            .id(uuid)
            .documentId(documentId)
            .ownerId(ownerId)
            .link(baseUrl + "/api/document/download/" + uuid)
            .build();

        valueCommands.set(uuid, entry, new SetArgs().ex(linkTtl));

        return entry;
    }

    public DocumentLinkEntry getDocumentLink(String objectKey) {
        
        var documentLinkEntry = valueCommands.get(objectKey);

        if (documentLinkEntry == null) {
            throw new NotFoundException("Document link has expired");
        }

        return documentLinkEntry;
    }

    public void removeLink(String objectKey) {
        
        valueCommands.getdel(objectKey);
    }
}

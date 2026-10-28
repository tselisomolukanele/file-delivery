package com.example.documentservice.service;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class DocumentLinkEntry {
    private String id;
    private long documentId;
    private String ownerId;
    private String link;
}

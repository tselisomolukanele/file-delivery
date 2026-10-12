package com.example.documentservice.service;

import lombok.Data;

@Data 
public class DocumentLinkEntry {

    private String id;
    private String documentId;
    private String ownerId;
    private String link;
}

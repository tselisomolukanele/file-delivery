package com.example.documentservice.service;

import java.io.File;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class DocumentData {
    private byte[] data;
    private File file;
    private String filename;
    private String mimetype;
    private String owner;
}

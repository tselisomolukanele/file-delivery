package com.example.documentservice.service;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor 
public class DocumentData {
    private byte[] data;
    private File file;
    private String filename;
    private String mimetype;
}

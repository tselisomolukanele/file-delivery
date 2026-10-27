package com.example.documentservice.presentation;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class DocumentDTO {
    private String name;
    private LocalDateTime uploadedAt;
    private long size;
    private long id;
}

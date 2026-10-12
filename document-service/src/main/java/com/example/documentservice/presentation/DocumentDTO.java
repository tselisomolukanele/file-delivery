package com.example.documentservice.presentation;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class DocumentDTO {
    private String name;
    private String link;
}

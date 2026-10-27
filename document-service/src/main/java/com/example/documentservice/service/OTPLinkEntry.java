package com.example.documentservice.service;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class OTPLinkEntry {

    private String otp;
    private String ownerId;
}

package com.example.documentservice.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.documentservice.persistence.model.Customer;
import com.example.documentservice.persistence.model.Document;
import com.example.documentservice.persistence.model.DocumentAuditAction;
import com.example.documentservice.persistence.model.DocumentAuditAuthType;
import com.example.documentservice.persistence.repository.CustomerRepository;
import com.example.documentservice.persistence.repository.DocumentRepository;
import com.example.documentservice.presentation.CustomerDTO;
import com.example.documentservice.presentation.DocumentDTO;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped 
@Transactional 
@Slf4j 
public class CustomerService {

    @Inject
    DocumentLinkService documentLinkService;

    @Inject
    private SecurityIdentity securityIdentity;
    
    @Inject
    CustomerRepository customerRepository;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    DocumentAuditService documentAuditService;

    @Inject 
    RedisDataSource ds;

    @ConfigProperty(name = "otp.ttl")
    int otpTtl;

    private final ValueCommands<String,OTPLinkEntry> valueCommands;
    private final ValueCommands<String, Long> countCommands;

    public CustomerService(RedisDataSource ds) { 
        valueCommands = ds.value(OTPLinkEntry.class);
        countCommands = ds.value(Long.class);
    }

    public List<CustomerDTO> getAllCustomers() {

        return customerRepository
            .findAll()
            .stream()
            .map(
                customerEntity ->  
                CustomerDTO.builder()
                    .name(customerEntity.getName())
                    .build()
            )
            .collect(Collectors.toList());
    }

    public List<DocumentDTO> getCustomerDocuments() {

        var customerName = securityIdentity.getPrincipal().getName();
        log.info("Fetching documents for customer: {}", customerName);

        return customerRepository.findByName(customerName)
            .orElseThrow(() -> new NotFoundException("Customer not found"))
            .getDocuments()
            .stream()
            .map(
                documentEntity ->
                DocumentDTO.builder()
                    .name(documentEntity.getFilename())
                    .size(documentEntity.getFileSize())
                    .uploadedAt(documentEntity.getUploadedAt())
                    .id(documentEntity.getId())
                    .build()
            )
            .collect(Collectors.toList());
    }

    public Document addDocumentToCustomer(String customerName, File file, String filename) {

        try {
            ByteSource byteSource = com.google.common.io.Files.asByteSource(file);
            HashCode hc = byteSource.hash(Hashing.sha256());
            String checksum = hc.toString();

            var newDocument = new Document();
            newDocument.setFilename(filename);
            newDocument.setFileSize(Files.size(Paths.get(file.getPath())));
            newDocument.setUploadedAt(LocalDateTime.now());
            newDocument.setUploadedBy(securityIdentity.getPrincipal().getName());
            newDocument.setChecksum(checksum);

            Optional<Document> found =
                documentRepository.findByChecksumAndCustomerName(checksum, customerName);
            if (found.isPresent()) {
                throw new BadRequestException("Document already exist for the customer");
            }

            customerRepository.findByName(customerName)
                .orElseThrow(() -> new NotFoundException("Customer not found"))
                .addDocument(newDocument);

            Document added =
                documentRepository.findByChecksumAndCustomerName(checksum, customerName)
                .orElseThrow();

            var loggedinUser = securityIdentity.getPrincipal().getName();
            documentAuditService.recordDocumentAccess(
                newDocument.getId(),
                loggedinUser,
                DocumentAuditAction.UPLOAD,
                DocumentAuditAuthType.SECURE
            );

            return added;
        } catch (IOException exception) {
            log.error("Document failed to upload", exception);
            throw new BadRequestException("Document failed to upload");
        }
    }

    public CustomerDTO addCustomer(CustomerDTO customerDTO) {
        var existingCustomer = customerRepository.findByName(customerDTO.getName());
        if (existingCustomer.isPresent()) {
            throw new BadRequestException("Customer already exists");
        }

        var newCustomer = new Customer();
        newCustomer.setName(customerDTO.getName());
        customerRepository.persist(newCustomer);

        return customerRepository.findByName(customerDTO.getName()).map(    
            customerEntity ->  
            CustomerDTO.builder()
                .name(customerEntity.getName())
                .build()
        ).orElseThrow(() -> new NotFoundException("Customer not found after creation"));
    }

    public String getOTP() {
        var key = securityIdentity.getPrincipal().getName();
        String otpKey = "otp:" + key;
        String countKey = "count:" + key;
        Random random = new SecureRandom();
        var otp = String.format("%06d", random.nextInt(1000000));
        
        valueCommands.set(
            otpKey, 
            OTPLinkEntry.builder().otp(otp).build(),
            new SetArgs().ex(otpTtl)
        );

        countCommands.set(
            countKey, 
            0l,
            new SetArgs().ex(otpTtl)
        );

        return otp;
    }

    public boolean validateOTP(String otp, String key) {
        String otpKey = "otp:" + key;
        String countKey = "count:" + key;
        var entry = valueCommands.get(otpKey);

        if (entry == null) {
            log.error("OTP has no entry");
            return false;
        }

        countCommands.incrby(countKey, 1);

        if (!entry.getOtp().equals(otp)) {

            log.error("OTP has no match");
            if (countCommands.get(countKey) > 3) {
                log.warn("OTP validation attempt reached maximum, otp will be removed to prevent brute force attempts.");
                valueCommands.getdel(otpKey);
                countCommands.getdel(countKey);        
            }
            return false;
        }

        valueCommands.getdel(otpKey);
        countCommands.getdel(countKey);
        return true;
    }
}

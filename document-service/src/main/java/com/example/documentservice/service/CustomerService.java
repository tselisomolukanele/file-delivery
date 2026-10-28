package com.example.documentservice.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

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

    private final ValueCommands<String,OTPLinkEntry> valueCommands;

    public CustomerService(RedisDataSource ds) { 
        valueCommands = ds.value(OTPLinkEntry.class);
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
            throw new RuntimeException(exception);
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
        var username = securityIdentity.getPrincipal().getName();
        Random random = new Random();
        var otp = String.format("%04d", random.nextInt(10000));
        
        valueCommands.set(
            username, 
            OTPLinkEntry.builder().otp(otp).ownerId(username).build()
        );

        return otp;
    }

    public boolean validateOTP(String otp, String username) {
        var entry = valueCommands.get(username);

        if (entry == null) {
            log.error(
                "OTP has no entry: {}  {}", 
                username,
                otp);
            return false;
        }

        if (!entry.getOwnerId().equals(username) || !entry.getOtp().equals(otp)) {

            log.error(
                "OTP has no match: {} != {}, {} != {}", 
                entry.getOwnerId(), 
                username, 
                entry.getOtp(),
                otp);
            return false;
        }

        valueCommands.getdel(username);
        return true;
    }
}

package com.example.documentservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.documentservice.persistence.model.Customer;
import com.example.documentservice.persistence.model.Document;
import com.example.documentservice.persistence.repository.CustomerRepository;
import com.example.documentservice.presentation.CustomerDTO;
import com.example.documentservice.presentation.DocumentDTO;

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

        log.info("Fetching documents for customer: {}", securityIdentity.getPrincipal().getName());

        var customerName = securityIdentity.getPrincipal().getName();
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
                    .link(
                        documentLinkService.createDocumentLink(documentEntity.getFilename(), 
                        securityIdentity.getPrincipal().getName()).getLink()
                    )
                    .build()
            )
            .collect(Collectors.toList());
    }

    public void addDocumentToCustomer(String customerName, String filename, long filesize) {

        var newDocument = new Document();
        newDocument.setFilename(filename);
        newDocument.setFileSize(filesize);
        newDocument.setUploadedAt(LocalDateTime.now());
        newDocument.setUploadedBy(securityIdentity.getPrincipal().getName());

        customerRepository.findByName(customerName)
            .orElseThrow(() -> new NotFoundException("Customer not found"))
            .addDocument(newDocument);
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
}

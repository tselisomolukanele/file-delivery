package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.example.documentservice.persistence.model.Customer;
import com.example.documentservice.persistence.model.Document;
import com.example.documentservice.persistence.repository.CustomerRepository;
import com.example.documentservice.presentation.CustomerDTO;
import com.example.documentservice.presentation.DocumentDTO;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CustomerServiceTest {

    @Inject
    CustomerService customerService;

    @InjectMock
    CustomerRepository customerRepository;

    @InjectMock
    DocumentLinkService documentLinkService;

    @InjectMock
    SecurityIdentity securityIdentity;

    @BeforeEach
    void setUp() {
        Principal principal = () -> "alice";
        when(securityIdentity.getPrincipal()).thenReturn(principal);
    }

    @Test
    void getAllCustomersReturnsMappedCustomers() {
        Customer customer = new Customer();
        customer.setName("alicia");

        PanacheQuery<Customer> query = org.mockito.Mockito.mock(PanacheQuery.class);
        when(customerRepository.findAll()).thenReturn(query);
        when(query.stream()).thenReturn(Stream.of(customer));

        List<CustomerDTO> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("alicia", result.get(0).getName());
    }

    @Test
    void getCustomerDocumentsReturnsDocumentsWithLinks() {
        Customer customer = new Customer();
        customer.setName("alice");

        Document document = new Document();
        document.setFilename("invoice.pdf");
        customer.addDocument(document);

        DocumentLinkEntry linkEntry = new DocumentLinkEntry();
        linkEntry.setLink("https://example.com/document/download/abc");

        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));
        when(documentLinkService.createDocumentLink("invoice.pdf", "alice")).thenReturn(linkEntry);

        List<DocumentDTO> result = customerService.getCustomerDocuments();

        assertEquals(1, result.size());
        assertEquals("invoice.pdf", result.get(0).getName());
        assertEquals("https://example.com/document/download/abc", result.get(0).getLink());
    }

    @Test
    void addCustomerPersistsWhenCustomerDoesNotExist() {
        CustomerDTO request = CustomerDTO.builder().name("bob").build();
        Customer saved = new Customer();
        saved.setName("bob");

        when(customerRepository.findByName("bob")).thenReturn(Optional.empty(), Optional.of(saved));

        CustomerDTO result = customerService.addCustomer(request);

        assertEquals("bob", result.getName());
        verify(customerRepository).persist(any(Customer.class));
    }

    @Test
    void addCustomerThrowsWhenCustomerAlreadyExists() {
        CustomerDTO request = CustomerDTO.builder().name("alice").build();
        Customer existing = new Customer();
        existing.setName("alice");

        when(customerRepository.findByName("alice")).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> customerService.addCustomer(request));
    }
}

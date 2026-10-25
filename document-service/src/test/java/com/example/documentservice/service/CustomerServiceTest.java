package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private CustomerService service;
    private CustomerRepository customerRepository;
    private DocumentLinkService documentLinkService;
    private SecurityIdentity securityIdentity;

    @BeforeEach
    void setUp() throws Exception {
        service = new CustomerService();
        customerRepository = mock(CustomerRepository.class);
        documentLinkService = mock(DocumentLinkService.class);
        securityIdentity = mock(SecurityIdentity.class);

        setField("customerRepository", customerRepository);
        setField("documentLinkService", documentLinkService);
        setField("securityIdentity", securityIdentity);

        Principal principal = () -> "alice";
        when(securityIdentity.getPrincipal()).thenReturn(principal);
    }

    @Test
    void getAllCustomersReturnsCustomerNames() {
        Customer customer = new Customer();
        customer.setName("alice");
        PanacheQuery<Customer> query = mock(PanacheQuery.class);
        when(customerRepository.findAll()).thenReturn(query);
        when(query.stream()).thenReturn(Stream.of(customer));

        List<CustomerDTO> results = service.getAllCustomers();

        assertEquals(1, results.size());
        assertEquals("alice", results.get(0).getName());
    }

    @Test
    void getCustomerDocumentsReturnsDocumentListWithLinks() {
        Customer customer = new Customer();
        Document document = new Document();
        document.setFilename("invoice.pdf");
        customer.addDocument(document);

        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));
        when(documentLinkService.createDocumentLink("invoice.pdf", "alice"))
            .thenReturn(newDocumentLink("https://example.com/download/abc-123"));

        List<DocumentDTO> results = service.getCustomerDocuments();

        assertEquals(1, results.size());
        assertEquals("invoice.pdf", results.get(0).getName());
        assertEquals("https://example.com/download/abc-123", results.get(0).getLink());
    }

    @Test
    void addCustomerRejectsDuplicateCustomer() {
        Customer customer = new Customer();
        customer.setName("alice");
        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));

        CustomerDTO payload = CustomerDTO.builder().name("alice").build();

        assertThrows(BadRequestException.class, () -> service.addCustomer(payload));
    }

    @Test
    void addDocumentToCustomerAddsDocumentToExistingCustomer() {
        Customer customer = new Customer();
        customer.setName("alice");
        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));

        service.addDocumentToCustomer("alice", "invoice.pdf");

        assertEquals(1, customer.getDocuments().size());
        assertEquals("invoice.pdf", customer.getDocuments().get(0).getFilename());
    }

    @Test
    void addDocumentToCustomerThrowsWhenCustomerIsMissing() {
        when(customerRepository.findByName("missing")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addDocumentToCustomer("missing", "invoice.pdf"));
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = CustomerService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    private DocumentLinkEntry newDocumentLink(String link) {
        DocumentLinkEntry entry = new DocumentLinkEntry();
        entry.setId("abc-123");
        entry.setDocumentId("invoice.pdf");
        entry.setOwnerId("alice");
        entry.setLink(link);
        return entry;
    }
}

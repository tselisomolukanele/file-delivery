package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.example.documentservice.persistence.model.Customer;
import com.example.documentservice.persistence.model.Document;
import com.example.documentservice.persistence.repository.CustomerRepository;
import com.example.documentservice.persistence.repository.DocumentRepository;
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
    private DocumentRepository documentRepository;
    private DocumentLinkService documentLinkService;
    private SecurityIdentity securityIdentity;

    @BeforeEach
    void setUp() throws Exception {
        service = new CustomerService();
        customerRepository = mock(CustomerRepository.class);
        documentRepository = mock(DocumentRepository.class);
        documentLinkService = mock(DocumentLinkService.class);
        securityIdentity = mock(SecurityIdentity.class);

        setField("customerRepository", customerRepository);
        setField("documentRepository", documentRepository);
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
    void getCustomerDocumentsReturnsDocumentListWithLinks() throws Exception {
        Customer customer = new Customer();
        Document document = new Document();
        setDocumentId(document, 42L);
        document.setFilename("invoice.pdf");
        customer.addDocument(document);

        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));
        when(documentLinkService.createDocumentLink(42L, "alice"))
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
    void addDocumentToCustomerAddsDocumentToExistingCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setName("alice");
        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));
        when(documentRepository.findByChecksumAndCustomerName(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("alice")))
            .thenReturn(Optional.empty());

        File file = File.createTempFile("invoice", ".pdf");
        file.deleteOnExit();

        service.addDocumentToCustomer("alice", file, "invoice.pdf");

        assertEquals(1, customer.getDocuments().size());
        assertEquals("invoice.pdf", customer.getDocuments().get(0).getFilename());
    }

    @Test
    void addDocumentToCustomerThrowsWhenCustomerIsMissing() throws Exception {
        when(customerRepository.findByName("missing")).thenReturn(Optional.empty());
        when(documentRepository.findByChecksumAndCustomerName(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("missing")))
            .thenReturn(Optional.empty());

        File file = File.createTempFile("missing", ".pdf");
        file.deleteOnExit();

        assertThrows(NotFoundException.class, () -> service.addDocumentToCustomer("missing", file, "missing.pdf"));
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = CustomerService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    private void setDocumentId(Document document, Long id) throws Exception {
        Field field = Document.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(document, id);
    }

    private DocumentLinkEntry newDocumentLink(String link) {
        DocumentLinkEntry entry = new DocumentLinkEntry();
        entry.setId("abc-123");
        entry.setDocumentId(42L);
        entry.setOwnerId("alice");
        entry.setLink(link);
        return entry;
    }
}

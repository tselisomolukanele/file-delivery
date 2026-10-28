package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private CustomerService service;
    private CustomerRepository customerRepository;
    private DocumentRepository documentRepository;
    private SecurityIdentity securityIdentity;
    private DocumentAuditService documentAuditService;
    private RedisDataSource redisDataSource;
    private ValueCommands<String, OTPLinkEntry> valueCommands;

    @BeforeEach
    void setUp() throws Exception {
        redisDataSource = mock(RedisDataSource.class);
        valueCommands = mock(ValueCommands.class);
        when(redisDataSource.value(OTPLinkEntry.class)).thenReturn(valueCommands);

        service = new CustomerService(redisDataSource);
        customerRepository = mock(CustomerRepository.class);
        documentRepository = mock(DocumentRepository.class);
        securityIdentity = mock(SecurityIdentity.class);
        documentAuditService = mock(DocumentAuditService.class);

        setField("customerRepository", customerRepository);
        setField("documentRepository", documentRepository);
        setField("securityIdentity", securityIdentity);
        setField("documentAuditService", documentAuditService);

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
    void getCustomerDocumentsReturnsDocumentList() throws Exception {
        Customer customer = new Customer();
        Document document = new Document();
        setDocumentId(document, 42L);
        document.setFilename("invoice.pdf");
        customer.addDocument(document);

        when(customerRepository.findByName("alice")).thenReturn(Optional.of(customer));

        List<DocumentDTO> results = service.getCustomerDocuments();

        assertEquals(1, results.size());
        assertEquals("invoice.pdf", results.get(0).getName());
        assertEquals(42L, results.get(0).getId());
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
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(new Document()));

        File file = File.createTempFile("invoice", ".pdf");
        file.deleteOnExit();

        Document result = service.addDocumentToCustomer("alice", file, "invoice.pdf");

        assertEquals(1, customer.getDocuments().size());
        assertEquals("invoice.pdf", customer.getDocuments().get(0).getFilename());
        assertNotNull(result);
    }

    @Test
    void addCustomerCreatesNewCustomerAndReturnsDto() {
        Customer savedCustomer = new Customer();
        savedCustomer.setName("bob");

        when(customerRepository.findByName("bob"))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(savedCustomer));

        CustomerDTO result = service.addCustomer(CustomerDTO.builder().name("bob").build());

        assertEquals("bob", result.getName());
    }

    @Test
    void getOTPStoresOtpForCurrentUser() {
        String otp = service.getOTP();

        assertEquals(4, otp.length());
        verify(valueCommands).set(org.mockito.ArgumentMatchers.eq("alice"), org.mockito.ArgumentMatchers.any(OTPLinkEntry.class));
    }

    @Test
    void validateOTPRejectsInvalidCode() {
        when(valueCommands.get("alice")).thenReturn(null);

        assertEquals(false, service.validateOTP("9999", "alice"));
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


}

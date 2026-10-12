package com.example.documentservice.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import com.example.documentservice.persistence.model.Customer;
import com.example.documentservice.persistence.model.Document;

@QuarkusTest
class CustomerRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @Test
    void persistCustomerWithDocuments() {
        Customer customer = new Customer();
        customer.setName("Acme Ltd");
        var document = new Document();
        document.setFilename("invoice-2026.pdf");
        customer.addDocument(document);

        customerRepository.save(customer);

        Customer persisted = customerRepository.findById(customer.getId());
        assertNotNull(persisted);
        assertEquals("Acme Ltd", persisted.getName());
        assertEquals(1, persisted.getDocuments().size());
        assertEquals("invoice-2026.pdf", persisted.getDocuments().get(0).getFilename());
    }
}

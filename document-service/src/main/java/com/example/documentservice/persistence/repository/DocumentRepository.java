package com.example.documentservice.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;

import com.example.documentservice.persistence.model.Document;

@ApplicationScoped
@Transactional
public class DocumentRepository implements PanacheRepository<Document> {
    public Optional<Document> findByChecksumAndCustomerName(String checksum, String customerName) {
        return find("checksum = ?1 and customer.name = ?2", checksum, customerName)
            .firstResultOptional();
    }

    public Optional<Document> findByIdAndCustomerName(long id, String customerName) {
        return find("id = ?1 and customer.name = ?2", id, customerName)
            .firstResultOptional();
    }
}

package com.example.documentservice.persistence.repository;

import com.example.documentservice.persistence.model.DocumentAudit;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class DocumentAuditRepository implements PanacheRepository<DocumentAudit> {
}

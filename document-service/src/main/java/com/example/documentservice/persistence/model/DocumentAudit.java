package com.example.documentservice.persistence.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_audits")
@Getter
@Setter
@NoArgsConstructor
public class DocumentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentAuditAction actionType;

    @Column(name = "auth_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentAuditAuthType authType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}

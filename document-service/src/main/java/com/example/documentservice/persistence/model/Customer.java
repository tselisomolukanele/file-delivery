package com.example.documentservice.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Setter
@Getter 
@NoArgsConstructor 
public class Customer extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
        for (Document document : this.documents) {
            document.setCustomer(this);
        }
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setCustomer(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setCustomer(null);
    }
}

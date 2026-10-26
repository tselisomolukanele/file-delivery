package com.example.documentservice.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;

import com.example.documentservice.persistence.model.Customer;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

    @Transactional
    public Customer save(Customer customer) {
        persist(customer);
        return customer;
    }

    public Optional<Customer> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}

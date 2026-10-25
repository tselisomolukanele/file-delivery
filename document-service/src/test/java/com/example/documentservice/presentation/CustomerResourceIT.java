package com.example.documentservice.presentation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.example.documentservice.service.CustomerService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CustomerResourceIT {

    @InjectMock
    CustomerService customerService;

    @Test
    @TestSecurity(user = "alice", roles = {"file-uploader"})
    void listCustomersReturnsCustomerNames() {
        when(customerService.getAllCustomers()).thenReturn(List.of(CustomerDTO.builder().name("alice").build()));

        given()
            .when()
            .get("/api/customers/")
            .then()
            .statusCode(200)
            .body("[0].name", equalTo("alice"));
    }

    @Test
    @TestSecurity(user = "alice", roles = {"file-uploader"})
    void addCustomerCreatesCustomer() {
        CustomerDTO payload = CustomerDTO.builder().name("bob").build();
        when(customerService.addCustomer(payload)).thenReturn(payload);

        given()
            .contentType("application/json")
            .body(payload)
            .when()
            .post("/api/customers/")
            .then()
            .statusCode(200)
            .body("name", equalTo("bob"));
    }

    @Test
    @TestSecurity(user = "alice", roles = {"file-owner"})
    void getCurrentCustomerDocumentsReturnsDocumentList() {
        DocumentDTO document = DocumentDTO.builder()
            .name("invoice.pdf")
            .link("https://example.com/download/abc")
            .uploadedAt(LocalDateTime.of(2026, 10, 25, 12, 0))
            .size(42)
            .build();

        when(customerService.getCustomerDocuments()).thenReturn(List.of(document));

        given()
            .when()
            .get("/api/customers/current/documents")
            .then()
            .statusCode(200)
            .body("[0].name", equalTo("invoice.pdf"))
            .body("[0].link", equalTo("https://example.com/download/abc"));
    }
}

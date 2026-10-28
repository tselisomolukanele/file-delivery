package com.example.documentservice.presentation;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;

import com.example.documentservice.service.CustomerService;
import com.example.documentservice.service.DocumentAuditService;
import com.example.documentservice.service.DocumentData;
import com.example.documentservice.service.OrchestratorService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DocumentResourceIT {

    @InjectMock
    OrchestratorService orchestratorService;

    @InjectMock
    CustomerService customerService;

    @InjectMock
    DocumentAuditService documentAuditService;

    @Test
    @TestSecurity(user = "alice", roles = {"file-uploader"})
    void uploadDocumentReturnsCreatedStatus() throws Exception {
        File tempFile = new File("src/test/resources/invoice.pdf");

        doNothing().when(orchestratorService)
            .createAndUploadDocument(anyString(), any(File.class), anyString(), anyString());

        given()
            .multiPart("file", tempFile, "application/pdf")
            .multiPart("filename", "invoice.pdf")
            .multiPart("customerName", "alice")
            .multiPart("mimetype", "application/pdf")
            .when()
            .post("/api/document/upload")
            .then()
            .statusCode(201);
    }

    @Test
    @TestSecurity(user = "alice", roles = {"file-uploader"})
    void uploadDocumentRejectsMissingFilename() {
        given()
            .multiPart("file", "document.txt", "hello".getBytes(StandardCharsets.UTF_8), "text/plain")
            .multiPart("customerName", "alice")
            .multiPart("mimetype", "text/plain")
            .when()
            .post("/api/document/upload")
            .then()
            .statusCode(400);
    }

    @Test
    void downloadDocumentReturnsFileContents() {
        DocumentData documentData = 
            DocumentData
            .builder()
            .data("hello".getBytes())
            .filename("invoice.pdf")
            .mimetype("application/pdf")
            .owner("person")
            .build();

        when(orchestratorService.getDocumentData("token-123")).thenReturn(documentData);
        when(customerService.validateOTP("2345", "person")).thenReturn(true);
        doNothing().when(documentAuditService).recordDocumentAccess(anyLong(), anyString(), any(), any());

        given()
            .when()
            .get("/api/document/download/token-123?otp=2345")
            .then()
            .statusCode(200)
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=\"invoice.pdf\"; filename*=UTF-8''invoice.pdf");
    }

    @Test
    void downloadDocumentEncodesFilenameForUtf8() {
        DocumentData documentData = 
            DocumentData
            .builder()
            .data("hello".getBytes())
            .filename("invoice résumé.pdf")
            .mimetype("application/pdf")
            .owner("person")
            .build();

        when(orchestratorService.getDocumentData("token-utf8")).thenReturn(documentData);
        when(customerService.validateOTP("2345", "person")).thenReturn(true);
        doNothing().when(documentAuditService).recordDocumentAccess(anyLong(), anyString(), any(), any());

        given()
            .when()
            .get("/api/document/download/token-utf8?otp=2345")
            .then()
            .statusCode(200)
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=\"invoice résumé.pdf\"; filename*=UTF-8''invoice%20r%C3%A9sum%C3%A9.pdf");
    }
}

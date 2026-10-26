package com.example.documentservice.presentation;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;

import com.example.documentservice.service.DocumentData;
import com.example.documentservice.service.OrcherstratorService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DocumentResourceIT {

    @InjectMock
    OrcherstratorService orchestratorService;

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
        DocumentData documentData = new DocumentData("hello".getBytes(), null, "invoice.pdf", "application/pdf");
        when(orchestratorService.getDocumentData("token-123")).thenReturn(documentData);

        given()
            .when()
            .get("/api/document/download/token-123")
            .then()
            .statusCode(200)
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment;filename=invoice.pdf");
    }
}

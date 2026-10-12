package com.example.documentservice.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class DocumentDataServiceTest {

    private DocumentDataService service;
    private S3Client s3Client;

    @BeforeEach
    void setUp() throws Exception {
        service = new DocumentDataService();
        s3Client = mock(S3Client.class);

        Field s3Field = DocumentDataService.class.getDeclaredField("s3");
        s3Field.setAccessible(true);
        s3Field.set(service, s3Client);

        Field bucketField = DocumentDataService.class.getDeclaredField("bucketName");
        bucketField.setAccessible(true);
        bucketField.set(service, "test-bucket");
    }

    @Test
    void downloadFileReturnsDocumentDataWhenObjectExists() {
        byte[] payload = "hello world".getBytes(StandardCharsets.UTF_8);
        ResponseBytes<GetObjectResponse> objectBytes = ResponseBytes.fromByteArray(
            GetObjectResponse.builder().contentType("text/plain").build(),
            payload
        );

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(objectBytes);

        DocumentData result = service.downloadFile("notes.txt");

        assertEquals("notes.txt", result.getFilename());
        assertEquals("text/plain", result.getMimetype());
        assertEquals("hello world", new String(result.getData(), StandardCharsets.UTF_8));
    }

    @Test
    void downloadFileThrowsWhenObjectIsEmpty() {
        ResponseBytes<GetObjectResponse> objectBytes = ResponseBytes.fromByteArray(
            GetObjectResponse.builder().contentType("text/plain").build(),
            new byte[0]
        );

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(objectBytes);

        assertThrows(NotFoundException.class, () -> service.downloadFile("empty.txt"));
    }

    @Test
    void uploadFileDoesNotThrowWhenPutObjectSucceeds() throws Exception {
        File tempFile = File.createTempFile("upload-test", ".txt");
        tempFile.deleteOnExit();

        DocumentData documentData = new DocumentData(
            "payload".getBytes(StandardCharsets.UTF_8),
            tempFile,
            "upload.txt",
            "text/plain"
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());

        assertDoesNotThrow(() -> service.uploadFile(documentData));
    }
}

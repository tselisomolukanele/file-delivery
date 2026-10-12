package com.example.documentservice.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;

@ApplicationScoped 
@Transactional
public class DocumentDataService {

    @Inject
    S3Client s3;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    public DocumentData downloadFile(String fileName) {
        var getObjectRequest = buildGetRequest(fileName);
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);

        byte[] data = objectBytes.asByteArray();

        if (data.length == 0) {
            throw new NotFoundException("The file is empty on S3.");
        }

        return new DocumentData(data, null, fileName, objectBytes.response().contentType());
    }

    public void uploadFile(DocumentData documentData) {

        try {
            var putResponse = s3.putObject(buildPutRequest(documentData), RequestBody.fromFile(documentData.getFile()));

            if (putResponse == null) {
                throw new InternalServerErrorException("Failed to upload the file to S3.");
            }
        } catch (Exception e) {
            Log.error("Failed to upload the file to S3.", e);
            throw new InternalServerErrorException("Failed to upload the file to S3.", e);
        }
    }

    private PutObjectRequest buildPutRequest(DocumentData documentData) {
    return PutObjectRequest.builder()
            .bucket(bucketName)
            .key(documentData.getFilename())
            .contentType(documentData.getMimetype())
            .build();
    }

    private GetObjectRequest buildGetRequest(String objectKey) {
    return GetObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();
    }
    
}

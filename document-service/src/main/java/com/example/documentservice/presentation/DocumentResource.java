package com.example.documentservice.presentation;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.MediaType;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.MultipartForm;

import software.amazon.awssdk.services.s3.S3Client;

@Path("/api")
public class DocumentResource {

    @Inject
    S3Client s3;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-owner"})
    public List<Map<String, String>> documents() {
        return List.of(
                Map.of("id", "DOC-1001", "name", "contract.pdf", "status", "approved"),
                Map.of("id", "DOC-1002", "name", "invoice.pdf", "status", "pending")
        );
    }

    @GET
    @Path("download/{objectKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(String objectKey) {

        var getObjectRequest = buildGetRequest(objectKey);
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);

        byte[] data = objectBytes.asByteArray();

        if (data.length == 0) {
            System.out.println("The file is empty on S3.");
        } else {
            System.out.println("Successfully read " + data.length + " bytes.");
        }

        ResponseBuilder response = Response.ok(data);
        response.header("Content-Disposition", "attachment;filename=" + objectKey);
        response.header("Content-Type", objectBytes.response().contentType());
        return response.build();
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm FormData formData) throws Exception {

        if (formData.filename == null || formData.filename.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (formData.mimetype == null || formData.mimetype.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        PutObjectResponse putResponse = s3.putObject(buildPutRequest(formData),
                RequestBody.fromFile(formData.data));
        if (putResponse != null) {
            return Response.ok().status(Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }

    protected PutObjectRequest buildPutRequest(FormData formData) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(formData.filename)
                .contentType(formData.mimetype)
                .build();
    }

    protected GetObjectRequest buildGetRequest(String objectKey) {
    return GetObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();
    }
}

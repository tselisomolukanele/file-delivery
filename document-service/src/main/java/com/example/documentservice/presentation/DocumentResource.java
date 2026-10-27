package com.example.documentservice.presentation;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.MediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.apache.tika.Tika;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.documentservice.service.CustomerService;
import com.example.documentservice.service.OrcherstratorService;

@Path("/api/document")
@Slf4j
public class DocumentResource {

    @Inject 
    OrcherstratorService orchestrator;

    @Inject 
    CustomerService customerService;

    @ConfigProperty(name = "document.link.base.url")
    String baseUrl;

    @GET
    @Path("/download/{objectKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(String objectKey, @QueryParam("otp") String otp) {

        if (otp == null || otp.isBlank()) {
            return Response
                .seeOther(
                    UriBuilder
                    .fromUri(baseUrl + "/confirm-otp.html")
                    .queryParam(
                        "downloadUrl",
                        UriBuilder.fromUri(baseUrl + "/api/document/download/" + objectKey)
                        .build() 
                    )   
                    .build()
                )
                .build();
        }

        var documentData = orchestrator.getDocumentData(objectKey);

        if (!customerService.validateOTP(otp, documentData.getOwner())) {
            return Response
                .seeOther(
                    UriBuilder
                    .fromUri(baseUrl + "/confirm-otp.html")
                    .queryParam(
                        "downloadUrl",
                        UriBuilder.fromUri(baseUrl + "/api/document/download/" + objectKey)
                        .build()
                    )
                    .queryParam("error", "Provide OTP is not valid")
                    .build()
                )
                .build();
        }

        ResponseBuilder response = Response.ok(documentData.getData());
        response.header("Content-Disposition", "attachment;filename=" + documentData.getFilename());
        response.header("Content-Type", documentData.getMimetype());
        return response.build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"file-uploader"})
    public Response uploadFile(FormData formData)
        throws Exception {

        if (formData.filename == null || formData.filename.isEmpty()) {
            return Response
                .status(Status.BAD_REQUEST.getStatusCode())
                .entity("Filename is required")
                .type(MediaType.TEXT_PLAIN)
                .build();
        }

        if (formData.customerName == null || formData.customerName.isEmpty()) {
            return Response
                .status(Status.BAD_REQUEST.getStatusCode())
                .entity("Customer is required")
                .type(MediaType.TEXT_PLAIN)
                .build();
        }

        Tika tika = new Tika();
        var fileType = tika.detect(formData.data);
        if (!"application/pdf".equals(fileType)) {
            return Response
                .status(Status.BAD_REQUEST.getStatusCode())
                .entity("File upload is not a valid PDF")
                .type(MediaType.TEXT_PLAIN)
                .build();
        }

        orchestrator.createAndUploadDocument(
            formData.customerName, 
            formData.data, 
            formData.filename, 
            fileType
        );

        return Response.ok().status(Status.CREATED).build();
    }
}

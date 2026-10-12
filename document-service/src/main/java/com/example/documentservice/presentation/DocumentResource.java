package com.example.documentservice.presentation;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import org.jboss.resteasy.reactive.MultipartForm;

import com.example.documentservice.service.DocumentData;
import com.example.documentservice.service.DocumentDataService;
import com.example.documentservice.service.DocumentLinkService;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/document")
public class DocumentResource {

    @Inject 
    DocumentLinkService documentLinkService;

    @Inject
    private SecurityIdentity securityIdentity;

    @Inject
    DocumentDataService documentDataService;

    @GET
    @Path("/download/{objectKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(String objectKey) {

        var documentLinkEntry = documentLinkService.getDocumentLink(objectKey, securityIdentity.getPrincipal().getName());

        var documentData = documentDataService.downloadFile(documentLinkEntry.getDocumentId());

        ResponseBuilder response = Response.ok(documentData.getData());
        response.header("Content-Disposition", "attachment;filename=" + documentData.getFilename());
        response.header("Content-Type", documentData.getMimetype());
        return response.build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"file-uploader"})
    public Response uploadFile(@MultipartForm FormData formData) throws Exception {

        if (formData.filename == null || formData.filename.isEmpty()) {
            return Response.status(Status.BAD_REQUEST.getStatusCode(), "Filename is required").build();
        }

        if (formData.mimetype == null || formData.mimetype.isEmpty()) {
            return Response.status(Status.BAD_REQUEST.getStatusCode(), "MIME type is required").build();
        }

        documentDataService.uploadFile(new DocumentData(null, formData.data, formData.filename, formData.mimetype));
        return Response.ok().status(Status.CREATED).build();
    }
}

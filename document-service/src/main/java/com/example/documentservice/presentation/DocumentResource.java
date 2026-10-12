package com.example.documentservice.presentation;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.MediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import com.example.documentservice.service.OrcherstratorService;

@Path("/document")
public class DocumentResource {

    @Inject 
    OrcherstratorService orchestrator;

    @GET
    @Path("/download/{objectKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(String objectKey) {

        var documentData = orchestrator.getDocumentData(objectKey);

        ResponseBuilder response = Response.ok(documentData.getData());
        response.header("Content-Disposition", "attachment;filename=" + documentData.getFilename());
        response.header("Content-Type", documentData.getMimetype());
        return response.build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"file-uploader"})
    public Response uploadFile(
        @MultipartForm FormData formData) 
        throws Exception {

        if (formData.filename == null || formData.filename.isEmpty()) {
            return Response.status(Status.BAD_REQUEST.getStatusCode(), "Filename is required").build();
        }

        if (formData.mimetype == null || formData.mimetype.isEmpty()) {
            return Response.status(Status.BAD_REQUEST.getStatusCode(), "MIME type is required").build();
        }

        orchestrator.createAndUploadDocument(formData.customerName, formData.data, formData.filename, formData.mimetype);

        return Response.ok().status(Status.CREATED).build();
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(ClientErrorException exception) {
        return RestResponse.status(exception.getResponse().getStatus(),  exception.getMessage());
    }
}
